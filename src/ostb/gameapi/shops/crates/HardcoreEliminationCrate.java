package ostb.gameapi.shops.crates;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.HardcoreEliminationShop;
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

public class HardcoreEliminationCrate implements Listener {
	private static String name = null;
	private static List<String> delayed = null;
	private static final int cost = 50;
	
	public HardcoreEliminationCrate() {
		name = "Hardcore Elimination Crate";
		delayed = new ArrayList<String>();
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
					"&eGet random kits, vote passes and more",
					"",
					"&7Coins: &a" + cost,
					"&7Get one &aFREE &7through &a/vote",
					"",
					"&7Left click to open a crate",
					"&7Middle click to view possible items",
					"&7Right click to purchase a key",
					"",
					"&eKeys owned: &a" + getKeys(player),
					"&7Lifetime Hardcore crates opened: &a" + DB.HUB_LIFETIME_HE_CRATES_OPENED.getInt("uuid", uuid, "amount"),
					"&7Monthly Hardcore crates opened: &a" + DB.HUB_MONTHLY_HE_CRATES_OPENED.getInt(new String [] {"uuid", "month"}, new String [] {uuid, month + ""}, "amount"),
					"&7Weekly Hardcore crates opened: &a" + DB.HUB_WEEKLY_HE_CRATES_OPENED.getInt(new String [] {"uuid", "week"}, new String [] {uuid, week + ""}, "amount"),
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
		if(title != null && title.equals(HardcoreEliminationShop.getName())) {
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
		Bukkit.getLogger().info("hardcore elimination crate: get keys");
		return DB.HUB_HE_CRATE_KEYS.getInt("uuid", player.getUniqueId().toString(), "amount");
	}
	
	public static void giveKey(final UUID uuid, final int toAdd) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(DB.HUB_HE_CRATE_KEYS.isUUIDSet(uuid)) {
					int amount = DB.HUB_HE_CRATE_KEYS.getInt("uuid", uuid.toString(), "amount") + toAdd;
					DB.HUB_HE_CRATE_KEYS.updateInt("amount", amount, "uuid", uuid.toString());
				} else {
					DB.HUB_HE_CRATE_KEYS.insert("'" + uuid.toString() + "', '" + toAdd + "'");
				}
				Player player = Bukkit.getPlayer(uuid);
				if(player != null) {
					updateItem(player);
				}
				Bukkit.getLogger().info("hardcore elimination crate: give key");
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(ChatColor.stripColor(event.getItemTitle()).equals(name)) {
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
							List<ItemStack> items = new ArrayList<ItemStack>();
							for(KitBase kit : KitBase.getKits()) {
								if(kit.getPlugin() == Plugins.HE_KITS) {
									items.add(new ItemCreator(kit.getIcon()).setName("&b" + kit.getIcon().getItemMeta().getDisplayName()).setLores(new String [] {}).getItemStack());
								}
							}
							new CrateBase(player, Plugins.HE_KITS, HardcoreEliminationCrate.name, items);
							items = null;
						} else {
							EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						}
					}
				} else if(event.getClickType() == ClickType.MIDDLE) {
					//TODO: Display item options and rarities
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				} else if(event.getClickType() == ClickType.RIGHT) {
					CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.HE_KITS);
					int coins = coinsHandler.getCoins(player);
					if(coins >= cost) {
						coinsHandler.addCoins(player, cost * -1);
						giveKey(player.getUniqueId(), 1);
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
	public void onCrateFinished(CrateFinishedEvent event) {
		if(event.getPlugin() == Plugins.HE_KITS) {
			Player player = event.getPlayer();
			giveKey(player.getUniqueId(), -1);
		}
	}
}
