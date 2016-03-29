package ostb.server.servers.hub.items.features.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFallingSand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import ostb.customevents.TimeEvent;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class SpinBlockEntity implements Listener {
	private FallingBlock block = null;
	private final Player player;
	private Location location = null;
	private Material material = null;
	private byte data = 0;
	private double radius = 2.0d;
	private double decreaseCounter = 0.10d;
	private int counter = 0;
	
	public SpinBlockEntity(Material material, Player player) {
		this(material, 0, player);
	}
	
	public SpinBlockEntity(Material material, int data, Player player) {
		this(material, (byte) data, player);
	}
	
	public SpinBlockEntity(Material material, byte data, Player player) {
		this.player = player;
		spinBlock(material, data);
	}
	
	public SpinBlockEntity(Material material, Location location) {
		this(material, 0, location);
	}
	
	public SpinBlockEntity(Material material, int data, Location location) {
		this(material, (byte) data, location);
	}
	
	public SpinBlockEntity(Material material, byte data, Location location) {
		this.player = null;
		this.location = location;
		spinBlock(material, data);
	}
	
	private void spinBlock(Material material, byte data) {
		this.material = material;
		this.data = data;
		if(player != null && player.isOnline()) {
			location = player.getLocation().add(0.5, 2, 0.5);
		}
		if(location != null) {
			block = location.getWorld().spawnFallingBlock(location, material, data);
			block.setDropItem(false);
			EventUtil.register(this);
		}
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getRadius() {
		return this.radius;
	}
	
	public void remove() {
		HandlerList.unregisterAll(this);
		if(block != null) {
			block.remove();
			block = null;
		}
		location = null;
		material = null;
		data = 0;
		radius = 0.0d;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			if(block != null) {
				CraftFallingSand craftFallingSand = (CraftFallingSand) block;
				craftFallingSand.getHandle().ticksLived = 1;
				if(player != null && player.isOnline()) {
					location = player.getLocation().add(0, 2, 0);
				} else if(location == null) {
					remove();
				}
				if(decreaseCounter > 0) {
					location = location.add(0, decreaseCounter * -1, 0);
					decreaseCounter -= 0.10;
				}
				if(location != null) {
					if(counter >= 360) {
						counter = 0;
					} else {
						counter += 6;
						double angle = counter * Math.PI / 180;
						double x = (double) (location.getX() + radius * Math.cos(angle));
						double z = (double) (location.getZ() + radius * Math.sin(angle));
						Location newLoc = new Location(location.getWorld(), x, location.getY() + 2, z);
						if(block.isValid()) {
							double directionX = newLoc.getX() - block.getLocation().getX();
							double directionY = block.isOnGround() ? 1 : location.getY() - block.getLocation().getY();
							double directionZ = newLoc.getZ() - block.getLocation().getZ();
							double factor = Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2) + Math.pow(directionZ, 2));
							double velocityX = directionX * factor;
							double velocityY = directionY * factor;
							double velocityZ = directionZ * factor;
							Vector vector = new Vector(velocityX, velocityY, velocityZ);
							block.setVelocity(vector);
							if(block.getLocation().distance(newLoc) >= 3) {
								block.remove();
								block = newLoc.getWorld().spawnFallingBlock(newLoc, material, data);
								block.setDropItem(false);
							}
						} else {
							block.remove();
							block = newLoc.getWorld().spawnFallingBlock(newLoc, material, data);
							block.setDropItem(false);
						}
					}
				}
			}
		}
	}
}
