package network.gameapi.games.kitpvp;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.server.util.EventUtil;

public class SpawnHandler implements Listener {
	public static int spawnY = 45;
	
	public SpawnHandler() {
		EventUtil.register(this);
	}
	
	public static Location spawn(Player player) {
		Random random = new Random();
		int range = 3;
		Location spawn = player.getWorld().getSpawnLocation();
		spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setY(spawn.getY() + 2.5d);
		spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		player.teleport(spawn);
		return spawn;
	}
	
	public static boolean isAtSpawn(Entity entity) {
		return entity.getLocation().getY() >= spawnY;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		spawn(event.getPlayer());
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isAtSpawn(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(isAtSpawn(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerSpectateEnd(PlayerSpectatorEvent event) {
		if(event.getState() == SpectatorState.END) {
			spawn(event.getPlayer());
		}
	}
}