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

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
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
import ostb.gameapi.kit.KitBase;
import ostb.server.DB;
import ostb.server.util.EffectUtil;

public class SkyWarsShop extends ShopBase {
	private static SkyWarsShop instance = null;
	
	public SkyWarsShop() {
		super("Shop - Sky Wars", "kit.sky_wars.", DB.PLAYERS_COINS_SKY_WARS, Plugins.SW, 3);
		instance = this;
		if(OSTB.getPlugin() == Plugins.HUB) {
			new SkyWarsCrate();
		}
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
	public void openShop(Player player, int page) {
		InventoryView view = player.getOpenInventory();
		Inventory inventory = Bukkit.createInventory(player, 9 * (OSTB.getPlugin() == Plugins.HUB ? 6 : 5), getName());
		pages.put(player.getName(), page);
		if(hasCrate(player, view)) {
			inventory.setItem(4, view.getItem(4));
		} else {
			SkyWarsCrate.addItem(player, inventory);
		}
		if(OSTB.getPlugin() == Plugins.HUB) {
			updateItems(player, inventory);
		} else {
			setBackItem(player, inventory);
			setNextItem(player, inventory);
			updateCoinsItem(player, inventory);
		}
		String type = "";
		String subType = "";
		if(page == 1) {
			type = "kit";
		} else if(page == 2) {
			type = "cage";
			subType = "small_cage";
		} else if(page == 3) {
			type = "cage";
			subType = "big_cage";
		}
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getPluginData().equals(Plugins.SW.getData()) && type.equals(kit.getKitType()) && subType.equals(kit.getKitSubType())) {
				inventory.setItem(kit.getSlot(), kit.getIcon(player));
			}
		}
		player.openInventory(inventory);
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
				inventory.setItem(inventory.getTopInventory().getSize() - 6, new KitData(player, "Small Cages Owned", "cage", "small_cage").getItem());
			} else if(page == 3) {
				inventory.setItem(inventory.getTopInventory().getSize() - 6, new KitData(player, "Big Cages Owned", "cage", "big_cage").getItem());
			}
		}
	}

	@Override
	public void updateInfoItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page == 1) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Kits Owned", "kit").getItem());
		} else if(page == 2) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Small Cages Owned", "cage", "small_cage").getItem());
		} else if(page == 3) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Big Cages Owned", "cage", "big_cage").getItem());
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.ARROW) {
				InventoryView inv = player.getOpenInventory();
				int size = inv.getTopInventory().getSize();
				if(event.getSlot() == size - 8) {
					openShop(player, getPage(player) - 1);
					return;
				} else if(event.getSlot() == size - 2) {
					openShop(player, getPage(player) + 1);
					return;
				}
			}
			for(KitBase kit : KitBase.getKits()) {
				String name = ChatColor.stripColor(event.getItemTitle());
				if(kit.getPluginData().equals(Plugins.SW.getData()) && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
					if(!kit.use(player)) {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
					return;
				}
			}
		}
	}
}
