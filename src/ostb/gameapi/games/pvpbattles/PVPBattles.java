package ostb.gameapi.games.pvpbattles;

import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.player.CoinsHandler;
import ostb.server.DB;

public class PVPBattles extends MiniGame {
	public PVPBattles(String name) {
		super(name);
		setStartingCounter(20);
		setRequiredPlayers(8);
		setFlintAndSteelUses(4);
		getTeamHandler().toggleTeamItem();
		new CoinsHandler(DB.PLAYERS_COINS_PVP_BATTLES, Plugins.PVP_BATTLES);
		CoinsHandler.setKillCoins(20);
		CoinsHandler.setWinCoins(75);
		new Events();
	}
}
