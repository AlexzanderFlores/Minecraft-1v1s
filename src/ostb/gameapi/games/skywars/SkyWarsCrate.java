package ostb.gameapi.games.skywars;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

public class SkyWarsCrate implements Listener {
	private static String name = null;
	private static final int cost = 25;
	private static Map<String, Integer> keys = null;
	
	public SkyWarsCrate() {
		name = "Sky Wars Crate";
		keys = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static void addItem(final Player player, final Inventory inventory) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String uuid = player.getUniqueId().toString();
				int month = Calendar.getInstance().get(Calendar.MONTH);
				int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
				ItemCreator itemCreator = new ItemCreator(Material.CHEST).setName("&b" + name).setLores(new String [] {
					"",
					"&eGet random kits, cages and more!",
					"",
					"&7Coins:&a " + cost,
					"&7Get one &aFREE &7through &a/vote",
					"",
					"&7Left click to open a crate",
					"&7Middle click to view possible items",
					"&7Right click to purchase a key",
					"",
					"&eKeys owned: &a" + getKeys(player),
					"&7Lifetime Sky Wars crates opened: &a" + DB.HUB_LIFETIME_SKY_WARS_CRATES_OPENED.getInt("uuid", uuid, "amount"),
					"&7Monthly Sky Wars crates opened: &a" + DB.HUB_MONTHLY_SKY_WARS_CRATES_OPENED.getInt(new String [] {"uuid", "month"}, new String [] {uuid, month + ""}, "amount"),
					"&7Weekly Sky Wars crates opened: &a" + DB.HUB_WEEKLY_SKY_WARS_CRATES_OPENED.getInt(new String [] {"uuid", "week"}, new String [] {uuid, week + ""}, "amount"),
					""
				});
				if(OSTB.getPlugin() != Plugins.HUB) {
					itemCreator.addLore("&4&nThis can only be used in the hub");
					itemCreator.addLore("");
				}
				inventory.setItem(4, itemCreator.getItemStack());
			}
		});
	}
	
	private static void updateItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(SkyWarsShop.getName())) {
			ItemCreator itemCreator = new ItemCreator(player.getOpenInventory().getItem(4));
			int index = -1;
			for(String lore : itemCreator.getLores()) {
				++index;
				if(ChatColor.stripColor(lore).startsWith("Keys")) {
					String [] lores = itemCreator.getLoreArray();
					lores[index] = StringUtil.color("&eKeys owned: &a" + getKeys(player));
					itemCreator.setLores(lores);
					player.getOpenInventory().setItem(4, itemCreator.getItemStack());
					break;
				}
			}
		}
	}
	
	private static int getKeys(Player player) {
		if(!keys.containsKey(player.getName())) {
			keys.put(player.getName(), DB.HUB_SKY_WARS_CRATE_KEYS.getInt("uuid", player.getUniqueId().toString(), "amount"));
		}
		return keys.get(player.getName());
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			
			event.setCancelled(true);
		} else if(ChatColor.stripColor(event.getItemTitle()).equals(name)) {
			if(OSTB.getPlugin() == Plugins.HUB) {
				if(event.getClickType() == ClickType.LEFT) {
					Player player = event.getPlayer();
					if(getKeys(player) > 0) {
						//TODO: Run
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
				} else if(event.getClickType() == ClickType.MIDDLE) {
					//TODO: Display item options and rarities
				} else if(event.getClickType() == ClickType.RIGHT) {
					final Player player = event.getPlayer();
					CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.SKY_WARS);
					int coins = coinsHandler.getCoins(player);
					if(coins >= cost) {
						coinsHandler.addCoins(player, cost * -1);
						keys.put(player.getName(), getKeys(player) + 1);
						updateItem(player);
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(keys.containsKey(name)) {
			UUID uuid = event.getUUID();
			if(DB.HUB_SKY_WARS_CRATE_KEYS.isUUIDSet(uuid)) {
				DB.HUB_SKY_WARS_CRATE_KEYS.updateInt("amount", keys.get(name), "uuid", uuid.toString());
			} else {
				DB.HUB_SKY_WARS_CRATE_KEYS.insert("'" + uuid.toString() + "', '" + keys.get(name) + "'");
			}
			keys.remove(name);
		}
	}
}
