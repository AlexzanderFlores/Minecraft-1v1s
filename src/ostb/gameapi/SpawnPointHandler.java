package ostb.gameapi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import ostb.server.util.ConfigurationUtil;

public class SpawnPointHandler {
	private World world = null;
	private ConfigurationUtil config = null;
	private List<Location> spawns = null;
	
	public SpawnPointHandler(World world) {
		this(world, "spawns");
	}
	
	public SpawnPointHandler(World world, String file) {
		this.world = world;
		this.config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/" + file + ".yml");
		spawns = new ArrayList<Location>();
	}
	
	public ConfigurationUtil getConfig() {
		return config;
	}
	
	public void teleport(List<Player> players) {
		int counter = 0;
		int numberOfSpawns = getSpawns().size();
		for(Player player : players) {
			if(counter >= numberOfSpawns) {
				counter = 0;
			}
			Location location = getSpawns().get(counter);
			Bukkit.getLogger().info(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
			player.teleport(location);
		}
	}
	
	public List<Location> getSpawns() {
		if(spawns == null || spawns.isEmpty()) {
			spawns = new ArrayList<Location>();
			for(String key : config.getConfig().getKeys(false)) {
				String [] location = config.getConfig().getString(key).split(",");
				double x = Double.valueOf(location[0]);
				double y = Double.valueOf(location[1]);
				double z = Double.valueOf(location[2]);
				spawns.add(new Location(world, x, y, z));
			}
		}
		return spawns;
	}
}