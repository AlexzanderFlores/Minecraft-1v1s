package ostb.gameapi.games.skywars;

import ostb.gameapi.MiniGame;
import ostb.gameapi.games.skywars.cages.Cage;
import ostb.gameapi.games.skywars.cages.CageSelector;
import ostb.gameapi.games.skywars.kits.Archer;
import ostb.gameapi.games.skywars.kits.Bomber;
import ostb.gameapi.games.skywars.kits.Builder;
import ostb.gameapi.games.skywars.kits.CowSlayer;
import ostb.gameapi.games.skywars.kits.Enchanter;
import ostb.gameapi.games.skywars.kits.Enderman;
import ostb.gameapi.games.skywars.kits.Fisherman;
import ostb.gameapi.games.skywars.kits.Looter;
import ostb.gameapi.games.skywars.kits.Medic;
import ostb.gameapi.games.skywars.kits.Miner;
import ostb.gameapi.games.skywars.kits.Ninja;
import ostb.gameapi.games.skywars.kits.Pyro;
import ostb.gameapi.games.skywars.kits.Spiderman;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;

public class SkyWars extends MiniGame {
	public SkyWars() {
		super("Sky Wars");
		setVotingCounter(45);
		setStartingCounter(10);
		setFlintAndSteelUses(4);
		new BelowNameHealthScoreboardUtil();
		new Events();
		Cage.createCages();
		new Archer();
		new Builder();
		new Looter();
		new Enchanter();
		new Bomber();
		new Ninja();
		new Medic();
		new CowSlayer();
		new Enderman();
		new Fisherman();
		new Spiderman();
		new Pyro();
		new Miner();
		new CageSelector();
	}
}
