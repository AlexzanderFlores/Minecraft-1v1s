package ostb.gameapi.games.speeduhc;

import java.io.File;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.server.BiomeSwap;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ZipUtil;

public class WorldHandler implements Listener {
	private static World world = null;
	private static double radius = 1500;
	private static boolean shrink = false;
	private static File [] files = null;
	
	public WorldHandler() {
		BiomeSwap.setUpUHC();
		files = new File(Bukkit.getWorldContainer().getPath() + "/../pregen/worlds").listFiles();
		if(files.length == 0) {
			ProPlugin.restartServer();
		} else {
			EventUtil.register(this);
		}
	}
	
	public static void shrink() {
		shrink = true;
	}
	
	public static void setBorder() {
		int r = ((int) radius) / 2;
		OSTB.getSidebar().removeScore(11);
		OSTB.getSidebar().setText("     ", 13);
		OSTB.getSidebar().setText("&eBorder", 12);
		OSTB.getSidebar().setText("&b" + r + "x" + r, 11);
		try {
			world.getWorldBorder().setSize(radius);
		} catch(Exception e) {
			
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
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameStarting(GameStartingEvent event) {
		files = new File(Bukkit.getWorldContainer().getPath() + "/../pregen/worlds").listFiles();
		final int index = new Random().nextInt(files.length - 1);
		File file = files[index];
		Bukkit.getLogger().info("Loading world #" + index);
		File zip = new File(Bukkit.getWorldContainer().getPath() + "/world.zip");
		FileHandler.copyFile(file, zip);
		ZipUtil.unZipIt(zip.getPath(), Bukkit.getWorldContainer().getPath() + "/");
		world = Bukkit.createWorld(new WorldCreator("world"));
		FileHandler.delete(zip);
		OSTB.getMiniGame().setMap(world);
		world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setDamageAmount(1.0d);
		world.getWorldBorder().setWarningDistance(25);
		world.getWorldBorder().setWarningTime(20);
		setBorder();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String world = index + "";
				if(DB.NETWORK_PREGEN_USES.isKeySet("world", world)) {
					int uses = DB.NETWORK_PREGEN_USES.getInt("world", world, "uses") + 1;
					DB.NETWORK_PREGEN_USES.updateInt("uses", uses, "world", world);
				} else {
					DB.NETWORK_PREGEN_USES.insert("'" + world + "', '1'");
				}
			}
		});
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && shrink) {
			radius -= .5;
			setBorder();
		}
	}
}
