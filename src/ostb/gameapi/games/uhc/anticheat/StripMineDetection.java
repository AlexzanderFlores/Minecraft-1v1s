package ostb.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.games.uhc.HostHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class StripMineDetection implements Listener {
    private Map<String, Vector> levelMined = null;
    private Map<String, Integer> counters = null;
    private List<String> delayed = null;

    public StripMineDetection() {
        levelMined = new HashMap<String, Vector>();
        counters = new HashMap<String, Integer>();
        delayed = new ArrayList<String>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(!event.isCancelled()) {
            Block block = event.getBlock();
            Vector vector = block.getLocation().toVector();
            Player player = event.getPlayer();
            if(vector.getBlockY() <= 25) {
                if(levelMined.containsKey(player.getName())) {
                    Vector previous = levelMined.get(player.getName());
                    int x = vector.getBlockX();
                    int prevX = previous.getBlockX();
                    int z = vector.getBlockZ();
                    int prevZ = previous.getBlockZ();
                    if((x == prevX && z != prevZ) || (z == prevZ && x != prevX)) {
                        int prevY = previous.getBlockY();
                        int y = vector.getBlockY();
                        if(y == prevY || y == prevY - 1 || y == prevY + 1) {
                            int counter = 0;
                            if(counters.containsKey(player.getName())) {
                                counter = counters.get(player.getName());
                            }
                            if(++counter >= 10 && !delayed.contains(player.getName())) {
                                final String name = player.getName();
                                delayed.add(name);
                                new DelayedTask(new Runnable() {
                                    @Override
                                    public void run() {
                                        delayed.remove(name);
                                    }
                                }, 20 * 10);
                                for(Player online : Bukkit.getOnlinePlayers()) {
                                    if(HostHandler.isHost(online.getUniqueId())) {
                                        MessageHandler.sendMessage(online, AccountHandler.getPrefix(player) + " &cIS POSSIBLY STRIP MINING");
                                    }
                                }
                            }
                            counters.put(player.getName(), counter);
                        } else {
                            counters.remove(player.getName());
                        }
                    } else {
                        counters.remove(player.getName());
                    }
                    levelMined.put(player.getName(), vector);
                } else {
                    counters.remove(player.getName());
                    levelMined.put(player.getName(), vector);
                }
            } else {
                levelMined.remove(player.getName());
                counters.remove(player.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        Player player = event.getPlayer();
        levelMined.remove(player.getName());
        counters.remove(player.getName());
    }
}
