package network.gameapi.games.kitpvp;

import java.io.File;

import org.bukkit.Bukkit;

import network.Network;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.gameapi.TemporaryFireUtil;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.games.kitpvp.shop.Shop;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.server.DB;
import network.server.ServerLogger;
import network.server.util.FileHandler;

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
		new Shop(Bukkit.getWorlds().get(0));
		new SpawnHandler();
		Bukkit.getWorlds().get(0).setGameRuleValue("keepInventory", "true");
	}
	
	@Override
	public void disable() {
		String container = "/root/" + Network.getServerName().toLowerCase() + "/";
		String name = "kitpvp";
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/" + name));
		FileHandler.copyFolder(new File("/root/resources/maps/" + name), new File(container + "/" + name));
	}
}
