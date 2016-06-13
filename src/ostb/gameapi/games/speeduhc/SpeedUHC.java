package ostb.gameapi.games.speeduhc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.MiniGame;
import ostb.gameapi.competitive.StatsHandler;
import ostb.gameapi.uhc.GoldenHeadUtil;
import ostb.gameapi.uhc.SkullPikeUtil;
import ostb.gameapi.uhc.scenarios.scenarios.AppleRates;
import ostb.gameapi.uhc.scenarios.scenarios.CutClean;
import ostb.gameapi.uhc.scenarios.scenarios.OreMultipliers;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.BiomeSwap;
import ostb.server.DB;

public class SpeedUHC extends MiniGame {
	public SpeedUHC() {
		super("Speed UHC");
		setRequiredPlayers(4);
		setStartingCounter(10);
		BiomeSwap.setUpUHC();
		new OreMultipliers("Double Ores", "2xO", 2, new ItemStack(Material.IRON_INGOT, 2));
		new CutClean();
		new AppleRates(50);
		new Events();
		new WorldHandler();
		new SkullPikeUtil();
		new GoldenHeadUtil();
		new BelowNameHealthScoreboardUtil();
		new StatsHandler(DB.PLAYERS_STATS_SPEED_UHC, DB.PLAYERS_STATS_SPEED_UHC_MONTHLY, DB.PLAYERS_STATS_SPEED_UHC_WEEKLY);
	}
}
