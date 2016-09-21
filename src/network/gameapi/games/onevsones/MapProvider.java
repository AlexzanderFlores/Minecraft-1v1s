package network.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.server.util.EventUtil;

public class MapProvider implements Listener {
    public static Map<Integer, List<Location>> openMaps = null; // <map number> <target Xs>
    private static int numberOfMaps = 0;

    public MapProvider(World world) {
        openMaps = new HashMap<Integer, List<Location>>();
        Block mapCheckBlock = null;
        int counter = 0;
        int z = 0;
        do {
        	mapCheckBlock = world.getBlockAt(382, 4, z);
        	do {
                mapCheckBlock = mapCheckBlock.getRelative(118, 0, 0);
                if(mapCheckBlock.getType() != Material.AIR) {
                	List<Location> locations = openMaps.get(counter);
                	if(locations == null) {
                		locations = new ArrayList<Location>();
                	}
                	locations.add(new Location(world, mapCheckBlock.getX(), 4, z));
                	openMaps.put(counter, locations);
                    numberOfMaps++;
                }
            } while(mapCheckBlock.getType() != Material.AIR);
        	++counter;
        	z += 100;
        	mapCheckBlock = world.getBlockAt(500, 4, z);
        } while(mapCheckBlock.getType() != Material.AIR);
        Bukkit.getLogger().info("Maps found: " + numberOfMaps);
        EventUtil.register(this);
    }

    public MapProvider(Player playerOne, Player playerTwo, World world, boolean tournament, boolean ranked) {
    	int error = -1;
        int map = new Random().nextInt(openMaps.keySet().size());
        Location location = null;
        if(openMaps.containsKey(map)) {
        	List<Location> locations = openMaps.get(map);
        	if(locations != null && !locations.isEmpty()) {
        		location = locations.get(0);
        		locations.remove(0);
        		openMaps.put(map, locations);
                new Battle(map, location, playerOne, playerTwo, tournament, ranked);
        	} else {
        		error = 1;
        	}
        } else {
        	error = 2;
        }
        if(error > 0) {
        	String loc = location == null ? "null" : location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
        	String message = "&cThere was an error with map provider, please report this (" + loc + ", " + map + ", " + error + ")";
        	MessageHandler.sendMessage(playerOne, message);
        	MessageHandler.sendMessage(playerTwo, message);
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            for(Battle battle : BattleHandler.getBattles()) {
                battle.incrementTimer();
                if(battle.getTimer() == 5) {
                    battle.start();
                }
            }
        }
    }
}
