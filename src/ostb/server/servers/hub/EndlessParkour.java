package ostb.server.servers.hub;

import org.bukkit.event.Listener;

import ostb.server.util.EventUtil;

public class EndlessParkour implements Listener {
	private int x = 1588;
	private int y1 = 5;
	private int y2 = 7;
	private int z1 = -1261;
	private int z2 = -1265;
	
	public EndlessParkour() {
		//1588, 7, -1261
		//1588, 5, -1265
		EventUtil.register(this);
	}
}
