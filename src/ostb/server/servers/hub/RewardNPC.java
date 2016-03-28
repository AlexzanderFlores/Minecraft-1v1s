package ostb.server.servers.hub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.server.nms.npcs.NPCEntity;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class RewardNPC implements Listener {
	private String name = null;
	
	public RewardNPC() {
		name = "Reward NPC";
		new NPCEntity(EntityType.ZOMBIE, "&e&n" + name, new Location(Bukkit.getWorlds().get(0), 1672.5, 5, -1295.5, -44.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				open(player);
			}
		};
		EventUtil.register(this);
	}
	
	private void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		inventory.setItem(10, new ItemCreator(Material.NAME_TAG).setName("&bClaim Vote Rewards").getItemStack());
		inventory.setItem(12, new ItemCreator(Material.WATCH).setName("&bMonthly Reward").getItemStack());
		inventory.setItem(14, new ItemCreator(Material.WATCH).setName("&bDaily Reward").getItemStack());
		inventory.setItem(16, new ItemCreator(Material.JUKEBOX).setName("&bAnswer Poll").getItemStack());
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			
			event.setCancelled(true);
		}
	}
}
