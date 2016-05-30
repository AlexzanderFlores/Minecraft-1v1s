package ostb.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.games.uhc.HostHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class DiamondTracker implements Listener {
    private Map<String, Integer> mined = null;
    private List<String> delayed = null;

    public DiamondTracker() {
        mined = new HashMap<String, Integer>();
        delayed = new ArrayList<String>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() == Material.DIAMOND_ORE) {
            Player player = event.getPlayer();
            int times = 0;
            if(mined.containsKey(player.getName())) {
                times = mined.get(player.getName());
            }
            if(++times >= 10 && !delayed.contains(player.getName())) {
                final String name = player.getName();
                delayed.add(name);
                new DelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        delayed.remove(name);
                    }
                }, 20 * 5);
                for(Player online : Bukkit.getOnlinePlayers()) {
                    if(HostHandler.isHost(online.getUniqueId())) {
                        MessageHandler.sendMessage(online, AccountHandler.getPrefix(player) + " &cHAS MINED &e&l" + times + " &cDIAMONDS WITHIN THE LAST 5 MINUTES");
                    }
                }
            }
            mined.put(player.getName(), times);
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20 * 60 * 5) {
            mined.clear();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        mined.remove(event.getPlayer().getName());
    }
}
