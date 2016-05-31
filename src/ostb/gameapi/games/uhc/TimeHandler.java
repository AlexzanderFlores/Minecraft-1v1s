package ostb.gameapi.games.uhc;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.util.AsyncDelayedTask;
import ostb.customevents.TimeEvent;
import ostb.gameapi.scenarios.Scenario;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class TimeHandler implements Listener {
	private static final String defaultOptions = "1000111110";
	
	public TimeHandler() {
		new CommandBase("addUHCGame", 5, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				int day = 0;
				int hour = 0;
				int started = 0;
				try {
					day = Integer.valueOf(arguments[0]);
					hour = Integer.valueOf(arguments[1]);
					started = Integer.valueOf(arguments[2]);
				} catch(NumberFormatException e) {
					return false;
				}
				String options = arguments[3];
				if(options.length() != 10) {
					MessageHandler.sendMessage(sender, "&cInvalid options length. Default example: &b1000111110");
					return true;
				}
				String scenarios = "";
				for(int a = 4; a < arguments.length; ++a) {
					String scenario = arguments[a];
					if(!ScenarioManager.isScenario(scenario)) {
						MessageHandler.sendMessage(sender, "&c\"" + scenario + "\" is not a valid scenario. Valid scenarios are the names on the left below:");
						for(Scenario scenario2 : ScenarioManager.getAllScenarios()) {
							MessageHandler.sendMessage(sender, scenario2.getShortName() + " &7- &x" + scenario2.getName());
						}
						return true;
					}
					scenarios += scenario + " ";
				}
				addGame(day, hour, started, options, scenarios.substring(0, scenarios.length() - 1));
				MessageHandler.sendMessage(sender, "Added game:");
				MessageHandler.sendMessage(sender, "Day: &b" + day);
				MessageHandler.sendMessage(sender, "Hour: &b" + hour);
				MessageHandler.sendMessage(sender, "Started: &b" + started);
				MessageHandler.sendMessage(sender, "Options: &b" + options);
				MessageHandler.sendMessage(sender, "Scenarios: &b" + scenarios);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					//day INT, hour INT, started BOOL, options INT
					//1234456789
					//1 = team size
					//2 = bool for rush
					//3 = bool for nether
					//44 = apple rates, 00 = 100%
					//5 = bool for horses
					//6 = bool for horse healing
					//7 = bool for pearl damage
					//8 = bool for absorption
					//9 = bool for end
					Calendar calendar = Calendar.getInstance();
					int day = calendar.get(Calendar.DAY_OF_YEAR);
					int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
					Bukkit.getLogger().info("Day: " + day + " Hour: " + hourOfDay);
					DB db = DB.PLAYERS_UHC_TIMES;
					if(db.isKeySet("day", "" + day)) {
						String [] keys = new String [] {"day", "hour", "started"};
						String [] values = new String [] {day + "", + hourOfDay + "", "0"};
						if(db.isKeySet(keys, values)) {
							int id = db.getInt(keys, values, "id");
							db.updateInt("started", 1, keys, values);
							TweetHandler.tweet(Bukkit.getConsoleSender(), id);
						}
					} else {
						db.delete("day", (day - 1) + "");
						int teamSize = TeamHandler.getMaxTeamSize();
						String scenarios = "To" + teamSize + " " +  ScenarioManager.getText();
						for(int a = 0, hour = 12; a < 5; ++a, hour += 2) {
							addGame(day, hour, 0, defaultOptions, scenarios);
						}
					}
				}
			});
		}
	}
	
	private int addGame(int day, int hour, int started, String options, String scenarios) {
		DB db = DB.PLAYERS_UHC_TIMES;
		db.insert("'" + day + "', '" + hour + "', '" + started + "', '" + options + "', '" + scenarios + "'");
		String [] keys = new String [] {"day", "hour", "started", "options", "scenarios"};
		String [] values = new String [] {day + "", hour + "", started + "", options, scenarios};
		return db.getInt(keys, values, "id");
	}
}
