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
import ostb.gameapi.crates.SpeedUHCCrate;
import ostb.gameapi.games.speeduhc.kits.BlastMiner;
import ostb.gameapi.games.speeduhc.kits.Butcher;
import ostb.gameapi.games.speeduhc.kits.CowSlayer;
import ostb.gameapi.games.speeduhc.kits.Enchanter;
import ostb.gameapi.games.speeduhc.kits.FeatherFalling;
import ostb.gameapi.games.speeduhc.kits.Haste;
import ostb.gameapi.games.speeduhc.kits.Lumberjack;
import ostb.gameapi.games.speeduhc.kits.Miner;
import ostb.gameapi.games.speeduhc.kits.Swordsman;
import ostb.gameapi.games.speeduhc.kits.WallBreather;
import ostb.gameapi.kit.KitBase;
import ostb.server.DB;
import ostb.server.util.EffectUtil;

public class SpeedUHCShop extends ShopBase {
	private static SpeedUHCShop instance = null;
	
	public SpeedUHCShop() {
		super("Shop - Speed UHC", "kit.speed_uhc.", DB.PLAYERS_COINS_SPEED_UHC, Plugins.SPEED_UHC_KITS, 1);
		instance = this;
		if(OSTB.getPlugin() == Plugins.HUB) {
			new SpeedUHCCrate();
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
	
	public static SpeedUHCShop getInstance() {
		if(instance == null) {
			new SpeedUHCShop();
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
			SpeedUHCCrate.addItem(player, inventory);
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
			if(kit.getPlugin() == Plugins.SPEED_UHC_KITS && kit.getKitType().equals(type)) {
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
				if(kit.getPlugin() == Plugins.SPEED_UHC_KITS && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
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
