package ostb.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.AsyncDelayedTask;

public class AntiCheatListener {
	public AntiCheatListener() {
		new CommandBase("antiCheat", -1) {
			@Override
			public boolean execute(CommandSender sender, final String [] arguments) {
				String arg = "";
				for(String a : arguments) {
					arg += a + " ";
				}
				Bukkit.getLogger().info("/antiCheat " + arg);
				String action = arguments[0];
				if(action.equalsIgnoreCase("ban")) {
					// /antiCheat ban name reason
					MessageHandler.alert("Banning " + arguments[1] + " for " + arguments[2]);
				} else if(action.equalsIgnoreCase("kick")) {
					// /antiCheat kick name reason
					MessageHandler.alert("Kick " + arguments[1] + " for " + arguments[2]);
				} else if(action.equalsIgnoreCase("NETWORK_DISTANCE_LOGS")) {
					// /antiCheat NETWORK_DISTANCE_LOGS uuid average_distance
					//NOTE: All distances here pass the 8.0 blocks per second speed
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.NETWORK_ATTACK_DISTANCE_LOGS.insert("'" + arguments[1] + "', '" + arguments[2] + "'");
						}
					});
				} else if(action.equalsIgnoreCase("NETWORK_POWER_BOW_LOGS")) {
					// /antiCheat NETWORK_DISTANCE_LOGS uuid times_shot
					//NOTE: All times shot here are fully pulled back bow shots more than 10 seconds
					//NOTE: Sometimes client-side FPS lag can mess this up
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.NETWORK_POWER_BOW_LOGS.insert("'" + arguments[1] + "', '" + arguments[2] + "'");
						}
					});
				} else if(action.equalsIgnoreCase("NETWORK_CPS_LOGS")) {
					// /antiCheat NETWORK_DISTANCE_LOGS uuid times_clicked_fast
					//NOTE: All times here are 20 or more clicks per second
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.NETWORK_CPS_LOGS.insert("'" + arguments[1] + "', '" + arguments[2] + "'");
						}
					});
				} else if(action.equalsIgnoreCase("NETWORK_ATTACK_DISTANCE_LOGS")) {
					// /antiCheat NETWORK_DISTANCE_LOGS uuid average_distance_between_players
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.NETWORK_ATTACK_DISTANCE_LOGS.insert("'" + arguments[1] + "', '" + arguments[2] + "'");
						}
					});
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
}
