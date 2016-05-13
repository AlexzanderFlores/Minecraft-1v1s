package ostb.gameapi.games.skywars;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.gameapi.StatsHandler;
import ostb.gameapi.games.skywars.mapeffects.Frozen;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.DB;

public class SkyWars extends MiniGame {
	public SkyWars() {
		super("Sky Wars");
		setVotingCounter(45);
		setStartingCounter(10);
		setFlintAndSteelUses(4);
		new CoinsHandler(DB.PLAYERS_COINS_SKY_WARS, Plugins.SW.getData());
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(10);
		new StatsHandler(DB.PLAYERS_STATS_SKY_WARS, DB.PLAYERS_STATS_SKY_WARS_MONTHLY, DB.PLAYERS_STATS_SKY_WARS_WEEKLY);
		new BelowNameHealthScoreboardUtil();
		new Events();
		new ChestHandler();
		new SkyWarsShop();
		if(OSTB.getPlugin() == Plugins.SWT) {
			new TeamHandler();
			setRequiredPlayers(8);
		}
		// Map effects
		new Frozen();
	}
}
