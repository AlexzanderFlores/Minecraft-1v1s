package ostb.gameapi.games.skywars;

import org.bukkit.ChatColor;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.gameapi.MiniGame;
import ostb.gameapi.competitive.StatsHandler;
import ostb.gameapi.games.skywars.mapeffects.Frozen;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.gameapi.uhc.scenarios.scenarios.CutClean;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;

public class SkyWars extends MiniGame {
	private String oldCountDownLine = "";
	
	public SkyWars() {
		this("Sky Wars");
	}
	
	public SkyWars(String name) {
		super(name);
		setVotingCounter(45);
		setStartingCounter(10);
		setFlintAndSteelUses(4);
		setCanJoinWhileStarting(false);
		new CoinsHandler(DB.PLAYERS_COINS_SKY_WARS, Plugins.SW.getData());
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(10);
		new StatsHandler(DB.PLAYERS_STATS_SKY_WARS, DB.PLAYERS_STATS_SKY_WARS_MONTHLY, DB.PLAYERS_STATS_SKY_WARS_WEEKLY);
		new BelowNameHealthScoreboardUtil();
		new Events();
		new ChestHandler();
		new SkyWarsShop();
		new LootPassHandler();
		if(OSTB.getPlugin() == Plugins.SWT) {
			new TeamHandler();
			setRequiredPlayers(8);
		}
		// Map effects
		new Frozen();
		new CutClean().enable(false);
		OSTB.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
			@Override
			public void update() {
				int size = ProPlugin.getPlayers().size();
				int restockCounter = ChestHandler.getRestockCounter();
				String restock = restockCounter > 0 && getGameState() == GameStates.STARTED ? " &7Chest Refill " + new CountDownUtil(restockCounter).getCounterAsString(ChatColor.GRAY) : "";
				String countDownLine = getGameState() == GameStates.WAITING ? "&b" + size + " &7/&b " + getRequiredPlayers() : CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA) + restock;
				if(!oldCountDownLine.equals(countDownLine)) {
					oldCountDownLine = countDownLine;
					removeScore(5);
				}
				if(ServerLogger.updatePlayerCount()) {
					removeScore(8);
				}
				if(getGameState() != getOldGameState()) {
					setOldGameState(getGameState());
					removeScore(6);
				}
				setText(new String [] {
					" ",
					"&e&lPlaying",
					"&b" + size + " &7/&b " + OSTB.getMaxPlayers(),
					"  ",
					"&e&l" + getGameState().getDisplay() + (getGameState() == GameStates.STARTED ? "" : " Stage"),
					countDownLine,
					"   ",
					"&a&lOutsideTheBlock.org",
					"&e&lServer &b&l" + OSTB.getPlugin().getServer().toUpperCase() + OSTB.getServerName().replaceAll("[^\\d.]", ""),
					"    "
				});
				super.update();
			}
		});
	}
}
