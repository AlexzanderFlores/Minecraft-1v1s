package ostb.gameapi.games.speeduhc;

import ostb.gameapi.GoldenHeadUtil;
import ostb.gameapi.MiniGame;
import ostb.gameapi.SkullPikeUtil;
import ostb.gameapi.scenarios.scenarios.AppleRates;
import ostb.gameapi.scenarios.scenarios.CutClean;
import ostb.gameapi.scenarios.scenarios.OreMultipliers;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.BiomeSwap;

public class SpeedUHC extends MiniGame {
	public SpeedUHC() {
		super("Speed UHC");
		setRequiredPlayers(4);
		setStartingCounter(10);
		BiomeSwap.setUpUHC();
		new OreMultipliers();
		OreMultipliers.setMultiplier(2);
		new CutClean();
		new AppleRates(50);
		new Events();
		new WorldHandler();
		new SkullPikeUtil();
		new GoldenHeadUtil();
		new BelowNameHealthScoreboardUtil();
	}
}
