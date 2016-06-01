package ostb.gameapi.games.uhc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.gameapi.scenarios.Scenario;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.CommandDispatcher;
import ostb.server.DB;
import ostb.server.Tweeter;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;

public class TweetHandler implements Listener {
    private static int opensIn = 10;
    private static CountDownUtil countDown = null;
    private static boolean hasTweeted = false;
    private static String tweet = null;
    private static String scenarios = null;
    private static long id = 0;

    public TweetHandler() {
    	tweet = "";
        new CommandBase("endTweet", 2, -1, true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if(!HostedEvent.isEvent() && Ranks.OWNER.hasRank(player)) {
                    String url = arguments[0];
                    if(FileHandler.isImage(url)) {
                        String message = "Congrats to ";
                        if(arguments.length > 2) {
                            for(int a = 1; a < arguments.length; ++a) {
                                if(a == arguments.length - 1) {
                                    message = message.substring(0, message.length() - 2);
                                    message += " & " + arguments[a];
                                } else {
                                    message += arguments[a] + ", ";
                                }
                            }
                        } else {
                            message += arguments[1];
                        }
                        message += " for winning this UHC!";
                        if(!message.equals(tweet)) {
                            MessageHandler.sendLine(player);
                            MessageHandler.sendMessage(player, "");
                            MessageHandler.sendMessage(player, "Does this tweet look correct? If so run this command again");
                            MessageHandler.sendMessage(player, "");
                            MessageHandler.sendMessage(player, message);
                            MessageHandler.sendMessage(player, "");
                            MessageHandler.sendLine(player);
                            tweet = message;
                        } else {
                            String path = Bukkit.getWorldContainer().getPath() + "/../resources/winner.png";
                            FileHandler.delete(new File(path));
                            FileHandler.downloadImage(url, path);
                            long id = Tweeter.tweet(tweet, path);
                            if(id == -1) {
                                MessageHandler.sendMessage(player, "&cFailed to send tweet. Possible duplicate tweet.");
                            } else {
                                MessageHandler.sendLine(player);
                                MessageHandler.sendMessage(player, "");
                                MessageHandler.sendMessage(player, "&6To restart the server run &b/uhcOver");
                                MessageHandler.sendMessage(player, "");
                                MessageHandler.sendLine(player);
                                MessageHandler.alert("");
                                MessageHandler.alert("Tweet Sent! &ehttps://twitter.com/" + UHC.getAcount() + "/status/" + id);
                                MessageHandler.alert("");
                            }
                            new CommandBase("uhcOver", true) {
                                @Override
                                public boolean execute(CommandSender sender, String[] arguments) {
                                    Player player = (Player) sender;
                                    if(Ranks.OWNER.hasRank(player)) {
                                        ProPlugin.restartServer();
                                    } else {
                                        MessageHandler.sendUnknownCommand(player);
                                    }
                                    return true;
                                }
                            };
                        }
                    } else {
                        MessageHandler.sendMessage(player, "&cYou cannot use this URL! Please right click the image and click \"Copy Image Location\" then paste that URL");
                    }
                } else {
                    MessageHandler.sendUnknownCommand(player);
                }
                return true;
            }
        }.enableDelay(5);
        EventUtil.register(this);
    }

    public static void tweet(CommandSender sender, int gameID) {
    	if(HostedEvent.isEvent()) {
            MessageHandler.alert("Game will open in &c" + opensIn + " &xminutes");
            countDown = new CountDownUtil(60 * opensIn);
        } else if(hasTweeted) {
            MessageHandler.sendMessage(sender, "&cThis game has already been tweeted");
        } else {
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
            }
        }
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
            Bukkit.getLogger().info("Adding rush");
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
        if(ticks == 20 && countDown != null && countDown.getCounter() > 0) {
        	if(countDown.canDisplay()) {
                MessageHandler.alert("Game opening in " + countDown.getCounterAsString());
            }
            int counter = countDown.getCounter();
            if(!HostedEvent.isEvent() && (counter == 10 * 60 || counter == 5 * 60 || counter == 60)) {
            	CommandDispatcher.sendToGame("UHC", "say &a&lUHC: &eA game is launching soon, run command &b/uhc");
            }
            countDown.decrementCounter();
            if(countDown.getCounter() <= 0) {
                WhitelistHandler.unWhitelist();
            }
        }
    }
}
