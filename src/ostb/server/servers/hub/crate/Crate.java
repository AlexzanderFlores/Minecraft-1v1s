package ostb.server.servers.hub.crate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class Crate {
	public Crate() {
		new Beacon();
		World world = Bukkit.getWorlds().get(0);
		Location location = new Location(world, 1651.5, 7, -1280.5);
		ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
	}
}
