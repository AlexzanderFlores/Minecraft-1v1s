package ostb.gameapi.games.hosteduhc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.player.MessageHandler;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EventUtil;

public class SurfaceHandler implements Listener {
    private CountDownUtil countDown = null;

    public SurfaceHandler() {
        if(!HostedEvent.isEvent()) {
            countDown = new CountDownUtil(60 * 2);
            EventUtil.register(this);
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            if(countDown.canDisplay()) {
                MessageHandler.alert("Teleporting players to surface in " + countDown.getCounterAsString());
            }
            countDown.decrementCounter();
            if(countDown.getCounter() <= 0) {
                TimeEvent.getHandlerList().unregister(this);
                for(Player player : ProPlugin.getPlayers()) {
                    if(player.getWorld().getName().equals(WorldHandler.getWorld().getName()) && player.getLocation().getY() <= 55) {
                        player.teleport(WorldHandler.getGround(player.getLocation()));
                    }
                }
            }
        }
    }
}
