package ostb.gameapi.games.pvpbattles;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.gameapi.MiniGame;
import ostb.gameapi.TeamHandler;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;

public class PVPBattles extends MiniGame {
	private int lastRedSize = -1;
	private int lastBlueSize = -1;
	
	public PVPBattles(String name) {
		super(name);
		setStartingCounter(20);
		setRequiredPlayers(8);
		setFlintAndSteelUses(4);
		getTeamHandler().toggleTeamItem();
		new CoinsHandler(DB.PLAYERS_COINS_PVP_BATTLES, Plugins.PVP_BATTLES);
		CoinsHandler.setKillCoins(20);
		CoinsHandler.setWinCoins(75);
		new Ranking();
		new Events();
		new BelowNameHealthScoreboardUtil();
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
	}
}
