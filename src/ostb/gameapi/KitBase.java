package ostb.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.PlayerKitPurchaseEvent;
import ostb.customevents.player.PlayerKitSelectEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerPostKitPurchaseEvent;
import ostb.player.CoinsHandler;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.server.DB;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public abstract class KitBase implements Listener {
	private static List<KitBase> kits = null;
	private static int lastSlot = -1;
	private Plugins plugin = null;
	private String kitType = null;
	private String kitSubType = null;
	private ItemStack icon = null;
	private ItemStack helmet = null;
	private ItemStack chestplate = null;
	private ItemStack leggings = null;
	private ItemStack boots = null;
	private int slot = 0;
	private int price = 0;
	private List<String> users = null;
	private Map<String, Boolean> unlocked = null;
	private Rarity rarity = null;
	
	public KitBase(Plugins plugin, ItemStack icon, Rarity rarity, int price) {
		this(plugin, icon, rarity, price, -1);
	}
	
	public KitBase(Plugins plugin, ItemStack icon, Rarity rarity, int price, int slot) {
		if(kits == null) {
			kits = new ArrayList<KitBase>();
		}
		this.plugin = plugin;
		kitType = "kit";
		kitSubType = "";
		if(slot > -1) {
			lastSlot = slot;
		} else {
			slot = ++lastSlot;
		}
		this.rarity = rarity;
		this.price = price;
		this.slot = slot;
		ItemMeta meta = icon.getItemMeta();
		icon = new ItemCreator(icon).setName(meta.getDisplayName()).getItemStack();
		this.icon = icon;
		users = new ArrayList<String>();
		unlocked = new HashMap<String, Boolean>();
		EventUtil.register(this);
		kits.add(this);
	}
	
	public String getKitType() {
		return kitType;
	}
	
	public void setKitType(String kitType) {
		this.kitType = kitType;
	}
	
	public String getKitSubType() {
		return kitSubType;
	}
	
	public void setKitSubType(String kitSubType) {
		this.kitSubType = kitSubType;
	}
	
	public ItemStack getHelmet() {
		return helmet;
	}
	
	public KitBase setHelmet(ItemStack helmet) {
		this.helmet = helmet;
		return this;
	}
	
	public ItemStack getChestplate() {
		return chestplate;
	}
	
	public KitBase setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
		return this;
	}
	
	public ItemStack getLeggings() {
		return leggings;
	}
	
	public KitBase setLeggings(ItemStack leggings) {
		this.leggings = leggings;
		return this;
	}
	
	public ItemStack getBoots() {
		return boots;
	}
	
	public KitBase setBoots(ItemStack boots) {
		this.boots = boots;
		return this;
	}
	
	public boolean owns(Player player) {
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
				if(getPlugin() == OSTB.getPlugin()) {
					for(KitBase kit : kits) {
						if(kit.getKitType().equals(getKitType())) {
							kit.remove(player);
						}
					}
					users.add(player.getName());
					MessageHandler.sendMessage(player, "Selected &e" + getName());
					return true;
				}
			}
		} else if(price > 0) {
			PlayerKitPurchaseEvent event = new PlayerKitPurchaseEvent(player, this);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				if(CoinsHandler.getCoinsHandler(getPlugin()).getCoins(player) >= getPrice()) {
					giveKit(player);
					MessageHandler.sendMessage(player, "&6+1 Use for " + getName());
					use(player);
					CoinsHandler.getCoinsHandler(getPlugin()).addCoins(player, getPrice() * -1);
					return true;
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have enough coins for this kit");
				}
			}
		} else {
			MessageHandler.sendMessage(player, "&cUnlock this in crates &a/vote");
		}
		return false;
	}
	
	public void giveKit(Player player) {
		if(owns(player)) {
			MessageHandler.sendMessage(player, "&cYou already own &e" + getName());
			new TitleDisplayer(player, "&cYou already own", "&e" + getName()).display();
		} else {
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.PLAYERS_KITS.insert("'" + uuid.toString() + "', '" + getPermission() + "'");
				}
			});
			unlocked.put(player.getName(), true);
			MessageHandler.sendMessage(player, "You unlocked &e" + getName());
			new TitleDisplayer(player, "&bYou unlocked", "&e" + getName()).display();
			Bukkit.getPluginManager().callEvent(new PlayerPostKitPurchaseEvent(player, this));
		}
	}
	
	public void setDefaultKit(Player player) {
		final UUID uuid = player.getUniqueId();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String game = plugin.toString();
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
		boolean owns = owns(player);
		String name = (owns ? "&b" : "&c") + getName() + " " + (owns ? "&a" + UnicodeUtil.getUnicode("2714") : "&4" + UnicodeUtil.getUnicode("2716"));
		String lore = "&7Status: " + (owns ? "&aUnlocked" : "&cLocked");
		ItemStack item = getIcon();
		if(owns) {
			return new ItemCreator(item).setName(name).addLore(lore).getItemStack();
		} else {
			return new ItemCreator(Material.INK_SACK, 8).setName(name).setLores(item.getItemMeta().getLore()).getItemStack();
		}
	}
	
	public Rarity getKitRarity() {
		return rarity;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public int getPrice() {
		return price;
	}
	
	public boolean has(Player player) {
		for(String user : users) {
			Bukkit.getLogger().info(getName() + ": " + user);
		}
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
	
	public void executeArt(ArmorStand armorStand, boolean all, Player player) {
		armorStand.setHelmet(getHelmet());
		armorStand.setChestplate(getChestplate());
		armorStand.setLeggings(getLeggings());
		armorStand.setBoots(getBoots());
		armorStand.setItemInHand(getIcon());
	}
	
	public void disableArt(ArmorStand armorStand) {
		armorStand.setHelmet(new ItemStack(Material.AIR));
		armorStand.setChestplate(new ItemStack(Material.AIR));
		armorStand.setLeggings(new ItemStack(Material.AIR));
		armorStand.setBoots(new ItemStack(Material.AIR));
		armorStand.setItemInHand(new ItemStack(Material.AIR));
	}
	
	public abstract String getPermission();
	public abstract void execute();
	public abstract void execute(Player player);
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		owns(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		users.remove(event.getPlayer().getName());
		unlocked.remove(event.getPlayer().getName());
	}
}
