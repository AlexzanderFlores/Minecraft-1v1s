package ostb.gameapi.games.uhc;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.MiniGame;
import ostb.gameapi.games.uhc.anticheat.AntiIPVP;
import ostb.gameapi.uhc.GoldenHeadUtil;
import ostb.gameapi.uhc.SkullPikeUtil;
import ostb.gameapi.uhc.scenarios.Scenario;
import ostb.gameapi.uhc.scenarios.scenarios.AppleRates;
import ostb.player.MessageHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.ChatClickHandler;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.Tweeter;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.CountDownUtil;
import ostb.server.util.FileHandler;

public class UHC extends MiniGame {
    private static final String account = "OSTBUHC";
    private static String oldScenarios = "";
    private static int oldRadius = -1;

    public UHC() {
        super("UHC");
        setRequiredPlayers(30);
        setVotingCounter(-1);
        setStartingCounter(60 * 3 + 30);
        setEndingCounter(20);
        setAutoJoin(false);
        setCanJoinWhileStarting(true);
        setResetPlayerUponJoining(true);
        setRestartWithOnePlayerLeft(false);
        setDoDaylightCycle(true);
        setUseCoinBoosters(false);
        new Events();
        new WhitelistHandler();
        new ScenarioManager();
        new TeamHandler();
        new OptionsHandler();
        new WorldHandler();
        new AntiIPVP();
        new HealthHandler();
        new BelowNameHealthScoreboardUtil();
        new QuestionAnswerer();
        new HostedEvent();
        new GoldenHeadUtil();
        new SkullPikeUtil();
        new TimeHandler();
        new TweetHandler();
        new KillLogger();
        //UHC:
        ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/../twitter.yml");
        String consumerKey = config.getConfig().getString("uhc.consumerkey");
    	String consumerSecret = config.getConfig().getString("uhc.consumersecret");
    	String accessToken = config.getConfig().getString("uhc.accesstoken");
    	String accessSecret = config.getConfig().getString("uhc.accesssecret");
        new Tweeter(consumerKey, consumerSecret, accessToken, accessSecret);
        new CommandBase("info") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
            	int teamSize = TeamHandler.getMaxTeamSize();
                String scenarios = teamSize == 1 ? "FFA " : "To" + TeamHandler.getMaxTeamSize() + " ";
                for(Scenario scenario : ScenarioManager.getActiveScenarios()) {
                    scenarios += scenario.getName() + ", ";
                }
                scenarios = scenarios.substring(0, scenarios.length() - 2);
                if(OptionsHandler.isRush()) {
                	scenarios += " Rush";
                }
                MessageHandler.sendMessage(sender, "");
                if(sender instanceof Player) {
                    Player player = (Player) sender;
                    ChatClickHandler.sendMessageToRunCommand(player, " &eClick for Info", "Click for info", "/sinfo", "&eScenarios: &b" + scenarios);
                } else {
                	MessageHandler.sendMessage(sender, "Scenario: &b" + scenarios);
                }
                if(teamSize > 1) {
                	MessageHandler.sendMessage(sender, "Team damage: &bFalse");
                }
                MessageHandler.sendMessage(sender, "Nether: " + (OptionsHandler.isNetherEnabled() ? "&bON" : "&cOFF") + " &xEnd: " + (OptionsHandler.isEndEnabled() ? "&bON" : "&cOFF"));
                MessageHandler.sendMessage(sender, "Apple rates: &b" + AppleRates.getRates() + "%");
                MessageHandler.sendMessage(sender, "Horses: " + (OptionsHandler.getAllowHorses() ? "&bON" : "&cOFF") + " &7(&eHorse Healing is " + (OptionsHandler.getAllowHorseHealing() ? "&bON" : "&cOFF") + "&7)");
                MessageHandler.sendMessage(sender, "Ender pearl damage: " + (OptionsHandler.getAllowPearlDamage() ? "&bON" : "&cOFF"));
                MessageHandler.sendMessage(sender, "Absorption: " + (OptionsHandler.getAllowAbsorption() ? "&bON" : "&cOFF"));
                return true;
            }
        };
        new CommandBase("sinfo") {
            @Override
            public boolean execute(CommandSender sender, String [] arguments) {
            	List<Scenario> scenarios = ScenarioManager.getActiveScenarios();
                for(int a = 0; a < scenarios.size(); ++a) {
                    if(a == 0) {
                    	MessageHandler.sendMessage(sender, "");
                    }
                    Scenario scenario = scenarios.get(a);
                    String info = scenario.getInfo();
                    if(info == null) {
                        MessageHandler.sendMessage(sender, "&cNo info to display at this time for &b" + scenario.getName());
                    } else {
                        MessageHandler.sendMessage(sender, "&e&l" + scenario.getName() + " &b" + info);
                    }
                    MessageHandler.sendMessage(sender, "");
                }
                if(sender instanceof Player) {
                	Player player = (Player) sender;
                	ChatClickHandler.sendMessageToRunCommand(player,  "&bClick Here", "Click for Info", "/info", "&eMore game information: &a/info &eor ");
                } else {
                	MessageHandler.sendMessage(sender, "More game information: &a/info");
                }
                return true;
            }
        };
        OSTB.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
			@Override
			public void update() {
				int r = ((int) getMap().getWorldBorder().getSize()) / 2;
				if(oldRadius != r) {
					oldRadius = r;
					OSTB.getSidebar().removeScore(14);
				}
				int teamSize = TeamHandler.getMaxTeamSize();
				String scenarios = (teamSize == 1 ? "FFA" : "To" + teamSize) + " " +  ScenarioManager.getText() + (OptionsHandler.isRush() ? " Rush" : "");
				if(!scenarios.equals(oldScenarios)) {
					oldScenarios = scenarios;
					removeScore(11);
				}
				if(ServerLogger.updatePlayerCount()) {
					removeScore(7);
					removeScore(4);
				}
				if(getGameState() != GameStates.WAITING) {
					removeScore(4);
				}
				if(getGameState() != getOldGameState()) {
					setOldGameState(getGameState());
					removeScore(5);
				}
				int size = ProPlugin.getPlayers().size();
				String countDown = getGameState() == GameStates.WAITING ? "&b" + size + " &7/&b " + getRequiredPlayers() : CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA);
				if(GracePeriod.isRunning()) {
					countDown += " " + GracePeriod.getGraceCounterString(ChatColor.GRAY) + " Grace";
				}
				setText(new String [] {
					"&e&lBorder Radius",
					"&b" + r + "x" + r,
					"  ",
					"&e&lScenario",
					"&b" + scenarios,
					"&7More Info: /sInfo",
					"   ",
					"&e&lPlaying",
					"&b" + size + " &7/&b " + OSTB.getMaxPlayers(),
					"    ",
					"&e&l" + getGameState().getDisplay() + (getGameState() == GameStates.STARTED ? "" : " Stage"),
					countDown,
					"     ",
					"&a&lOutsideTheBlock.org",
					"&e&lServer &b&l" + OSTB.getPlugin().getServer().toUpperCase() + OSTB.getServerName().replaceAll("[^\\d.]", "")
				});
				super.update();
			}
		});
    }

    @Override
    public void disable() {
    	int server = Integer.valueOf(OSTB.getServerName().replace("UHC", ""));
    	DB.NETWORK_UHC_URL.delete("server", "" + server);
    	super.disable();
    	String container = Bukkit.getWorldContainer().getPath();
        Bukkit.unloadWorld(getLobby(), false);
        File newWorld = new File(container + "/../resources/maps/uhc");
        if(newWorld.exists() && newWorld.isDirectory()) {
            FileHandler.delete(new File(container + "/lobby"));
            FileHandler.copyFolder(newWorld, new File(container + "/lobby"));
        }
    }
    
    public static String getAcount() {
    	return account;
    }
}
