package ostb.gameapi.games.pvpbattles;
import ostb.gameapi.MiniGame;

public class PVPBattles extends MiniGame {
	public PVPBattles(String name) {
		super(name);
		setRequiredPlayers(8);
		new Events();
	}
}
