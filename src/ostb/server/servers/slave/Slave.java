package ostb.server.servers.slave;

import ostb.ProPlugin;
import ostb.server.networking.Server;

public class Slave extends ProPlugin {
	private static Server server = null;
	
	public Slave() {
		super("Slave");
		server = new Server(4500);
		server.start();
		new Voting();
		new PlayerLogger();
	}
	
	public static Server getServer() {
		return server;
	}
}