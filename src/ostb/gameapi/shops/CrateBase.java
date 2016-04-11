package ostb.gameapi.shops;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class CrateBase implements Listener {
	private int [] slots = null;
	private Random random = new Random();
	private Player player = null;
	private String  title = null;
	private List<ItemStack> items = null;
	private int glassSpeed = 2;
	private int tickSpeed = 2;
	private float pitch = 1000.0f;
	
	public CrateBase(Player player, String  title, List<ItemStack> items) {
		slots = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
		random = new Random();
		this.player = player;
		this.title = title;
		this.items = items;
		Inventory inventory = Bukkit.createInventory(player, 9 * 5, title);
		inventory.setItem(13, new ItemCreator(Material.HOPPER).setName(" ").getItemStack());
		player.openInventory(inventory);
		EventUtil.register(this);
	}
	
	private void remove() {
		HandlerList.unregisterAll(this);
		slots = null;
		random = null;
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView.getTitle().equals(title)) {
			player.closeInventory();
		}
		player = null;
		title = null;
		items = null;
	}
	
	public List<ItemStack> getItems() {
		return items;
	}
	
	public void placeGlass() {
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView != null && inventoryView.getTitle().equals(title)) {
			EffectUtil.playSound(player, Sound.NOTE_PIANO, pitch);
			for(int slot : slots) {
				inventoryView.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, random.nextInt(15)).setName(" ").getItemStack());
			}
		} else {
			remove();
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == glassSpeed) {
			placeGlass();
		} else if(ticks == tickSpeed) {
			InventoryView inventoryView = player.getOpenInventory();
			if(inventoryView != null && inventoryView.getTitle().equals(title)) {
				for(int a = 20; a < 25; ++a) {
					inventoryView.setItem(a, items.get(random.nextInt(items.size())));
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			InventoryView inventoryView = player.getOpenInventory();
			if(inventoryView != null && inventoryView.getTitle().equals(title)) {
				remove();
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(event.getPlayer().getName().equals(player.getName())) {
			remove();
		}
	}
}
