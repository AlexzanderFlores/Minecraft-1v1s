package ostb.server.servers.pregenerator;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.wimbli.WorldBorder.Events.WorldBorderFillFinishedEvent;

import ostb.ProPlugin;
import ostb.customevents.ServerRestartEvent;
import ostb.customevents.TimeEvent;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ZipUtil;

public class Events implements Listener {
	private boolean running = false;
	private World world = null;
	private final int max = 30;
	
	public Events() {
		EventUtil.register(this);
	}
	
	private int getWorlds() {
		return new File(getPath()).listFiles().length;
	}
	
	private String getPath() {
		return Bukkit.getWorldContainer().getPath() + "/../resources/maps/pregen/";
	}
	
	private Location getGround(Location location) {
		location.setY(250);
        while(location.getBlock().getType() == Material.AIR) {
        	location.setY(location.getBlockY() - 1);
        }
        return location.add(0, 1, 0);
	}
	
	private void run() {
		running = true;
		world = Bukkit.createWorld(new WorldCreator("world"));
		world.setSpawnLocation(0, getGround(new Location(world, 0, 0, 0)).getBlockY(), 0);
		world.setTime(0);
		world.setGameRuleValue("naturalRegeneration", "false");
		world.setDifficulty(Difficulty.HARD);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + world.getName() + " set 1500 1500 0 0");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + world.getName() + " fill 60");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm");
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(!running && ticks == (20 * 5)) {
			int worlds = getWorlds();
			if(worlds < max) {
				run();
			}
		}
	}
	
	@EventHandler
	public void onWorldBorderFinish(WorldBorderFillFinishedEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				String target = "";
				for(int a = 0; a < max; ++a) {
					target = getPath() + "/world" + a + ".zip";
					if(!new File(target).exists()) {
						break;
					}
				}
				ZipUtil.zipFolder(Bukkit.getWorldContainer().getPath() + "/" + world.getName(), target);
				ProPlugin.restartServer();
			}
		}, 20 * 3);
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill cancel");
		World world = Bukkit.getWorlds().get(0);
		Bukkit.unloadWorld(world, false);
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world.getName()));
	}
}
