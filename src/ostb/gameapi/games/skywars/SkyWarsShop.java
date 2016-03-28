package ostb.gameapi.games.skywars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import ostb.OSTB.Plugins;
import ostb.customevents.player.CoinUpdateEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerPostKitPurchaseEvent;
import ostb.gameapi.KitBase;
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
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class SkyWarsShop implements Listener {
	private static String name = null;
	private static String permission = null;
	private static final int size = 9 * 6;
	
	public SkyWarsShop() {
		name = "Shop - Sky Wars";
		permission = "kit.sky_wars";
		new CoinsHandler(DB.PLAYERS_COINS_SKY_WARS, Plugins.SKY_WARS_SOLO);
		new SkyWarsCrate();
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
		EventUtil.register(this);
	}
	
	public static String getName() {
		if(name == null) {
			new SkyWarsShop();
		}
		return name;
	}
	
	public static String getPermission() {
		return permission;
	}
	
	public static void openShop(final Player player) {
		final Inventory inventory = Bukkit.createInventory(player, size, getName());
		player.openInventory(inventory);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				SkyWarsCrate.addItem(player, inventory);
				for(KitBase kit : KitBase.getKits()) {
					if(kit.getPlugin() == Plugins.SKY_WARS_SOLO) {
						inventory.setItem(kit.getSlot(), kit.getIcon(player));
					}
				}
				updateInfoItem(player, inventory);
				inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
				updateCoinsItem(player, inventory);
			}
		});
	}
	
	private static void updateCoinsItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			player.getOpenInventory().setItem(size - 4, CoinsHandler.getCoinsHandler(Plugins.SKY_WARS_SOLO).getItemStack(player));
		}
	}
	
	private static void updateCoinsItem(Player player, Inventory inventory) {
		inventory.setItem(inventory.getSize() - 4, CoinsHandler.getCoinsHandler(Plugins.SKY_WARS_SOLO).getItemStack(player));
	}
	
	private static void updateInfoItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			int total = 0;
			int owned = 0;
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPlugin() == Plugins.SKY_WARS_SOLO) {
					++total;
					if(kit.owns(player)) {
						++owned;
					}
				}
			}
			int percentage = (int) (owned * 100.0 / total + 0.5);
			player.getOpenInventory().setItem(size - 6, new ItemCreator(Material.DIAMOND).setName("&7Kits Owned: &e" + owned + "&8/&e" + total + " &7(&e" + percentage + "%&7)").getItemStack());
		}
	}
	
	private static void updateInfoItem(Player player, Inventory inventory) {
		int total = 0;
		int owned = 0;
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getPlugin() == Plugins.SKY_WARS_SOLO) {
				++total;
				if(kit.owns(player)) {
					++owned;
				}
			}
		}
		int percentage = (int) (owned * 100.0 / total + 0.5);
		inventory.setItem(inventory.getSize() - 6, new ItemCreator(Material.DIAMOND).setName("&7Kits Owned: &e" + owned + "&8/&e" + total + " &7(&e" + percentage + "%&7)").getItemStack());
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			Player player = event.getPlayer();
			for(KitBase kit : KitBase.getKits()) {
				String name = ChatColor.stripColor(event.getItemTitle());
				if(name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
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
	
	@EventHandler
	public void onPlayerPostKitPurchase(PlayerPostKitPurchaseEvent event) {
		Player player = event.getPlayer();
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			player.getOpenInventory().setItem(event.getKit().getSlot(), event.getKit().getIcon(player));
		}
	}
	
	@EventHandler
	public void onCoinUpdate(CoinUpdateEvent event) {
		updateCoinsItem(event.getPlayer());
		updateInfoItem(event.getPlayer());
	}
}
