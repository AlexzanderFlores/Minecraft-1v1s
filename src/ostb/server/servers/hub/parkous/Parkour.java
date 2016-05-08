package ostb.server.servers.hub.parkous;

import org.bukkit.event.Listener;

import anticheat.util.EventUtil;

public class Parkour implements Listener {
	public Parkour() {
		EventUtil.register(this);
	}
}
