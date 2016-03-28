package ostb.gameapi.games.hardcoreelimination;

import ostb.gameapi.MiniGame;
import ostb.gameapi.scenarios.scenarios.CutClean;
import ostb.gameapi.scenarios.scenarios.OreMultipliers;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;

public class HardcoreElimination extends MiniGame {
	public HardcoreElimination() {
		super("Hardcore Elimination");
		setRequiredPlayers(4);
		new OreMultipliers();
		OreMultipliers.setMultiplier(2);
		new CutClean();
		new Events();
		new WorldHandler();
		new BelowNameHealthScoreboardUtil();
	}
}
