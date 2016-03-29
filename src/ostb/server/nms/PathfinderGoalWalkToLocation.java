package ostb.server.nms;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.PathfinderGoal;

import org.bukkit.Location;

public class PathfinderGoalWalkToLocation extends PathfinderGoal {
	private EntityInsentient entityInsentient = null;
	private float speed = 1.0f;
	private Location location = null;
	
	public PathfinderGoalWalkToLocation(EntityInsentient entityInsentient, float speed, Location location) {
		this.entityInsentient = entityInsentient;
		this.speed = speed;
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	@Override
	public boolean a() {
		c();
		return true;
	}
	
	@Override
	public void c() {
		int x = getLocation().getBlockX();
		int y = getLocation().getBlockY();
		int z = getLocation().getBlockZ();
		this.entityInsentient.world.getWorld().loadChunk(x, z);
		this.entityInsentient.getNavigation().a(x, y, z, speed);
	}
}
