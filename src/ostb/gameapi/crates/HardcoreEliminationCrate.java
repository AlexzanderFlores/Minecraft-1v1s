package ostb.gameapi.crates;

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
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.HardcoreEliminationShop;
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.servers.hub.items.features.FeatureItem;
import ostb.server.servers.hub.items.features.FeatureItem.FeatureType;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

public class HardcoreEliminationCrate implements Listener {
	private static List<String> delayed = null;
	private static List<FeatureItem> features = null;
	private static final int cost = 50;
	
	public HardcoreEliminationCrate() {
		delayed = new ArrayList<String>();
		features = new ArrayList<FeatureItem>();
		EventUtil.register(this);
	}
	
	public static void addItem(final Player player, final Inventory inventory) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String uuid = player.getUniqueId().toString();
				int month = Calendar.getInstance().get(Calendar.MONTH);
				int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
				String [] lores = null;
				if(OSTB.getPlugin() == Plugins.HUB) {
					lores = new String [] {
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
					};
				} else {
					lores = new String [] {
						"",
						"&eGet random kits, vote passes and more",
						"",
						"&4&nThis can only be used in the hub",
						""
					};
				}
				ItemCreator itemCreator = new ItemCreator(Material.CHEST).setName("&b" + getName()).setLores(lores);
				inventory.setItem(4, itemCreator.getItemStack());
			}
		});
	}
	
	private static String getName() {
		return "Hardcore Elimination Crate";
	}
	
	private static void updateItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(HardcoreEliminationShop.getInstance().getName())) {
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
	
	private void populateFeatures() {
		if(features.isEmpty()) {
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPlugin() == Plugins.HE_KITS) {
					features.add(new FeatureItem(kit.getName(), kit.getIcon(), kit.getKitRarity(), FeatureType.HARDCORE_ELIMINATION));
				}
			}
			features.add(new FeatureItem("15 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.COMMON, FeatureType.HARDCORE_ELIMINATION));
			features.add(new FeatureItem("25 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.COMMON, FeatureType.HARDCORE_ELIMINATION));
			features.add(new FeatureItem("35 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.UNCOMMON, FeatureType.HARDCORE_ELIMINATION));
			features.add(new FeatureItem("45 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.UNCOMMON, FeatureType.HARDCORE_ELIMINATION));
			features.add(new FeatureItem("60 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.RARE, FeatureType.HARDCORE_ELIMINATION));
			features.add(new FeatureItem("80 Coins", new ItemStack(Material.GOLD_INGOT), Rarity.RARE, FeatureType.HARDCORE_ELIMINATION));
			features.add(new FeatureItem("Crate Key x3", new ItemCreator(Material.TRIPWIRE_HOOK).setGlow(true).getItemStack(), Rarity.RARE, FeatureType.HARDCORE_ELIMINATION));
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(ChatColor.stripColor(event.getItemTitle()).equals(getName())) {
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
							populateFeatures();
							new CrateBase(player, Plugins.HE_KITS, HardcoreEliminationCrate.getName(), features).setLifetime(DB.HUB_LIFETIME_HE_CRATES_OPENED).setMonthly(DB.HUB_MONTHLY_HE_CRATES_OPENED).setWeekly(DB.HUB_WEEKLY_HE_CRATES_OPENED);
						} else {
							EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						}
					}
				} else if(event.getClickType() == ClickType.MIDDLE) {
					//TODO: Display item options and rarities
					populateFeatures();
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
			FeatureItem won = event.getItemWon();
			String name = ChatColor.stripColor(won.getName());
			if(won.getItemStack().getType() == Material.GOLD_INGOT) {
				int coins = Integer.valueOf(name.split(" ")[0]);
				CoinsHandler handler = CoinsHandler.getCoinsHandler(event.getPlugin());
				handler.addCoins(player, coins, false);
				return;
			} else if(won.getItemStack().getType() == Material.TRIPWIRE_HOOK) {
				giveKey(player.getUniqueId(), 3);
				return;
			}
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPlugin() == event.getPlugin() && name.equals(ChatColor.stripColor(kit.getName()))) {
					kit.giveKit(player);
					return;
				}
			}
		}
	}
}
