package ostb.server.servers.hub.crate;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Crate {
	private static Beacon voting = null;
	
	public Crate() {
		World world = Bukkit.getWorlds().get(0);
		voting = new Beacon("Voting Crate&8 (&7Click&8)", "voting", world.getBlockAt(1651, 6, -1281), new Vector(0.85, 0.75, 0.5));
	}
	
	public static Beacon getVoting() {
		return voting;
	}
}
