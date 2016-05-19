package ostb.gameapi.games.kitpvp;

import org.bukkit.Bukkit;

import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.gameapi.StatsHandler;
import ostb.player.CoinsHandler;
import ostb.server.DB;

public class KitPVP extends MiniGame {
	private static TeamHandler teamHandler = null;
	
	public KitPVP() {
		super("KitPVP");
		setPlayersHaveOneLife(false);
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY, DB.PLAYERS_STATS_KIT_PVP_WEEKLY);
		new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData());
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(25);
		teamHandler = new TeamHandler();
		new SpawnHandler();
		new Events();
		setGameState(GameStates.STARTED);
		setMap(Bukkit.getWorlds().get(0));
	}
	
	public static TeamHandler getKitPVPTeamHandler() {
		return teamHandler;
	}
}
