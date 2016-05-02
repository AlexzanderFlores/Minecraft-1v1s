package ostb.gameapi.games.speeduhc;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.server.BiomeSwap;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ZipUtil;

public class WorldHandler implements Listener {
	private static World world = null;
	private static int radius = 750;
	
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
	
	public static void register() {
		EventUtil.register(new WorldHandler());
	}
	
	public static void setBorder() {
		world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setSize(radius);
		OSTB.getSidebar().removeScore(11);
		OSTB.getSidebar().setText("     ", 13);
		OSTB.getSidebar().setText("&eBorder", 12);
		OSTB.getSidebar().setText("&b" + radius, 11);
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
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			--radius;
			setBorder();
		}
	}
}