package ostb.gameapi;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameEndingEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.server.DB;
import ostb.server.PerformanceHandler;
import ostb.server.util.EventUtil;

public class PerformanceLogger implements Listener {
	private static int maxPlayers = 0;
	private double maxMemory = 0;
	private String maxMemoryTime = "N/A";
	private double lowestTPS = 20.0;
	private String map = "none";
	
	public PerformanceLogger() {
		EventUtil.register(this);
	}
	
	public static int getMaxPlayers() {
		return maxPlayers;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			double memory = PerformanceHandler.getMemory();
			if(memory >= maxMemory) {
				maxMemory = memory;
				maxMemoryTime = PerformanceHandler.getUptimeString();
			}
			double tps = PerformanceHandler.getTicksPerSecond();
			if(PerformanceHandler.getUptime() >= 10 && tps <= lowestTPS) {
				lowestTPS = tps;
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		int size = Bukkit.getOnlinePlayers().size();
		if(size > maxPlayers) {
			maxPlayers = size;
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		map = ProPlugin.getPlayers().get(0).getWorld().getName();
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		String server = OSTB.getServerName();
		DB.NETWORK_MINI_GAME_PERFORMANCE.insert("'" + server + "', '" + map + "', '" + maxPlayers + "', '" + maxMemory + "', '" + maxMemoryTime + "', '" + lowestTPS + "'");
	}
}
