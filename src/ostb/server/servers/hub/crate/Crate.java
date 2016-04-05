package ostb.server.servers.hub.crate;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;

public class Crate {
	private static Beacon voting = null;
	
	public Crate() {
		World world = Bukkit.getWorlds().get(0);
		voting = new Beacon("Voting Crate&8 (&7Click&8)", "voting", world.getBlockAt(1651, 6, -1281), new Vector(0.85, 0.75, 0.5));
		new CommandBase("giveKey", 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				Beacon.giveKey(uuid, Integer.valueOf(arguments[1]), arguments[2]);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	public static Beacon getVoting() {
		return voting;
	}
}
