package ostb.server.servers.hub.items.features.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import ostb.server.util.CircleUtil;

public class SpinBlockEntity {
	private ArmorStand stand = null;
	private final Player player;
	private Location location = null;
	private double radius = 2.0d;
	
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
		if(player != null && player.isOnline()) {
			location = player.getLocation().add(0.5, 2, 0.5);
		}
		if(location != null) {
			stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setHelmet(new ItemStack(material, data));
			new CircleUtil(player, 1, 12) {
				@Override
				public void run(Vector vector, Location location) {
					if(stand == null) {
						delete();
						remove();
					} else if(!player.isOnline()) {
						delete();
						remove();
					} else {
						stand.teleport(location.add(0, 0.5, 0));
					}
				}
			};
		}
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getRadius() {
		return this.radius;
	}
	
	public void remove() {
		if(stand != null) {
			stand.remove();
			stand = null;
		}
		location = null;
		radius = 0.0d;
	}
}
