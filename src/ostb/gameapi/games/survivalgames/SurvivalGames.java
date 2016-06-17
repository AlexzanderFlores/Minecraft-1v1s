package ostb.gameapi.games.survivalgames;

import ostb.gameapi.MiniGame;

public class SurvivalGames extends MiniGame {
	public SurvivalGames() {
		super("Survival Games");
		new Events();
		new ChestHandler();
	}
}
