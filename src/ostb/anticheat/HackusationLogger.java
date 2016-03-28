package ostb.anticheat;

import org.bukkit.event.Listener;

import ostb.server.util.EventUtil;

public class HackusationLogger implements Listener {
	//private Map<String, Integer> logs = null;
	
	public HackusationLogger() {
		//logs = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	
}
