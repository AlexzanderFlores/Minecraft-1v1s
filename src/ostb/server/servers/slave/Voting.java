package ostb.server.servers.slave;

import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.VotifierEvent;

import ostb.player.account.AccountHandler;
import ostb.server.DB;
import ostb.server.servers.hub.crate.Beacon;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class Voting implements Listener {
	public Voting() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onVotifier(VotifierEvent event) {
		execute(event.getVote().getUsername());
	}
	
	public static void execute(final String name) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID playerUUID = AccountHandler.getUUID(name);
				if(playerUUID != null) {
					String uuid = playerUUID.toString();
					if(DB.PLAYERS_LIFETIME_VOTES.isUUIDSet(playerUUID)) {
						int amount = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "amount") + 1;
						DB.PLAYERS_LIFETIME_VOTES.updateInt("amount", amount, "uuid", uuid);
					} else {
						DB.PLAYERS_LIFETIME_VOTES.insert("'" + uuid + "', '1'");
					}
					Bukkit.getLogger().info("voting: update lifetime votes");
					Calendar calendar = Calendar.getInstance();
					String month = calendar.get(Calendar.MONTH) + "";
					String [] keys = new String [] {"uuid", "month"};
					String [] values = new String [] {uuid, month};
					if(DB.PLAYERS_MONTHLY_VOTES.isKeySet(keys, values)) {
						int amount = DB.PLAYERS_MONTHLY_VOTES.getInt(keys, values, "amount") + 1;
						DB.PLAYERS_MONTHLY_VOTES.updateInt("amount", amount, keys, values);
					} else {
						DB.PLAYERS_MONTHLY_VOTES.insert("'" + uuid + "', '1', '" + month + "'");
					}
					Bukkit.getLogger().info("voting: update monthly votes");
					String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
					keys[1] = "week";
					values[1] = week;
					if(DB.PLAYERS_WEEKLY_VOTES.isKeySet(keys, values)) {
						int amount = DB.PLAYERS_WEEKLY_VOTES.getInt(keys, values, "amount") + 1;
						DB.PLAYERS_WEEKLY_VOTES.updateInt("amount", amount, keys, values);
					} else {
						DB.PLAYERS_WEEKLY_VOTES.insert("'" + uuid + "', '1', '" + week + "'");
					}
					Bukkit.getLogger().info("voting: update weekly votes");
					if(DB.PLAYERS_VOTE_PASSES.isUUIDSet(playerUUID)) {
						int amount = DB.PLAYERS_VOTE_PASSES.getInt("uuid", uuid, "amount") + 1;
						DB.PLAYERS_VOTE_PASSES.updateInt("amount", amount, "uuid", uuid);
					} else {
						DB.PLAYERS_VOTE_PASSES.insert("'" + uuid + "', '1'");
					}
					Beacon.giveKey(playerUUID, 1);
				}
			}
		});
	}
}
