package ostb.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PostPlayerJoinEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.games.uhc.events.PlayerTimeOutEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.util.EventUtil;

public class DisconnectHandler implements Listener {
    private static Map<UUID, Integer> times = null;
    private static List<String> cannotRelog = null;

    public DisconnectHandler() {
        times = new HashMap<UUID, Integer>();
        cannotRelog = new ArrayList<String>();
        EventUtil.register(this);
    }

    public static boolean isDisconnected(Player player) {
        return times.containsKey(player.getUniqueId());
    }

    public static boolean cannotRelog(Player player) {
        return cannotRelog.contains(player.getName());
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            if(!HostedEvent.isEvent()) {
                Iterator<UUID> iterator = times.keySet().iterator();
                while(iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    times.put(uuid, times.get(uuid) + 1);
                    if(times.get(uuid) >= (60 * 5)) {
                        Bukkit.getPluginManager().callEvent(new PlayerTimeOutEvent(uuid));
                        iterator.remove();
                        String name = AccountHandler.getName(uuid);
                        cannotRelog.remove(name);
                        WhitelistHandler.unWhitelist(uuid);
                        MessageHandler.alert(name + " &ctook too long to come back!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPostPlayerJoin(PostPlayerJoinEvent event) {
        times.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onGameDeath(GameDeathEvent event) {
        cannotRelog.add(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
        if(times.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        Player player = event.getPlayer();
        if(OSTB.getMiniGame().getGameState() == GameStates.STARTED && !SpectatorHandler.contains(player) && !cannotRelog.contains(player.getName())) {
            times.put(player.getUniqueId(), 0);
        }
    }
}
