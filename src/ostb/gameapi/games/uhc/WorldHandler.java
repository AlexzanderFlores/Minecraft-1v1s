package ostb.gameapi.games.uhc;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.wimbli.WorldBorder.Events.WorldBorderFillFinishedEvent;

import ostb.OSTB;
import ostb.player.MessageHandler;
import ostb.server.BiomeSwap;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;

public class WorldHandler implements Listener {
    private static World world = null;
    private static World nether = null;
    private static World end = null;
    private static boolean preGenerated = false;
    private static int radius = 1500;

    public WorldHandler() {
        BiomeSwap.setUpUHC();
        generateWorld();
        EventUtil.register(this);
    }

    public static void generateWorld() {
        MessageHandler.alert("Generating World...");
        if(world != null) {
            MessageHandler.alert("Deleting old World...");
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.getWorld().getName().equals(world.getName())) {
                    player.teleport(OSTB.getMiniGame().getLobby().getSpawnLocation());
                }
            }
            Bukkit.unloadWorld(world, false);
            FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world.getName()));
            MessageHandler.alert("Deleting old World... Complete!");
        }
        world = Bukkit.createWorld(new WorldCreator("world"));
        world.setSpawnLocation(0, getGround(new Location(world, 0, 0, 0)).getBlockY(), 0);
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setDifficulty(Difficulty.HARD);
        world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setDamageAmount(1.0d);
		world.getWorldBorder().setWarningDistance(25);
		world.getWorldBorder().setWarningTime(20 * 10);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + world.getName() + " set " + radius + " " + radius + " 0 0");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + world.getName() + " fill 40");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm");
		setBorder();
        OSTB.getMiniGame().setMap(world);
        MessageHandler.alert("Generating World... Complete!");
    }

    public static void generateNether() {
        MessageHandler.alert("Generating Nether...");
        if(nether != null) {
            MessageHandler.alert("Deleting old Nether...");
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.getWorld().getName().equals(nether.getName())) {
                    player.teleport(OSTB.getMiniGame().getLobby().getSpawnLocation());
                }
            }
            Bukkit.unloadWorld(nether, false);
            FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + nether.getName()));
            MessageHandler.alert("Deleting old Nether... Complete!");
        }
        WorldCreator worldCreator = new WorldCreator("world_nether");
        worldCreator.environment(Environment.NETHER);
        nether = Bukkit.createWorld(worldCreator);
        nether.setGameRuleValue("naturalRegeneration", "false");
        nether.setDifficulty(Difficulty.HARD);
        MessageHandler.alert("Generating Nether... Complete!");
    }

    public static void generateEnd() {
        MessageHandler.alert("Generating End...");
        if(end != null) {
            MessageHandler.alert("Deleting old End...");
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.getWorld().getName().equals(end.getName())) {
                    player.teleport(OSTB.getMiniGame().getLobby().getSpawnLocation());
                }
            }
            Bukkit.unloadWorld(end, false);
            FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + end.getName()));
            MessageHandler.alert("Deleting old End... Complete!");
        }
        WorldCreator worldCreator = new WorldCreator("world_end");
        worldCreator.environment(Environment.THE_END);
        end = Bukkit.createWorld(worldCreator);
        end.setGameRuleValue("naturalRegeneration", "false");
        end.setDifficulty(Difficulty.HARD);
        MessageHandler.alert("Generating End... Complete!");
    }

    public static boolean isPreGenerated() {
        return preGenerated;
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

    public static World getNether() {
        return nether;
    }

    public static World getEnd() {
        return end;
    }
    
    private static void setBorder() {
    	try {
    		world.getWorldBorder().setSize(radius);
    	} catch(Exception e) {
    		
    	}
    }
    
    @EventHandler
	public void onWorldBorderFinish(WorldBorderFillFinishedEvent event) {
    	preGenerated = true;
    	File file = new File(Bukkit.getWorldContainer().getPath() + "/" + getWorld().getName() + "/pregen.yml");
    	if(!file.exists()) {
    		try {
    			file.createNewFile();
    		} catch(IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
}
