package ostb.gameapi.games.uhc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.EventUtil;

public class KillLogger implements Listener {
    private Map<String, Integer> kills = null;

    public KillLogger() {
        kills = new HashMap<String, Integer>();
        EventUtil.register(this);
    }

    /*private static String getText(Player player) {
        String text = AccountHandler.getRank(player).getColor() + player.getName();
        if(text.length() > 16) {
            text = text.substring(0, 16);
        }
        return text;
    }*/

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(kills.containsKey(player.getName())) {
            
        }
    }

    @EventHandler
    public void onGameDeath(GameDeathEvent event) {
        if(kills.isEmpty()) {
            
        }
        Player player = event.getPlayer();
        kills.remove(player.getName());
        Player killer = event.getKiller();
        if(killer == null) {
            String name = "PVE";
            if(kills.containsKey(name)) {
                kills.put(name, kills.get(name) + 1);
            } else {
                kills.put(name, 1);
            }
        } else {
            if(kills.containsKey(killer.getName())) {
                kills.put(killer.getName(), kills.get(killer.getName()) + 1);
            } else {
                kills.put(killer.getName(), 1);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        
    }
}
