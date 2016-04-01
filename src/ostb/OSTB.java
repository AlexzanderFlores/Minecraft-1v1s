package ostb;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import ostb.anticheat.AntiCheat;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerAFKEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PostPlayerJoinEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.games.hardcoreelimination.HardcoreElimination;
import ostb.gameapi.games.pvpbattles.ctf.CTF;
import ostb.gameapi.games.pvpbattles.dom.DOM;
import ostb.gameapi.games.skywars.SkyWars;
import ostb.player.ChatLogger;
import ostb.player.LevelHandler;
import ostb.player.Particles;
import ostb.player.PrivateMessaging;
import ostb.player.account.AccountHandler;
import ostb.player.account.PlayerTracker;
import ostb.player.account.PlaytimeTracker;
import ostb.player.scoreboard.BelowNameScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.AlertHandler;
import ostb.server.AutoAlerts;
import ostb.server.CommandDispatcher;
import ostb.server.DB;
import ostb.server.DB.Databases;
import ostb.server.GeneralEvents;
import ostb.server.GlobalCommands;
import ostb.server.PerformanceHandler;
import ostb.server.RestarterHandler;
import ostb.server.networking.Client;
import ostb.server.servers.building.Building;
import ostb.server.servers.hub.main.MainHub;
import ostb.server.servers.slave.Slave;
import ostb.server.servers.worker.Worker;
import ostb.server.util.FileHandler;
import ostb.server.util.Glow;
import ostb.server.util.JarUtils;
import ostb.staff.Punishment;

public class OSTB extends JavaPlugin {
	public enum Plugins {
		HUB("HUB", "Hub"),
		CTF("CTF", "Capture the Flag"),
		DOM("DOM", "Domination"),
		SKY_WARS_SOLO("SW", "Solo Sky Wars"),
		SKY_WARS_TEAMS("SWT", "Team Sky Wars"),
		HE_NO_KITS("HENK", "Hardcore Elimination No Kits"),
		HE_KITS("HEK", "Hardcore Elimination Kits"),
		BUILDING("Building"),
		WORKER("Worker"),
		SLAVE("Slave");
		
		private String server = null;
		private String display = null;
		
		private Plugins(String server) {
			this.server = server;
			this.display = server;
		}
		
		private Plugins(String server, String display) {
			this.server = server;
			this.display = display;
		}
		
		public String getServer() {
			return server;
		}
		
		public String getDisplay() {
			return display;
		}
	}
	
	private static OSTB instance = null;
	private static Plugins plugin = null;
	private static ProPlugin proPlugin = null;
	private static MiniGame miniGame = null;
	private static Client client = null;
	private static String serverName = null;
	private static SidebarScoreboardUtil sidebar = null;
	private static BelowNameScoreboardUtil belowName = null;
	private static int maxPlayers = -1;
	
	@Override
	public void onEnable() {
		instance = this;
		Bukkit.getMessenger().registerOutgoingPluginChannel(getInstance(), "BungeeCord");
		sidebar = new SidebarScoreboardUtil("");
		try {
        	File [] libs = new File [] {
        		new File(Bukkit.getWorldContainer().getPath() + "/../resources/", "Twitter4j.jar")
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
		serverName = new File(Bukkit.getWorldContainer().getPath() + "/..").getAbsolutePath().replaceAll("/home/OSTB/", "");
		serverName = serverName.split("/")[0].toUpperCase();
		Bukkit.getLogger().info(serverName);
		plugin = Plugins.valueOf(serverName.replaceAll("[\\d]", ""));
		if(plugin == Plugins.HUB) {
			proPlugin = new MainHub();
		} else if(plugin == Plugins.CTF) {
			proPlugin = new CTF();
		} else if(plugin == Plugins.DOM) {
			proPlugin = new DOM();
		} else if(plugin == Plugins.SKY_WARS_SOLO || plugin == Plugins.SKY_WARS_TEAMS) {
			proPlugin = new SkyWars();
		} else if(plugin == Plugins.HE_NO_KITS || plugin == Plugins.HE_KITS) {
			proPlugin = new HardcoreElimination();
		} else if(plugin == Plugins.BUILDING) {
			proPlugin = new Building();
		} 
		else if(plugin == Plugins.WORKER) {
			proPlugin = new Worker();
		} else if(plugin == Plugins.SLAVE) {
			proPlugin = new Slave();
		}
		DB.values(); // Call the enumeration constructors for each item to initialize them
		maxPlayers = Bukkit.getMaxPlayers();
		new LevelHandler();
		new AccountHandler();
		new AntiCheat();
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
		Glow.register();
		client = new Client("192.198.207.74", 4500, 5000);
		client.start();
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
		if(client != null) {
			client.shutdown(true);
		} else if(plugin == Plugins.SLAVE && Slave.getServer() != null) {
			Slave.getServer().shutdown();
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
	
	public static OSTB getInstance() {
		return instance;
	}
	
	public static Plugins getPlugin() {
		return plugin;
	}
	
	public static ProPlugin getProPlugin() {
		return proPlugin;
	}
	
	public static void setProPlugin(ProPlugin proPlugin) {
		OSTB.proPlugin = proPlugin;
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
		OSTB.sidebar = sidebar;
	}
	
	public static BelowNameScoreboardUtil getBelowName() {
		return belowName;
	}
	
	public static void setBelowName(BelowNameScoreboardUtil belowName) {
		OSTB.belowName = belowName;
	}
	
	public static Scoreboard getScoreboard() {
		return getSidebar().getScoreboard();
	}
	
	public static Client getClient() {
		return client;
	}
	
	public static int getMaxPlayers() {
		return maxPlayers == -1 ? Bukkit.getMaxPlayers() : maxPlayers;
	}
	
	public static void setMaxPlayers(int max) {
		if(max != Bukkit.getMaxPlayers()) {
			maxPlayers = max;
		}
	}
}