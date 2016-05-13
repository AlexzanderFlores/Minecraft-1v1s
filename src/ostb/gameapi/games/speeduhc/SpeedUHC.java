package ostb.gameapi.games.speeduhc;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.gameapi.scenarios.scenarios.AppleRates;
import ostb.gameapi.scenarios.scenarios.CutClean;
import ostb.gameapi.scenarios.scenarios.OreMultipliers;
import ostb.gameapi.shops.SpeedUHCShop;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.DB;

public class SpeedUHC extends MiniGame {
	public SpeedUHC() {
		super("Speed UHC");
		setRequiredPlayers(4);
		setStartingCounter(10);
		new CoinsHandler(DB.PLAYERS_COINS_SPEED_UHC, Plugins.SUHCK);
		CoinsHandler.setKillCoins(5);
		CoinsHandler.setWinCoins(15);
		new OreMultipliers();
		OreMultipliers.setMultiplier(2);
		new CutClean();
		new AppleRates(50);
		new Events();
		new WorldHandler();
		new BelowNameHealthScoreboardUtil();
		if(OSTB.getPlugin() == Plugins.SUHCK) {
			new SpeedUHCShop();
		}
	}
}
