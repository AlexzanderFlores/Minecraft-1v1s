package ostb.gameapi.games.kitpvp.shop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.games.kitpvp.events.InventoryViewClick;
import ostb.server.util.EventUtil;

public class InventoryViewer implements Listener {
	private Map<Integer, Integer> slots = null;
	private String name = null;
	
	public InventoryViewer(String name, Player player) {
		// Init
		slots = new HashMap<Integer, Integer>();
		this.name = name;
		ItemStack dye = new ItemStack(Material.INK_SACK, 1, (byte) 8);
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
		// Place armor
		int [] slots = new int [] {7, 5, 3, 1};
		for(int a = 0; a < slots.length; ++a) {
			ItemStack armor = player.getInventory().getArmorContents()[a];
			if(armor == null || armor.getType() == Material.AIR) {
				armor = dye;
			}
			inventory.setItem(slots[a], armor);
			this.slots.put(slots[a], 39 - a);
		}
		// Place the rest of the inventory
		for(int a = 0; a < 9 * 4; ++a) {
			ItemStack item = player.getInventory().getItem(a);
			if(item == null || item.getType() == Material.AIR) {
				item = dye;
			}
			inventory.setItem(a + 18, item);
			this.slots.put(a + 18, a);
		}
		// Open inventory and register events
		player.openInventory(inventory);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			int slot = event.getSlot();
			if(slots.containsKey(slot)) {
				Bukkit.getPluginManager().callEvent(new InventoryViewClick(event.getPlayer(), slots.get(slot)));
			}
			event.setCancelled(true);
		}
	}
}
