package ostb.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.PlayerKitPurchaseEvent;
import ostb.customevents.player.PlayerKitSelectEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerPostKitPurchaseEvent;
import ostb.player.CoinsHandler;
import ostb.player.MessageHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public abstract class KitBase implements Listener {
	private static List<KitBase> kits = null;
	private static int lastSlot = -1;
	private Plugins plugin = null;
	private ItemStack icon = null;
	private int slot = 0;
	private int price = 0;
	private List<String> users = null;
	private Map<String, Boolean> unlocked = null;
	
	public KitBase(Plugins plugin, ItemStack icon, int price) {
		this(plugin, icon, price, -1);
	}
	
	public KitBase(Plugins plugin, ItemStack icon, int price, int slot) {
		if(kits == null) {
			kits = new ArrayList<KitBase>();
		}
		this.plugin = plugin;
		if(slot > -1) {
			lastSlot = slot;
		} else {
			slot = ++lastSlot;
		}
		this.price = price;
		this.slot = slot;
		ItemMeta meta = icon.getItemMeta();
		icon = new ItemCreator(icon).setName(meta.getDisplayName()).getItemStack();
		this.icon = icon;
		kits.add(this);
	}
	
	public boolean owns(Player player) {
		if(unlocked == null) {
			unlocked = new HashMap<String, Boolean>();
			EventUtil.register(this);
		}
		if(!unlocked.containsKey(player.getName())) {
			unlocked.put(player.getName(), DB.PLAYERS_KITS.isKeySet(new String [] {"uuid", "kit"}, new String [] {player.getUniqueId().toString(), getPermission()}));
		}
		return unlocked.get(player.getName());
	}
	
	public boolean use(Player player) {
		if(owns(player)) {
			PlayerKitSelectEvent event = new PlayerKitSelectEvent(player, this);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				setDefaultKit(player);
				//if(getPlugin() == OSTB.getPlugin()) {
					for(KitBase kit : kits) {
						kit.remove(player);
					}
					if(users == null) {
						users = new ArrayList<String>();
					}
					users.add(player.getName());
					MessageHandler.sendMessage(player, "Selected &e" + getName());
					return true;
				//}
			}
		} else if(price > 0) {
			if(CoinsHandler.getCoinsHandler(getPlugin()).getCoins(player) >= getPrice()) {
				PlayerKitPurchaseEvent event = new PlayerKitPurchaseEvent(player, this);
				Bukkit.getPluginManager().callEvent(event);
				if(!event.isCancelled()) {
					giveKit(player);
					MessageHandler.sendMessage(player, "Unlocked &e" + getName());
					use(player);
					CoinsHandler.getCoinsHandler(getPlugin()).addCoins(player, getPrice() * -1);
					return true;
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have enough coins for this kit");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou cannot purchase this kit with coins");
		}
		return false;
	}
	
	public void giveKit(Player player) {
		if(!owns(player)) {
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.PLAYERS_KITS.insert("'" + uuid.toString() + "', '" + getPermission() + "'");
				}
			});
			unlocked.put(player.getName(), true);
			Bukkit.getPluginManager().callEvent(new PlayerPostKitPurchaseEvent(player, this));
		}
	}
	
	public void setDefaultKit(Player player) {
		final UUID uuid = player.getUniqueId();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String game = OSTB.getPlugin().toString();
				String [] keys = new String [] {"uuid", "game"};
				String [] values = new String [] {uuid.toString(), game};
				if(DB.PLAYERS_DEFAULT_KITS.isKeySet(keys, values)) {
					DB.PLAYERS_DEFAULT_KITS.updateString("kit", getName(), keys, values);
				} else {
					DB.PLAYERS_DEFAULT_KITS.insert("'" + uuid.toString() + "', '" + game + "', '" + getName() + "'");
				}
			}
		});
	}
	
	public Plugins getPlugin() {
		return plugin;
	}
	
	public String getName() {
		return ChatColor.stripColor(getIcon().getItemMeta().getDisplayName());
	}
	
	public ItemStack getIcon() {
		return icon.clone();
	}
	
	public ItemStack getIcon(Player player) {
		boolean own = owns(player);
		String name = (own ? "&b" : "&c") + getName() + " " + (own ? "&a" + UnicodeUtil.getUnicode("2714") : "&4" + UnicodeUtil.getUnicode("2716"));
		String lore = "&7Status: " + (own ? "&bUnlocked" : "&cLocked");
		return new ItemCreator(icon.clone()).setName(name).addLore(lore).getItemStack();
	}
	
	public int getSlot() {
		return slot;
	}
	
	public int getPrice() {
		return price;
	}
	
	public boolean has(Player player) {
		return users != null && users.contains(player.getName()) && !SpectatorHandler.contains(player);
	}
	
	public void remove(Player player) {
		if(has(player)) {
			users.remove(player.getName());
		}
	}
	
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(has(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static List<KitBase> getKits() {
		return kits;
	}
	
	public static int getLastSlot() {
		return lastSlot;
	}
	
	public abstract String getPermission();
	public abstract void execute();
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(users != null) {
			users.remove(event.getPlayer().getName());
		}
		unlocked.remove(event.getPlayer().getName());
	}
}
