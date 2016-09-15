package network;

import java.io.File;
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
import network.gameapi.kit.DefaultKit;
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
import network.server.AutoAlerts;
import network.server.CommandDispatcher;
import network.server.DB;
import network.server.GeneralEvents;
import network.server.GlobalCommands;
import network.server.PerformanceHandler;
import network.server.RankAds;
import network.server.RestarterHandler;
import network.server.DB.Databases;
import network.server.servers.building.Building;
import network.server.servers.hub.items.features.particles.Particles;
import network.server.servers.hub.main.MainHub;
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
		BUILDING("Building", "building"),
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
        	File [] libs = new File [] {
        		new File("root/resources/", "Twitter4j.jar")
        	};
            for(File lib : libs) {
                if(lib.exists()) {
                    JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
                }
            }
            for(final File lib : libs) {
                if(lib.exists()) {
                	addClassPath(JarUtils.getJarUrl(lib));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
		serverName = new File(Bukkit.getWorldContainer().getPath() + "/..").getAbsolutePath().replaceAll("/root/", "");
		//Bukkit.getLogger().info(serverName);
		serverName = serverName.split("/")[0].toUpperCase();
		//Bukkit.getLogger().info(serverName);
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
			case BUILDING:
				proPlugin = new Building();
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
		new DefaultKit();
		new RankAds();
		new CommandRepeater();
		Glow.register();
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