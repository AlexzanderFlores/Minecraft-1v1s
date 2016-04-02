package ostb.gameapi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import npc.ostb.NPCEntity;
import ostb.ProPlugin;
import ostb.server.util.EventUtil;

public class KitSelection implements Listener {
	private String name = null;
	
	public KitSelection(Player player) {
		name = player.getName();
		EventUtil.register(this);
	}
	
	public Player getPlayer() {
		return ProPlugin.getPlayer(name);
	}
	
	public void spawnNPCs() {
		Player player = getPlayer();
		if(player != null) {
			Vector [] offsets = new Vector [3];
			float yaw = player.getLocation().getYaw();
			Bukkit.getLogger().info("Yaw: " + yaw);
			if(yaw <= 45 || yaw > 315) { // south
				offsets[0] = new Vector(2, 0, 2);
				offsets[1] = new Vector(0, 0, 2);
				offsets[2] = new Vector(-2, 0, 2);
			} else if(yaw > 45 && yaw <= 135) { // east
				offsets[0] = new Vector(2, 0, -2);
				offsets[1] = new Vector(2, 0, 0);
				offsets[2] = new Vector(2, 0, 2);
			} else if(yaw > 135 && yaw <= 225) { // north
				offsets[0] = new Vector(-2, 0, -2);
				offsets[1] = new Vector(0, 0, -2);
				offsets[2] = new Vector(2, 0, -2);
			} else { // west
				offsets[0] = new Vector(-2, 0, -2);
				offsets[1] = new Vector(-2, 0, 0);
				offsets[2] = new Vector(-2, 0, -2);
			}
			for(int a = 0; a < 3; ++a) {
				Location location = player.getLocation();
				location.setPitch(0.0f);
				location = location.add(offsets[a]);
				new NPCEntity(EntityType.SKELETON, "&cTest Kit NPC", location) {
					@Override
					public void onInteract(Player player) {
						remove();
					}
				};
				
			}
		}
	}
}
