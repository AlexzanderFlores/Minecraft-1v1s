package network.server.servers.slave;

import network.ProPlugin;
import npc.util.DelayedTask;

public class Slave extends ProPlugin {
	public Slave() {
		super("Slave");
		new Voting();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				new Server();
			}
		}, 20 * 3);
	}
}