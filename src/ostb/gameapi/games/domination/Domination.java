package ostb.gameapi.games.domination;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.gameapi.MiniGame;
import ostb.gameapi.StatsHandler;
import ostb.gameapi.TeamHandler;
import ostb.gameapi.games.domination.mapeffects.Divided_Kingdom;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;

public class Domination extends MiniGame {
	private int lastRedSize = -1;
	private int lastBlueSize = -1;
	
	public Domination() {
		super("Domination");
		setStartingCounter(20);
		setRequiredPlayers(8);
		setFlintAndSteelUses(4);
		getTeamHandler().toggleTeamItem();
		new DOM(1000);
		new CoinsHandler(DB.PLAYERS_COINS_DOMINATION, Plugins.DOM.getData());
		CoinsHandler.setKillCoins(20);
		CoinsHandler.setWinCoins(75);
		new StatsHandler(DB.PLAYERS_STATS_DOMINATION, DB.PLAYERS_STATS_DOMINATION_MONTHLY, DB.PLAYERS_STATS_DOMINATION_WEEKLY);
		StatsHandler.setEloDB(DB.PLAYERS_DOMINATION_ELO);
		new Ranking();
		new Events();
		new BelowNameHealthScoreboardUtil();
		new AutoRespawn();
		OSTB.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
			@Override
			public void update() {
				MiniGame miniGame = OSTB.getMiniGame();
				if(ServerLogger.updatePlayerCount()) {
					removeScore(5);
				}
				if(getGameState() != GameStates.WAITING) {
					removeScore(5);
				}
				if(getGameState() != miniGame.getOldGameState()) {
					miniGame.setOldGameState(getGameState());
					removeScore(6);
				}
				int size = ProPlugin.getPlayers().size();
				TeamHandler teamHandler = miniGame.getTeamHandler();
				Team redTeam = teamHandler.getTeam("red");
				Team blueTeam = teamHandler.getTeam("blue");
				int redSize = redTeam == null ? 0 : redTeam.getSize();
				int blueSize = blueTeam == null ? 0 : blueTeam.getSize();
				if(redSize != lastRedSize) {
					lastRedSize = redSize;
					removeScore(8);
				}
				if(blueSize != lastBlueSize) {
					lastBlueSize = blueSize;
					removeScore(8);
				}
				setText(new String [] {
					" ",
					"&eTeam Sizes",
					"&c" + redSize + "&7 - &b" + blueSize,
					"  ",
					"&e" + getGameState().getDisplay() + (getGameState() == GameStates.STARTED ? "" : " Stage"),
					getGameState() == GameStates.WAITING ? "&b" + size + " &7/&b " + getRequiredPlayers() : CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA),
					"   ",
					"&aOutsideTheBlock.org",
					"&eServer &b" + OSTB.getPlugin().getServer().toUpperCase() + OSTB.getServerName().replaceAll("[^\\d.]", ""),
					"    ",
				});
				super.update();
			}
		});
		new Divided_Kingdom();
	}
}
