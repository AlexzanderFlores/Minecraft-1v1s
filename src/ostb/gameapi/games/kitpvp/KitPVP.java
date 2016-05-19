package ostb.gameapi.games.kitpvp;

import java.io.File;

import org.bukkit.Bukkit;

import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.gameapi.StatsHandler;
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.util.FileHandler;

public class KitPVP extends MiniGame {
	private static TeamHandler teamHandler = null;
	
	public KitPVP() {
		super("KitPVP");
		setPlayersHaveOneLife(false);
		setMap(Bukkit.getWorlds().get(0));
		setGameState(GameStates.STARTED);
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowPlayerInteraction(true);
		setAllowBowShooting(true);
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY, DB.PLAYERS_STATS_KIT_PVP_WEEKLY);
		new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData());
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(25);
		teamHandler = new TeamHandler();
		new SpawnHandler();
		new Events();
	}
	
	@Override
	public void disable() {
		String container = Bukkit.getWorldContainer().getPath();
		String name = "kitpvp";
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/" + name));
		FileHandler.copyFolder(new File(container + "/../resources/maps/" + name), new File(container + "/" + name));
	}
	
	public static TeamHandler getKitPVPTeamHandler() {
		return teamHandler;
	}
}
