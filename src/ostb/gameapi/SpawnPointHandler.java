package ostb.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import ostb.customevents.game.GameStartEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;

public class SpawnPointHandler implements Listener {
	private World world = null;
	private ConfigurationUtil config = null;
	private Map<String, Integer> locatedAt = null;
	private List<Location> spawns = null;
	
	public SpawnPointHandler(World world) {
		this(world, "spawns");
	}
	
	public SpawnPointHandler(World world, String file) {
		this.world = world;
		this.config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/" + file + ".yml");
		spawns = new ArrayList<Location>();
		locatedAt = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static Location getCenter(World world, List<Location> spawns) {
		double x1 = spawns.get(0).getX();
		double z1 = spawns.get(0).getZ();
		double x2 = spawns.get(12).getX();
		double z2 = spawns.get(12).getZ();
		double x = (spawns.get(0).getX() - x2) / 2;
		double z = (z1 - z2) / 2;
		double centerX = (x1 > x2 ? x1 - x : x2 + x);
		double centerZ = (z1 > z2 ? z1 - z : z2 + z);
		return new Location(world, centerX, 0, centerZ);
	}
	
	public Location getSpawnCenter() {
		return getCenter(world, getSpawns());
	}
	
	public void teleport(List<Player> players) {
		locatedAt.clear();
		int counter = 0;
		int numberOfSpawns = getSpawns().size();
		for(Player player : players) {
			if(counter >= numberOfSpawns) {
				counter = 0;
			}
			Location location = getSpawns().get(counter);
			player.teleport(location);
			locatedAt.put(player.getName(), counter++);
		}
	}
	
	public void swapLocations(Player one, Player two) {
		String oneName = one.getName();
		String twoName = two.getName();
		if(locatedAt.containsKey(oneName) && locatedAt.containsKey(twoName)) {
			int spawnOne = locatedAt.get(oneName);
			int spawnTwo = locatedAt.get(twoName);
			locatedAt.put(oneName, spawnTwo);
			locatedAt.put(twoName, spawnOne);
		}
	}
	
	public List<Location> getSpawns() {
		if(spawns == null || spawns.isEmpty()) {
			spawns = new ArrayList<Location>();
			for(String key : config.getConfig().getKeys(false)) {
				double x = config.getConfig().getDouble(key + ".x");
				double y = config.getConfig().getDouble(key + ".y");
				double z = config.getConfig().getDouble(key + ".z");
				float yaw = (float) config.getConfig().getDouble(key + ".yaw");
				float pitch = (float) config.getConfig().getDouble(key + ".pitch");
				spawns.add(new Location(world, x, y, z, yaw, pitch));
			}
			return spawns;
		}
		return spawns;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		locatedAt.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		locatedAt.clear();
		locatedAt = null;
		HandlerList.unregisterAll(this);
	}
}