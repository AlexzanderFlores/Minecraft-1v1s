package ostb.server;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ostb.server.util.EventUtil;

public class Testing implements Listener {
	public Testing() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		
		Bukkit.getLogger().info("X: " + x);
		Bukkit.getLogger().info("Y: " + y);
		Bukkit.getLogger().info("Z: " + z);
		
		//-19, 66, 4
		
		//-19, 72, -4
		
		int minX = -19;
		int minY = 66;
		int minZ = -4;
		
		int maxX = -19;
		int maxY = 72;
		int maxZ = 4;
		
		if(x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
			//TODO: Stuff here
		}
	}
}
