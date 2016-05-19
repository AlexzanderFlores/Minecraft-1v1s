package ostb.gameapi.games.kitpvp;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.MiniGame;
import ostb.gameapi.StatsHandler;
import ostb.gameapi.games.kitpvp.TeamHandler.KitTeam;
import ostb.player.CoinsHandler;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.util.CountDownUtil;
import ostb.server.util.FileHandler;

public class KitPVP extends MiniGame {
	private static TeamHandler teamHandler = null;
	private String oldScore = "";
	private String oldCount = "";
	
	public KitPVP() {
		super("KitPVP");
		setPlayersHaveOneLife(false);
		setMap(Bukkit.getWorlds().get(0));
		setGameState(GameStates.STARTED);
		setCounter(60 * 60);
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowPlayerInteraction(true);
		setAllowBowShooting(true);
		setEnd(false);
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY, DB.PLAYERS_STATS_KIT_PVP_WEEKLY);
		new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData());
		CoinsHandler.setKillCoins(2);
		CoinsHandler.setWinCoins(25);
		OSTB.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
			@Override
			public void update() {
				String score = Events.getPaused() ? "&7Paused" : KitTeam.RED.getScoreString() + " &7/ " + KitTeam.BLUE.getScoreString() + " &7/ " + KitTeam.YELLOW.getScoreString() + " &7/ " + KitTeam.GREEN.getScoreString();
				if(!oldScore.equals(score)) {
					oldScore = score;
					removeScore(11);
				}
				String count = KitTeam.RED.getSizeString() + " &7/ " + KitTeam.BLUE.getSizeString() + " &7/ " + KitTeam.YELLOW.getSizeString() + " &7/ " + KitTeam.GREEN.getSizeString() + " ";
				if(!oldCount.equals(count)) {
					oldCount = count;
					removeScore(8);
				}
				removeScore(5);
				setText(new String [] {
					" ",
					"&eScores",
					score,
					"  ",
					"&ePlaying",
					count,
					"   ",
					"&eScores Reset In",
					CountDownUtil.getCounterAsString(getCounter(), ChatColor.AQUA) + (Events.getPaused() ? " &7(Paused)" : ""),
					"    ",
					"&aOutsideTheBlock.org",
					"&eServer &b" + OSTB.getPlugin().getServer().toUpperCase() + OSTB.getServerName().replaceAll("[^\\d.]", ""),
					"     "
				});
				super.update();
			}
		});
		teamHandler = new TeamHandler();
		new SpawnHandler();
		new Events();
	}
	
	@Override
	public void disable() {
		String container = Bukkit.getWorldContainer().getPath();
		String name = "kitpvp";
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/" + name));
		FileHandler.copyFolder(new File(container + "/../resources/maps/" + name), new File(container + "/" + name));
	}
	
	public static TeamHandler getKitPVPTeamHandler() {
		return teamHandler;
	}
}
