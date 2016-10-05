package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Scoreboard;

import network.customevents.player.InventoryItemClickEvent;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerAFKEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PostPlayerJoinEvent;
import network.gameapi.MiniGame;
import network.gameapi.games.kitpvp.KitPVP;
import network.gameapi.games.onevsones.OnevsOnes;
import network.gameapi.games.uhcskywars.SkyWars;
import network.player.ChatLogger;
import network.player.DefaultChatColor;
import network.player.LevelHandler;
import network.player.PrivateMessaging;
import network.player.account.AccountHandler;
import network.player.account.PlayerTracker;
import network.player.account.PlaytimeTracker;
import network.player.scoreboard.BelowNameScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.AlertHandler;
import network.server.Alerts;
import network.server.AutoAlerts;
import network.server.CommandDispatcher;
import network.server.DB;
import network.server.DB.Databases;
import network.server.GeneralEvents;
import network.server.GlobalCommands;
import network.server.PerformanceHandler;
import network.server.RestarterHandler;
import network.server.servers.building.Building;
import network.server.servers.hub.items.features.particles.Particles;
import network.server.servers.hub.main.MainHub;
import network.server.servers.slave.Slave;
import network.server.servers.worker.Worker;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.CommandRepeater;
import network.server.util.FileHandler;
import network.server.util.Glow;
import network.server.util.JarUtils;
import network.staff.Punishment;

public class Network extends JavaPlugin implements PluginMessageListener {
	public enum Plugins {
		HUB("HUB", "hub", "Hub"),
		KITPVP("KitPVP", "kit_pvp", "Kit PVP"),
		ONEVSONE("1v1s", "1v1", "1v1s"),
		UHCSW("UHCSW", "sky_wars", "UHC Sky Wars"),
		BUILDING("Building", "building"),
		SLAVE("Slave", "slave"),
		WORKER("Worker", "worker");
		
		private String server = null;
		private String data = null;
		private String display = null;
		
		private Plugins(String server, String data) {
			this.server = server;
			this.data = data;
			this.display = server;
		}
		
		private Plugins(String server, String data, String display) {
			this.server = server;
			this.data = data;
			this.display = display;
		}
		
		public String getServer() {
			return server;
		}
		
		public String getData() {
			return data;
		}
		
		public String getDisplay() {
			return display;
		}
	}
	
	private static Network instance = null;
	private static Plugins plugin = null;
	private static ProPlugin proPlugin = null;
	private static MiniGame miniGame = null;
	private static String serverName = null;
	private static String consumerKey = "";
	private static String consumerSecret = "";
	private static SidebarScoreboardUtil sidebar = null;
	private static BelowNameScoreboardUtil belowName = null;
	private static int maxPlayers = -1;
	
	@Override
	public void onEnable() {
		instance = this;
		Bukkit.getMessenger().registerOutgoingPluginChannel(getInstance(), "BungeeCord");
		Bukkit.getMessenger().registerIncomingPluginChannel(getInstance(), "WDL|INIT", this);
		Bukkit.getMessenger().registerIncomingPluginChannel(getInstance(), "WDL|CONTROL", this);
		sidebar = new SidebarScoreboardUtil("");
		try {
			String path = "/root/resources";
			File [] libs = new File [] {
    			new File(path, "twitter4j-core-4.0.4.jar"),
    			new File(path, "twitter4j-async-4.0.4.jar"),
    			new File(path, "twitter4j-media-support-4.0.4.jar"),
    			new File(path, "twitter4j-stream-4.0.4.jar")
        	};
            for(File lib : libs) {
                if(lib.exists()) {
                    JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
                }
            }
            for(File lib : libs) {
                if(lib.exists()) {
                	addClassPath(JarUtils.getJarUrl(lib));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
		serverName = new File(Bukkit.getWorldContainer().getPath() + "/..").getAbsolutePath().replaceAll("/root/", "");
		serverName = serverName.split("/")[0].toUpperCase();
		plugin = Plugins.valueOf(serverName.replaceAll("[\\d]", ""));
		try {
			switch(plugin) {
			case HUB:
				proPlugin = new MainHub();
				break;
			case KITPVP:
				proPlugin = new KitPVP();
				break;
			case ONEVSONE:
				proPlugin = new OnevsOnes();
				break;
			case UHCSW:
				proPlugin = new SkyWars();
				break;
			case BUILDING:
				proPlugin = new Building();
				break;
			case SLAVE:
				proPlugin = new Slave();
				break;
			case WORKER:
				proPlugin = new Worker();
				break;
			default:
				
				break;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		DB.values(); // Call the enumeration constructors for each item to initialize them
		maxPlayers = Bukkit.getMaxPlayers();
		new LevelHandler();
		new AccountHandler();
		new GlobalCommands();
		new PerformanceHandler();
		new GeneralEvents();
		new PlayerLeaveEvent();
		new InventoryItemClickEvent();
		new PrivateMessaging();
		new PlayerAFKEvent();
		new PlaytimeTracker();
		new RestarterHandler();
		new Punishment();
		new PlayerTracker();
		new PostPlayerJoinEvent();
		new MouseClickEvent();
		new AlertHandler();
		new ChatLogger();
		new Particles();
		new AutoAlerts();
		new CommandDispatcher();
		new DefaultChatColor();
		new Alerts();
		new CommandRepeater();
		Glow.register();
		
		// Init Twitter
		File file = new File("/root/resources/twitter4j.properties");
		if(file.exists()) {
			FileHandler.copyFile(file, new File("/root/" + getServerName().toLowerCase() + "/twitter4j.properties"));
			String consumerKeyDelimiter = "oauth.consumerKey=";
			String consumerSecretDelimiter = "oauth.consumerSecret=";
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String input = null;
				while((input = reader.readLine()) != null) {
					if(input.startsWith(consumerKeyDelimiter)) {
						consumerKey = input.replace(consumerKeyDelimiter, "");
					} else if(input.startsWith(consumerSecretDelimiter)) {
						consumerSecret = input.replace(consumerSecretDelimiter, "");
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if(reader != null) {
					try {
						reader.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public void onDisable() {
		proPlugin.disable();
		for(World world : Bukkit.getWorlds()) {
			Bukkit.unloadWorld(world, false);
		}
		for(Databases database : Databases.values()) {
			database.disconnect();
		}
		FileHandler.checkForUpdates();
	}
	
	private void addClassPath(URL url) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch(Throwable t) {
            t.printStackTrace();
            throw new IOException("Error adding " + url + " to system classloader");
        }
    }
	
	public static Network getInstance() {
		return instance;
	}
	
	public static Plugins getPlugin() {
		return plugin;
	}
	
	public static ProPlugin getProPlugin() {
		return proPlugin;
	}
	
	public static void setProPlugin(ProPlugin proPlugin) {
		Network.proPlugin = proPlugin;
	}
	
	public static void setMiniGame(MiniGame newMiniGame) {
		miniGame = newMiniGame;
	}
	
	public static MiniGame getMiniGame() {
		return miniGame;
	}
	
	public static String getServerName() {
		return serverName;
	}
	
	public static SidebarScoreboardUtil getSidebar() {
		return sidebar;
	}
	
	public static void setSidebar(SidebarScoreboardUtil sidebar) {
		Network.sidebar = sidebar;
	}
	
	public static BelowNameScoreboardUtil getBelowName() {
		return belowName;
	}
	
	public static void setBelowName(BelowNameScoreboardUtil belowName) {
		Network.belowName = belowName;
	}
	
	public static Scoreboard getScoreboard() {
		return getSidebar().getScoreboard();
	}
	
	public static int getMaxPlayers() {
		return maxPlayers == -1 ? Bukkit.getMaxPlayers() : maxPlayers;
	}
	
	public static void setMaxPlayers(int max) {
		if(max != Bukkit.getMaxPlayers()) {
			maxPlayers = max;
		}
	}
	
	public static String getConsumerKey() {
		return consumerKey;
	}
	
	public static String getConsumerSecret() {
		return consumerSecret;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte [] message) {
		if(channel.equals("WDL|INIT") || channel.equals("WDL|CONTROL")) {
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.PLAYERS_WORLD_DOWNLOADER.insert("'" + uuid.toString() + "'");
				}
			});
			player.kickPlayer(ChatColor.RED + "World Downloader is not allowed on this server!");
		}
	}
}