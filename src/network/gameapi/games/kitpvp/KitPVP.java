package network.gameapi.games.kitpvp;

import java.io.File;

import org.bukkit.Bukkit;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.gameapi.TemporaryFireUtil;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.games.kitpvp.shop.Shop;
import network.player.CoinsHandler;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.server.DB;
import network.server.ServerLogger;
import network.server.util.FileHandler;

public class KitPVP extends ProPlugin {
	public KitPVP() {
		super("KitPVP");
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
		new Shop();
		new SpawnHandler();
		new CoinsHandler(DB.PLAYERS_COINS_KIT_PVP, Plugins.KITPVP.getData());
		CoinsHandler.setKillCoins(5);
		CoinsHandler.setWinCoins(25);
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
