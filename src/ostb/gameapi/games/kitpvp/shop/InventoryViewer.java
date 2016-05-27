package ostb.gameapi.games.kitpvp.shop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import anticheat.events.PlayerLeaveEvent;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.games.kitpvp.events.InventoryViewClickEvent;
import ostb.player.CoinsHandler;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class InventoryViewer implements Listener {
	private static Map<String, InventoryViewer> viewers = null;
	private Map<Integer, Integer> slots = null;
	protected String name = null;
	private String playerName = null;
	protected CoinsHandler coinsHandler = null;
	
	public InventoryViewer(String name, Player player) {
		this(name, player, true);
	}
	
	public InventoryViewer(String name, Player player, boolean openInv) {
		// Init
		if(viewers == null) {
			viewers = new HashMap<String, InventoryViewer>();
		}
		if(viewers.containsKey(player.getName())) {
			//viewers.get(player.getName()).remove();
		}
		viewers.put(player.getName(), this);
		this.name = name;
		playerName = player.getName();
		coinsHandler = CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData());
		if(openInv) {
			slots = new HashMap<Integer, Integer>();
			ItemStack dye = new ItemCreator(Material.INK_SACK, 8).setName(" ").getItemStack();//new ItemStack(Material.INK_SACK, 1, (byte) 8);
			Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
			// Place armor
			int [] slots = new int [] {7, 5, 3, 1};
			for(int a = 0; a < slots.length; ++a) {
				ItemStack armor = player.getInventory().getArmorContents()[a];
				if(armor == null || armor.getType() == Material.AIR) {
					armor = dye;
				} else {
					this.slots.put(slots[a], 36 + a);
				}
				inventory.setItem(slots[a], armor);
			}
			// Place the rest of the inventory
			for(int a = 0; a < 9 * 4; ++a) {
				ItemStack item = player.getInventory().getItem(a);
				if(item == null || item.getType() == Material.AIR) {
					item = dye;
				} else {
					this.slots.put(a + 18, a);
				}
				inventory.setItem(a + 18, item);
			}
			// Open inventory and register events
			player.openInventory(inventory);
		}
		EventUtil.register(this);
	}
	
	protected void remove() {
		HandlerList.unregisterAll(this);
		viewers.remove(playerName);
		slots.clear();
		slots = null;
		name = null;
		playerName = null;
		coinsHandler = null;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(slots != null && event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			int slot = event.getSlot();
			if(slots.containsKey(slot)) {
				Bukkit.getPluginManager().callEvent(new InventoryViewClickEvent(player, slots.get(slot), slot));
			} else {
				ItemStack item = event.getItem();
				if(item.getType() == Material.INK_SACK) {
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getInventory().getTitle().equals(name) && event.getPlayer().getName().equals(playerName)) {
			remove();
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		InventoryViewer viewer = viewers.get(event.getPlayer().getName());
		if(viewer != null) {
			viewer.remove();
		}
	}
}
