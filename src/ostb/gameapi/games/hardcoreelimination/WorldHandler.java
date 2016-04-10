package ostb.gameapi.games.hardcoreelimination;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.server.BiomeSwap;
import ostb.server.util.FileHandler;
import ostb.server.util.ZipUtil;

public class WorldHandler {
	private static World world = null;
	
	public WorldHandler() {
		BiomeSwap.setUpUHC();
		File [] files = new File(Bukkit.getWorldContainer().getPath() + "/../resources/maps/pregen/").listFiles();
		if(files.length == 0) {
			ProPlugin.restartServer();
		} else {
			File file = files[files.length - 1];
			File zip = new File(Bukkit.getWorldContainer().getPath() + "/world.zip");
			FileHandler.copyFile(file, zip);
			ZipUtil.unZipIt(zip.getPath(), Bukkit.getWorldContainer().getPath() + "/");
			world = Bukkit.createWorld(new WorldCreator("world"));
			FileHandler.delete(file);
			FileHandler.delete(zip);
			OSTB.getMiniGame().setMap(world);
		}
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
