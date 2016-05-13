package ostb.server.servers.slave;

import ostb.ProPlugin;

public class Slave extends ProPlugin {
	public Slave() {
		super("Slave");
		new Voting();
	}
}