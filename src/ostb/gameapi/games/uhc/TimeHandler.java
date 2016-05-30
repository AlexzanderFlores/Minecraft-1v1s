package ostb.gameapi.games.uhc;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.util.AsyncDelayedTask;
import ostb.customevents.TimeEvent;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class TimeHandler implements Listener {
	public TimeHandler() {
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
					DB db = DB.PLAYERS_UHC_TIMES;
					if(db.isKeySet("day", "" + day)) {
						String [] keys = new String [] {"day", "hour", "started"};
						String [] values = new String [] {day + "", + calendar.get(Calendar.HOUR_OF_DAY) + "", "0"};
						if(db.isKeySet(keys, values)) {
							db.updateInt("started", 1, keys, values);
							TweetHandler.tweet(Bukkit.getConsoleSender());
						}
					} else {
						for(int a = 0, hour = 12; a < 5; ++a, hour += 2) {
							int teamSize = TeamHandler.getMaxTeamSize();
							String scenarios = "To" + teamSize + " " +  ScenarioManager.getText();
							db.insert("'" + day + "', '" + hour + "', '0', '1000111110', '" + scenarios + "'");
						}
					}
				}
			});
		}
	}
}
