package ostb.server.servers.slave;

import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.VotifierEvent;

import ostb.player.account.AccountHandler;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.servers.hub.crate.Beacon;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class Voting implements Listener {
	public Voting() {
		new CommandBase("test", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Voting.execute(arguments[0]);
				return true;
			}
		};
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
					int streak = 1;
					int multiplier = 1;
					int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
					if(DB.PLAYERS_LIFETIME_VOTES.isUUIDSet(playerUUID)) {
						int amount = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "amount") + 1;
						int day = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "day");
						if(day == currentDay - 1) {
							streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "streak") + 1;
							if(streak > DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "highest_streak")) {
								DB.PLAYERS_LIFETIME_VOTES.updateInt("highest_streak", streak, "uuid", uuid);
							}
						} else {
							streak = 1;
						}
						multiplier = streak <= 5 ? 1 : streak <= 10 ? 2 : streak <= 15 ? 3 : streak <= 20 ? 4 : streak <= 25 ? 5 : 6;
						DB.PLAYERS_LIFETIME_VOTES.updateInt("amount", amount, "uuid", uuid);
						DB.PLAYERS_LIFETIME_VOTES.updateInt("day", currentDay, "uuid", uuid);
						DB.PLAYERS_LIFETIME_VOTES.updateInt("streak", streak, "uuid", uuid);
					} else {
						DB.PLAYERS_LIFETIME_VOTES.insert("'" + uuid + "', '1', '" + currentDay + "', '1', '1'");
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
					Beacon.giveKey(playerUUID, 1 * multiplier, "voting");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "hubAlert &e" + name + " has voted for advantages. Run command &a/vote");
				}
			}
		});
	}
}
