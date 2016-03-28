package ostb.gameapi.modes;

import ostb.OSTB;
import ostb.gameapi.MiniGame.GameStates;

public class TDM extends ModeBase {
	private int killLimit = 0;
	private int redKills = 0;
	private int blueKills = 0;
	
	public TDM(int killLimit) {
		super("Team Deathmatch", "TDM");
		this.killLimit = killLimit;
	}
	
	@Override
	public Teams getWinning() {
		int red = getKills(Teams.RED);
		int blue = getKills(Teams.BLUE);
		return red > blue ? Teams.RED : blue > red ? Teams.BLUE : null;
	}
	
	public int getKillLimit() {
		return this.killLimit;
	}
	
	public int getKills(Teams team) {
		return team == Teams.RED ? redKills : team == Teams.BLUE ? blueKills : 0;
	}
	
	public void addKill(Teams team) {
		if(team == Teams.RED) {
			++redKills;
			if(comebackEffect == null && ((int) Math.round(redKills * 100.0 / killLimit)) >= comeBackPercentage) {
				executeComebackEffects(team);
			} else if(redKills >= killLimit){
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			}
		} else if(team == Teams.BLUE) {
			++blueKills;
			if(comebackEffect == null && ((int) Math.round(blueKills * 100.0 / killLimit)) >= comeBackPercentage) {
				executeComebackEffects(team);
			} else if(blueKills >= killLimit){
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			}
		}
	}
}
