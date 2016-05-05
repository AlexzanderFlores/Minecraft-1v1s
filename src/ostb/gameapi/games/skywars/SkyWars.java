package ostb.gameapi.games.skywars;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
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
		new CoinsHandler(DB.PLAYERS_COINS_SKY_WARS, Plugins.SW);
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(10);
		new BelowNameHealthScoreboardUtil();
		new Events();
		new ChestHandler();
		new SkyWarsShop();
		if(OSTB.getPlugin() == Plugins.SWT) {
			
		}
	}
}
