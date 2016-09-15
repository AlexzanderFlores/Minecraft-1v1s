package ostb.server.servers.hub.main;

import ostb.Network;
import ostb.server.servers.hub.HubBase;

public class MainHub extends HubBase {
	public MainHub() {
		super("MainHub");
		addGroup("mainhub");
		new MainHubTop5();
	}
	
	public static int getHubNumber() {
		return Integer.valueOf(Network.getServerName().toLowerCase().replace("hub", ""));
	}
}
