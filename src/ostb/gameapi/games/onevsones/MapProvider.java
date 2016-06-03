package ostb.gameapi.games.onevsones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.TimeEvent;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class MapProvider implements Listener {
    public static Map<Integer, List<Integer>> openMaps = null; // <map number> <target Xs>
    private static int numberOfMaps = 0;

    public MapProvider(World world) {
        openMaps = new HashMap<Integer, List<Integer>>();
        Block mapCheckBlock = world.getBlockAt(0, 3, 0);
        do {
            mapCheckBlock = mapCheckBlock.getRelative(100, 0, 0);
            if(mapCheckBlock.getType() != Material.AIR) {
                ++numberOfMaps;
            }
        } while(mapCheckBlock.getType() != Material.AIR);
        EventUtil.register(this);
    }

    public MapProvider(Player playerOne, Player playerTwo, World world, boolean tournament, boolean ranked) {
        int targetX = 0;
        Block targetBlock = world.getBlockAt(targetX, 3, 0);
        int map = new Random().nextInt(numberOfMaps) + 1;
        //Commented code is to look for an already existing map to use.
        if(openMaps.containsKey(map)) {
            List<Integer> maps = openMaps.get(map);
            if(maps != null && !maps.isEmpty()) {
                targetX = maps.get(0);
                maps.remove(0);
                openMaps.put(map, maps);
                targetBlock = world.getBlockAt(targetX, 3, 0);
            }
        }
        if(targetX == 0) {
            int y = 50;
            int x1 = Integer.valueOf((map - 1) + "70");
            int z1 = -35;
            int x2 = Integer.valueOf(map + "32");
            int z2 = 35;
            do {
                targetX -= 100;
                targetBlock = world.getBlockAt(targetX, 3, 0);
            } while(targetBlock.getType() != Material.AIR);
            for(int y1 = 0; y1 < y; ++y1) {
                for(int x = x1; x <= x2; ++x) {
                    for(int z = z1; z <= z2; ++z) {
                        Block block = world.getBlockAt(x, y1, z);
                        if(block.getType() != Material.AIR) {
                            world.getBlockAt(targetX + x - (100 * map), y1, z).setType(block.getType());
                            world.getBlockAt(targetX + x - (100 * map), y1, z).setData(block.getData());
                        }
                    }
                }
            }
        }
        new Battle(map, targetBlock, playerOne, playerTwo, tournament, ranked);
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
