package ostb.server.servers.hub.parkous;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import anticheat.util.EventUtil;
import ostb.server.util.CircleUtil;

public class Parkour implements Listener {
	public Parkour() {
		World world = Bukkit.getWorlds().get(0);
		for(Location location : new Location [] {new Location(world, 1571, 6, -1301), new Location(world, 1533, 17, -1297), new Location(world, 1526, 19, -1299), new Location(world, 1266, 22, -1298)}) {
			final ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			armorStand.setVisible(false);
			armorStand.setHelmet(new ItemStack(Material.LAVA));
			new CircleUtil(location, .85, 6) {
				@Override
				public void run(Vector vector, Location location) {
					armorStand.teleport(location);
				}
			};
		}
		EventUtil.register(this);
	}
}
