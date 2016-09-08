package ostb.server.servers.hub.main;

import npc.NPCEntity;
import ostb.OSTB;
import ostb.server.servers.hub.HubBase;

public class MainHub extends HubBase {
	private NPCEntity npc = null;
	
	public MainHub() {
		super("MainHub");
		addGroup("mainhub");
		new MainHubTop5();
	}
	
	public static int getHubNumber() {
		return Integer.valueOf(OSTB.getServerName().toLowerCase().replace("hub", ""));
	}
}
