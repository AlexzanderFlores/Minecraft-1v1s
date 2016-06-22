package ostb.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.TimeEvent;
import ostb.server.util.EventUtil;

public class RankAds implements Listener {
	public RankAds() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60 * 2) {
			
		}
	}
}
