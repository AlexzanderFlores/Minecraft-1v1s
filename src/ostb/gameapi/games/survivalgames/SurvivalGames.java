package ostb.gameapi.games.survivalgames;

import org.bukkit.ChatColor;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.MiniGame;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;

public class SurvivalGames extends MiniGame {
	private String oldCountDownLine = "";
	
	public SurvivalGames() {
		super("Survival Games");
		setStartingCounter(10);
		new Events();
		new ChestHandler();
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
