package ostb.gameapi.games.kitpvp;

import org.bukkit.Bukkit;

import ostb.gameapi.MiniGame;

public class KitPVP extends MiniGame {
	private static TeamHandler teamHandler = null;
	
	public KitPVP() {
		super("KitPVP");
		teamHandler = new TeamHandler();
		new SpawnHandler();
		setGameState(GameStates.STARTED);
		setMap(Bukkit.getWorlds().get(0));
	}
	
	public static TeamHandler getKitPVPTeamHandler() {
		return teamHandler;
	}
}
