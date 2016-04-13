package ostb.gameapi.shops;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.KitBase;
import ostb.gameapi.crates.SkyWarsCrate;
import ostb.gameapi.games.skywars.cages.Cage;
import ostb.gameapi.games.skywars.kits.Archer;
import ostb.gameapi.games.skywars.kits.Bomber;
import ostb.gameapi.games.skywars.kits.Builder;
import ostb.gameapi.games.skywars.kits.CowSlayer;
import ostb.gameapi.games.skywars.kits.Enchanter;
import ostb.gameapi.games.skywars.kits.Enderman;
import ostb.gameapi.games.skywars.kits.Fisherman;
import ostb.gameapi.games.skywars.kits.Looter;
import ostb.gameapi.games.skywars.kits.Medic;
import ostb.gameapi.games.skywars.kits.Miner;
import ostb.gameapi.games.skywars.kits.Ninja;
import ostb.gameapi.games.skywars.kits.Pyro;
import ostb.gameapi.games.skywars.kits.Spiderman;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;

public class SkyWarsShop extends ShopBase {
	private static SkyWarsShop instance = null;
	
	public SkyWarsShop() {
		super("Shop - Sky Wars", "kit.sky_wars.", DB.PLAYERS_COINS_SKY_WARS, Plugins.SKY_WARS_SOLO, 3);
		instance = this;
		new SkyWarsCrate();
		Cage.createCages();
		new Archer();
		new Builder();
		new Looter();
		new Enchanter();
		new Bomber();
		new Ninja();
		new Medic();
		new CowSlayer();
		new Enderman();
		new Fisherman();
		new Spiderman();
		new Pyro();
		new Miner();
	}
	
	public static SkyWarsShop getInstance() {
		if(instance == null) {
			new SkyWarsShop();
		}
		return instance;
	}
	
	@Override
	public void openShop(final Player player, final int page) {
		final InventoryView view = player.getOpenInventory();
		final Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
		player.openInventory(inventory);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(hasCrate(player, view)) {
					inventory.setItem(4, view.getItem(4));
				} else {
					SkyWarsCrate.addItem(player, inventory);
				}
				pages.put(player.getName(), page);
				if(page == 1) {
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && kit.getKitType().equals("kit")) {
							inventory.setItem(kit.getSlot(), kit.getIcon(player));
						}
					}
				} else if(page == 2) {
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && kit.getKitType().equals("small_cage")) {
							inventory.setItem(kit.getSlot(), kit.getIcon(player));
						}
					}
				} else if(page == 3) {
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && kit.getKitType().equals("big_cage")) {
							inventory.setItem(kit.getSlot(), kit.getIcon(player));
						}
					}
				}
				updateItems(player, inventory);
			}
		});
	}

	@Override
	public void updateInfoItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			InventoryView inventory = player.getOpenInventory();
			int page = getPage(player);
			if(page == 1) {
				inventory.setItem(inventory.getTopInventory().getSize() - 6, new KitData(player, "Kits Owned", "kit").getItem());
			} else if(page == 2) {
				inventory.setItem(inventory.getTopInventory().getSize() - 6, new KitData(player, "Small Cages Owned", "small_cage").getItem());
			} else if(page == 3) {
				inventory.setItem(inventory.getTopInventory().getSize() - 6, new KitData(player, "Big Cages Owned", "big_cage").getItem());
			}
		}
	}

	@Override
	public void updateInfoItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page == 1) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Kits Owned", "kit").getItem());
		} else if(page == 2) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Small Cages Owned", "small_cage").getItem());
		} else if(page == 3) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Big Cages Owned", "big_cage").getItem());
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.ARROW) {
				if(event.getSlot() == 0) {
					openShop(player, getPage(player) - 1);
					return;
				} else if(event.getSlot() == 8) {
					openShop(player, getPage(player) + 1);
					return;
				}
			}
			for(KitBase kit : KitBase.getKits()) {
				String name = ChatColor.stripColor(event.getItemTitle());
				if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
					if(kit.use(player)) {
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
					return;
				}
			}
		}
	}
}
