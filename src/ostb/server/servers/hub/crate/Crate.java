package ostb.server.servers.hub.crate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class Crate implements Listener {
	private Item item = null;
	
	public Crate() {
		new Beacon();
		World world = Bukkit.getWorlds().get(0);
		Location location = new Location(world, 1651.5, 7, -1280.5);
		ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		item = world.dropItem(location, new ItemCreator(Material.TRIPWIRE_HOOK).addEnchantment(Enchantment.DURABILITY).getItemStack());
		armorStand.setPassenger(item);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if(event.getEntity().equals(item)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(event.getItem().equals(item)) {
			event.setCancelled(true);
		}
	}
}
