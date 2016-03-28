package ostb.gameapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerSpectateStartEvent;
import ostb.server.DB;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class MapRating implements Listener {
	private String name = null;
	private ItemStack ratingItem = null;
	private Map<String, Integer> ratings = null;
	
	public MapRating() {
		name = "Rate Map";
		ratingItem = new ItemCreator(Material.NETHER_STAR).setName("&e" + name).getItemStack();
		ratings = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		event.getPlayer().getInventory().setItem(7, ratingItem);
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item != null && item.equals(ratingItem)) {
			String fullStar = " " + UnicodeUtil.getUnicode("2726");
			String emptyStar = " &7" + UnicodeUtil.getUnicode("2727");
			Inventory inventory = Bukkit.createInventory(player, 9, "Testing");
			inventory.setItem(0, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + emptyStar + emptyStar + emptyStar + emptyStar).getItemStack());
			inventory.setItem(2, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + emptyStar + emptyStar + emptyStar).setAmount(2).getItemStack());
			inventory.setItem(4, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + emptyStar + emptyStar).setAmount(3).getItemStack());
			inventory.setItem(6, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + fullStar + emptyStar).setAmount(4).getItemStack());
			inventory.setItem(8, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + fullStar + fullStar).setAmount(5).getItemStack());
			player.openInventory(inventory);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			ratings.put(event.getPlayer().getName(), event.getItem().getAmount());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(ratings.containsKey(name)) {
			UUID uuid = event.getUUID();
			MiniGame miniGame = OSTB.getMiniGame();
			DB.NETWORK_MAP_RATINGS.insert("'" + uuid.toString() + "', '" + miniGame.getMap().getName() + "', '" + ratings.get(name) + "'");
			ratings.remove(name);
		}
	}
}
