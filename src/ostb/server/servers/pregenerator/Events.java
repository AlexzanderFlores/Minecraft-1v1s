package ostb.server.servers.pregenerator;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.TimeEvent;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	private boolean running = false;
	
	public Events() {
		EventUtil.register(this);
	}
	
	private int getWorlds() {
		return new File(OSTB.getInstance().getDataFolder().getPath() + "../resources/maps/pregen/").listFiles().length;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(!running && ticks == 20 * 30 && getWorlds() < 20) {
			running = true;
			File worldFile = new File(Bukkit.getWorldContainer().getPath() + "/world");
			if(worldFile.exists()) {
				
			}
		}
	}
}
