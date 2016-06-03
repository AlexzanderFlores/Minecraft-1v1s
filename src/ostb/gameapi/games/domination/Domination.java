package ostb.gameapi.games.domination;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.gameapi.AssistTracker;
import ostb.gameapi.MiniGame;
import ostb.gameapi.TeamHandler;
import ostb.gameapi.TemporaryFireUtil;
import ostb.gameapi.competitive.EloRanking;
import ostb.gameapi.competitive.StatsHandler;
import ostb.gameapi.games.domination.mapeffects.Divided_Kingdom;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;
import ostb.server.util.ImageMap;

public class Domination extends MiniGame {
	private int lastRedSize = -1;
	private int lastBlueSize = -1;
	
	public Domination() {
		super("Domination");
		setStartingCounter(20);
		setRequiredPlayers(8);
		setFlintAndSteelUses(4);
		getTeamHandler().toggleTeamItem();
		new CoinsHandler(DB.PLAYERS_COINS_DOMINATION, Plugins.DOM.getData());
		CoinsHandler.setKillCoins(20);
		CoinsHandler.setWinCoins(75);
		new StatsHandler(DB.PLAYERS_STATS_DOMINATION, DB.PLAYERS_STATS_DOMINATION_MONTHLY, DB.PLAYERS_STATS_DOMINATION_WEEKLY);
		StatsHandler.setEloDB(DB.PLAYERS_DOMINATION_ELO);
		List<ItemFrame> frames = new ArrayList<ItemFrame>();
		World world = getLobby();
		frames.add(ImageMap.getItemFrame(world, 14, 7, -2));
		frames.add(ImageMap.getItemFrame(world, -14, 7, 2));
		new EloRanking(frames, DB.PLAYERS_DOMINATION_ELO, DB.PLAYERS_DOMINATION_RANK);
		frames.clear();
		frames = null;
		new Events();
		new BelowNameHealthScoreboardUtil();
		new AutoRespawn();
		new TipGiver();
		new TemporaryFireUtil(20 * 3);
		new AssistTracker();
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
					"&e&lTeam Sizes",
					"&c" + redSize + "&7 - &b" + blueSize,
					"  ",
					"&e&l" + getGameState().getDisplay() + (getGameState() == GameStates.STARTED ? "" : " Stage"),
					getGameState() == GameStates.WAITING ? "&b" + size + " &7/&b " + getRequiredPlayers() : CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA),
					"   ",
					"&a&lOutsideTheBlock.org",
					"&e&lServer &b&l" + OSTB.getPlugin().getServer().toUpperCase() + OSTB.getServerName().replaceAll("[^\\d.]", ""),
					"    ",
				});
				super.update();
			}
		});
		new Divided_Kingdom();
		new DOM(1000);
	}
}
