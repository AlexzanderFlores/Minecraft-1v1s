package ostb.gameapi.shops;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

public class SkyWarsCrate implements Listener {
	private static String name = null;
	private static List<String> delayed = null;
	private static final int cost = 50;
	private static List<ItemStack> items = null;
	
	public SkyWarsCrate() {
		name = "Sky Wars Crate";
		delayed = new ArrayList<String>();
		Random random = new Random();
		for(int a = 0; a < 40; ++a) {
			items.add(new ItemStack(Material.values()[random.nextInt(Material.values().length)]));
		}
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
		Bukkit.getLogger().info("sky wars crate: get keys");
		return DB.HUB_SKY_WARS_CRATE_KEYS.getInt("uuid", player.getUniqueId().toString(), "amount");
	}
	
	public static void giveKey(final Player player, final int toAdd) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID uuid = player.getUniqueId();
				if(DB.HUB_SKY_WARS_CRATE_KEYS.isUUIDSet(uuid)) {
					int amount = DB.HUB_SKY_WARS_CRATE_KEYS.getInt("uuid", uuid.toString(), "amount") + toAdd;
					DB.HUB_SKY_WARS_CRATE_KEYS.updateInt("amount", amount, "uuid", uuid.toString());
				} else {
					DB.HUB_SKY_WARS_CRATE_KEYS.insert("'" + uuid.toString() + "', '" + toAdd + "'");
				}
				updateItem(player);
				Bukkit.getLogger().info("sky wars crate: give keys");
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals(name)) {
			
			event.setCancelled(true);
		} else if(ChatColor.stripColor(event.getItemTitle()).equals(name)) {
			if(OSTB.getPlugin() == Plugins.HUB) {
				if(event.getClickType() == ClickType.LEFT) {
					if(delayed.contains(player.getName())) {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					} else {
						final String name = player.getName();
						delayed.add(name);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								delayed.remove(name);								
							}
						}, 20 * 2);
						if(getKeys(player) > 0) {
							new CrateBase(player, name, items);
						} else {
							EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						}
					}
				} else if(event.getClickType() == ClickType.MIDDLE) {
					//TODO: Display item options and rarities
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				} else if(event.getClickType() == ClickType.RIGHT) {
					CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.SKY_WARS_SOLO);
					int coins = coinsHandler.getCoins(player);
					if(coins >= cost) {
						coinsHandler.addCoins(player, cost * -1);
						giveKey(player, 1);
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
				}
			}
			event.setCancelled(true);
		}
	}
}