package ostb.gameapi.games.pvpbattles;

import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.player.CoinsHandler;
import ostb.server.DB;

public class PVPBattles extends MiniGame {
	public PVPBattles(String name) {
		super(name);
		setRequiredPlayers(8);
		setFlintAndSteelUses(4);
		getTeamHandler().toggleTeamItem();
		new CoinsHandler(DB.PLAYERS_COINS_PVP_BATTLES, Plugins.PVP_BATTLES);
		CoinsHandler.setKillCoins(5);
		CoinsHandler.setWinCoins(20);
		new Events();
	}
}
