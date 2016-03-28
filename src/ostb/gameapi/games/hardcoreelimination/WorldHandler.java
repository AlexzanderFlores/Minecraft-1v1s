package ostb.gameapi.games.hardcoreelimination;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import ostb.server.BiomeSwap;
import ostb.server.util.FileHandler;

public class WorldHandler {
	private static World world = null;
	
	public WorldHandler() {
		BiomeSwap.setUpUHC();
		generateWorld();
	}
	
	private void generateWorld() {
		if(world != null) {
			Bukkit.unloadWorld(world, false);
			FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world.getName()));
		}
		world = Bukkit.createWorld(new WorldCreator("world"));
		world.setSpawnLocation(0, getGround(new Location(world, 0, 0, 0)).getBlockY(), 0);
		world.setGameRuleValue("naturalRegeneration", "false");
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(6000);
	}
	
	public static Location getGround(Location location) {
		location.setY(250);
        while(location.getBlock().getType() == Material.AIR) {
        	location.setY(location.getBlockY() - 1);
        }
        return location.add(0, 1, 0);
	}
	
	public static World getWorld() {
		return world;
	}
}
