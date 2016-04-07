package ostb.server.servers.hub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import npc.ostb.NPCEntity;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class ParkourNPC implements Listener {
	private LivingEntity livingEntity = null;
	private String name = null;
	
	public ParkourNPC() {
		name = "Parkour";
		livingEntity = new NPCEntity(EntityType.SKELETON, "&e&n" + name, new Location(Bukkit.getWorlds().get(0), 1673.5, 5, -1291.5)) {
			@Override
			public void onInteract(Player player) {
				Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
				inventory.setItem(11, new ItemCreator(Material.GOLD_BOOTS).setName("&bEndless Parkour").getItemStack());
				inventory.setItem(15, new ItemCreator(Material.CHAINMAIL_BOOTS).setName("&bParkour Course").getItemStack());
				player.openInventory(inventory);
			}
		}.getLivingEntity();
		livingEntity.getEquipment().setBoots(new ItemCreator(Material.DIAMOND_BOOTS).setGlow(true).getItemStack());
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 10) {
			livingEntity.setVelocity(new Vector(0, 0.25, 0));
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			if(event.getSlot() == 11) {
				player.teleport(new Location(player.getWorld(), 1598.5, 5, -1262.5, -270.0f, 0.0f));
			} else {
				player.teleport(new Location(player.getWorld(), 1598.5, 5, -1298.5, -270.0f, 0.0f));
			}
			event.setCancelled(true);
		}
	}
}
