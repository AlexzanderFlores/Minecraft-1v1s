package ostb.gameapi.games.pvpbattles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.player.PlayerItemFrameInteractEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.TeamHandler;
import ostb.player.CoinsHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.CountDownUtil;
import ostb.server.util.ImageMap;

public class PVPBattles extends MiniGame {
	private int lastRedSize = -1;
	private int lastBlueSize = -1;
	private List<ItemFrame> frames = null;
	
	public PVPBattles(String name) {
		super(name);
		setStartingCounter(20);
		setRequiredPlayers(8);
		setFlintAndSteelUses(4);
		getTeamHandler().toggleTeamItem();
		new CoinsHandler(DB.PLAYERS_COINS_PVP_BATTLES, Plugins.PVP_BATTLES);
		CoinsHandler.setKillCoins(20);
		CoinsHandler.setWinCoins(75);
		new Events();
		new BelowNameHealthScoreboardUtil();
		frames = new ArrayList<ItemFrame>();
		String path = Bukkit.getWorldContainer().getPath() + "/../resources/Elo.png";
		frames.addAll(new ImageMap(ImageMap.getItemFrame(14, 7, -2), path).getItemFrames());
		frames.addAll(new ImageMap(ImageMap.getItemFrame(-14, 7, 2), path).getItemFrames());
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
	
	@EventHandler
	public void onPlayerItemFrameInteract(PlayerItemFrameInteractEvent event) {
		if(frames.contains(event.getItemFrame())) {
			Player player = event.getPlayer();
			MessageHandler.sendMessage(player, "You are within the percent range for BRONZE (" + (new Random().nextInt(100) + 1) + "%)");
			if(Ranks.PREMIUM.hasRank(player)) {
				MessageHandler.sendMessage(player, "Your exact Elo value is XXXX");
			} else {
				MessageHandler.sendMessage(player, "&cTo view your exact Elo value you must have " + Ranks.PREMIUM.getPrefix());
			}
		}
	}
}
