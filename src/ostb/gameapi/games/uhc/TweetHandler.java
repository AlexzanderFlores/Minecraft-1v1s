package ostb.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.util.DelayedTask;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.uhc.scenarios.Scenario;
import ostb.player.MessageHandler;
import ostb.server.CommandDispatcher;
import ostb.server.DB;
import ostb.server.Tweeter;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EventUtil;

public class TweetHandler implements Listener {
    private static int opensIn = 10;
    private static CountDownUtil countDown = null;
    private static boolean hasTweeted = false;
    private static boolean hasTweetedEnd = false;
    private static String scenarios = null;
    private static long id = 0;

    public TweetHandler() {
        EventUtil.register(this);
    }

    public static boolean tweet(CommandSender sender, int gameID) {
    	if(!WorldHandler.isPreGenerated()) {
    		return false;
    	}
    	if(HostedEvent.isEvent()) {
            MessageHandler.alert("Game will open in &c" + opensIn + " &xminutes");
            countDown = new CountDownUtil(60 * opensIn);
        } else if(!hasTweeted) {
            String message = getScenarios(gameID) + "\n\n" + getIP() + " > " + getCommand() + "\n\n" + getOpensIn();
            id = Tweeter.tweet(message, "uhc.jpg");
            if(id == -1) {
                MessageHandler.sendMessage(sender, "&cFailed to send Tweet! Possible duplicate tweet");
            } else {
                hasTweeted = true;
                MessageHandler.alert("Tweet sent! Game will open in &c" + opensIn + " &xminutes");
                countDown = new CountDownUtil(60 * opensIn);
                new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						int server = Integer.valueOf(OSTB.getServerName().replace("UHC", ""));
						String url = getURL();
						if(DB.NETWORK_UHC_URL.isKeySet("server", "" + server)) {
							DB.NETWORK_UHC_URL.updateString("url", url, "server", "" + server);
						} else {
							DB.NETWORK_UHC_URL.insert("'" + server + "', '" + url + "'");
						}
					}
				});
                return true;
            }
        }
    	return false;
    }

    private static void endTweet() {
    	if(hasTweetedEnd) {
    		return;
    	}
    	hasTweetedEnd = true;
    	List<Player> players = ProPlugin.getPlayers();
    	String message = "Congrats to ";
    	String player = "";
    	int kills = 0;
    	if(players.size() == 1) {
    		player = players.get(0).getName();
    		message += player;
    		kills = KillLogger.getKills(players.get(0));
    	} else {
    		for(int a = 0; a < players.size(); ++a) {
    			if(a == players.size() - 1) {
    				message = message.substring(0, message.length() - 2);
    				message += " & " + players.get(a).getName();
    			} else {
    				player = players.get(a).getName();
    				message += player + ", ";
    			}
    			kills += KillLogger.getKills(players.get(a));
    		}
    	}
        message += " for winning this UHC with " + kills + " kills!";
        long id = Tweeter.tweet(message, TweetHandler.id);
        if(id == -1) {
            Bukkit.getLogger().info("Failed to send tweet. Possible duplicate tweet.");
        } else {
            MessageHandler.alert("");
            MessageHandler.alert("Tweet Sent! &ehttps://twitter.com/" + UHC.getAcount() + "/status/" + id);
            MessageHandler.alert("Server restarting in &c10 &xseconds");
            MessageHandler.alert("");
        }
        new DelayedTask(new Runnable() {
			@Override
			public void run() {
				ProPlugin.restartServer();
			}
		}, 20 * 10);
    }

    public static String getURL() {
    	return "https://twitter.com/" + UHC.getAcount() + "/status/" + id;
    }

    public static String getScenarios() {
    	return scenarios;
    }

    public static String getScenarios(int id) {
    	DB db = DB.NETWORK_UHC_TIMES;
    	OptionsHandler.applyOptions(db.getString("id", "" + id, "options"));
    	String scenarios = db.getString("id", "" + id, "scenarios");
    	scenarios = scenarios.replace("Rush", "");
    	String [] split = scenarios.split(" ");
    	List<String> list = new ArrayList<String>();
    	for(String string : split) {
    		list.add(string);
    	}
    	for(Scenario scenario : ScenarioManager.getAllScenarios()) {
    		if(list.contains(scenario.getShortName())) {
    			scenario.enable(false);
    			scenarios = scenarios.replace(scenario.getShortName(), scenario.getName());
    		} else {
    			scenario.disable(false);
    		}
    	}
    	list.clear();
    	list = null;
        if(OptionsHandler.isRush()) {
            scenarios += " Rush";
        }
        int teamSize = TeamHandler.getMaxTeamSize();
        String size = teamSize == 1 ? "FFA" : "To" + teamSize;
        scenarios = size + " " + scenarios;
        TweetHandler.scenarios = scenarios;
        return scenarios;
    }

    private static String getIP() {
        return "IP OutsideTheBlock.org";
    }

    private static String getCommand() {
        return "/join " + OSTB.getServerName();
    }

    private static String getOpensIn() {
        return "Opens in " + opensIn + " minute" + (opensIn == 1 ? "" : "s");
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
        	if(countDown != null && countDown.getCounter() > 0) {
        		if(countDown.canDisplay()) {
                    MessageHandler.alert("Game opening in " + countDown.getCounterAsString());
                }
                int counter = countDown.getCounter();
                if(!HostedEvent.isEvent() && (counter == 10 * 60 || counter == 5 * 60)) {
                	CommandDispatcher.sendToGame("UHC", "say &a&lUHC: &eA game is launching soon, run command &b/uhc");
                }
                countDown.decrementCounter();
                if(countDown.getCounter() <= 0) {
                	CommandDispatcher.sendToGame("UHC", "say &a&lUHC: &eA game has launched! Run command &b/uhc");
                    WhitelistHandler.unWhitelist();
                }
        	} else if(OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
        		int playing = ProPlugin.getPlayers().size();
        		int teamSize = TeamHandler.getMaxTeamSize();
        		if(playing <= teamSize || TeamHandler.getTeams().size() == 1) {
        			endTweet();
        		} else if(ScenarioManager.getScenario("TrueLove").isEnabled()) {
        			if(playing <= 2) {
        				
        			} else if(playing == 1) {
        				endTweet();
        			}
        		}
        	}
        }
    }
}
