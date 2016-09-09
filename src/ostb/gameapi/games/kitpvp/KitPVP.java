package ostb.gameapi.games.kitpvp;

import java.io.File;

import org.bukkit.Bukkit;

import ostb.ProPlugin;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.TemporaryFireUtil;
import ostb.gameapi.competitive.StatsHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.FileHandler;

public class KitPVP extends ProPlugin {
	public KitPVP() {
		super("KitPVP");
		setCounter(60 * 10);
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowPlayerInteraction(true);
		setAllowBowShooting(true);
		setAllowInventoryClicking(true);
		setFlintAndSteelUses(2);
		new ServerLogger();
		new SpectatorHandler();
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY, DB.PLAYERS_STATS_KIT_PVP_WEEKLY);
		new Events();
		new TemporaryFireUtil(20 * 5);
		new BelowNameHealthScoreboardUtil();
		Bukkit.getWorlds().get(0).setGameRuleValue("keepInventory", "true");
	}
	
	@Override
	public void disable() {
		String container = Bukkit.getWorldContainer().getPath();
		String name = "kitpvp";
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/" + name));
		FileHandler.copyFolder(new File(container + "/../resources/maps/" + name), new File(container + "/" + name));
	}
}
