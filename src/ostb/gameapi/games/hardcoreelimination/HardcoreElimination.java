package ostb.gameapi.games.hardcoreelimination;

import ostb.gameapi.MiniGame;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;

public class HardcoreElimination extends MiniGame {
	public HardcoreElimination() {
		super("Hardcore Elimination");
		setRequiredPlayers(4);
		new Events();
		new WorldHandler();
		new BelowNameHealthScoreboardUtil();
	}
}
