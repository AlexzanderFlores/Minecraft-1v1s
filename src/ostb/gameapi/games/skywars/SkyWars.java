package ostb.gameapi.games.skywars;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;

public class SkyWars extends MiniGame {
	public SkyWars() {
		super("Sky Wars");
		setVotingCounter(45);
		setStartingCounter(10);
		setFlintAndSteelUses(4);
		new BelowNameHealthScoreboardUtil();
		new Events();
		new ChestHandler();
		new SkyWarsShop();
		if(OSTB.getPlugin() == Plugins.SKY_WARS_TEAMS) {
			
		}
	}
}
