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
import ostb.gameapi.crates.HardcoreEliminationCrate;
import ostb.gameapi.games.hardcoreelimination.kits.BlastMiner;
import ostb.gameapi.games.hardcoreelimination.kits.Butcher;
import ostb.gameapi.games.hardcoreelimination.kits.CowSlayer;
import ostb.gameapi.games.hardcoreelimination.kits.Enchanter;
import ostb.gameapi.games.hardcoreelimination.kits.FeatherFalling;
import ostb.gameapi.games.hardcoreelimination.kits.Haste;
import ostb.gameapi.games.hardcoreelimination.kits.Lumberjack;
import ostb.gameapi.games.hardcoreelimination.kits.Miner;
import ostb.gameapi.games.hardcoreelimination.kits.Swordsman;
import ostb.gameapi.games.hardcoreelimination.kits.WallBreather;
import ostb.gameapi.kit.KitBase;
import ostb.server.DB;
import ostb.server.util.EffectUtil;

public class HardcoreEliminationShop extends ShopBase {
	private static HardcoreEliminationShop instance = null;
	
	public HardcoreEliminationShop() {
		super("Shop - Hardcore Elimination", "kit.hardcore_elimination.", DB.PLAYERS_COINS_HE, Plugins.HE_KITS, 1);
		instance = this;
		if(OSTB.getPlugin() == Plugins.HUB) {
			new HardcoreEliminationCrate();
		}
		new Butcher();
		new CowSlayer();
		new Swordsman();
		new Lumberjack();
		new Miner();
		new Enchanter();
		new WallBreather();
		new FeatherFalling();
		new Haste();
		new BlastMiner();
	}
	
	public static HardcoreEliminationShop getInstance() {
		if(instance == null) {
			new HardcoreEliminationShop();
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
			HardcoreEliminationCrate.addItem(player, inventory);
		}
		if(OSTB.getPlugin() == Plugins.HUB) {
			updateItems(player, inventory);
		} else {
			setBackItem(player, inventory);
			setNextItem(player, inventory);
		}
		String type = "";
		if(page == 1) {
			type = "kit";
		} else if(page == 2) {
			type = "none";
		}
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getPlugin() == Plugins.HE_KITS && kit.getKitType().equals(type)) {
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
			}
		}
	}

	@Override
	public void updateInfoItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page == 1) {
			inventory.setItem(inventory.getSize() - 6, new KitData(player, "Kits Owned", "kit").getItem());
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
				if(kit.getPlugin() == Plugins.HE_KITS && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
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
