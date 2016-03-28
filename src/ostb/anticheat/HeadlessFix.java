package ostb.anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ostb.anticheat.killaura.KillAuraSpectatorCheck;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.EventUtil;


public class HeadlessFix extends AntiCheat implements Listener {
	private Map<String, Integer> counters = null;
	
	public HeadlessFix() {
		super("Headless/Derp");
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled() && event.getTo().getPitch() == 180.0f || event.getTo().getPitch() == -180.0f) {
			if(!KillAuraSpectatorCheck.isWatching(event.getPlayer())) {
				int counter = 0;
				if(counters.containsKey(event.getPlayer().getName())) {
					counter = counters.get(event.getPlayer().getName());
				}
				if(++counter >= 20) {
					ban(event.getPlayer());
				} else {
					counters.put(event.getPlayer().getName(), counter);
				}
			} else {
				counters.remove(event.getPlayer().getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			counters.remove(event.getPlayer().getName());
		}
	}
}
