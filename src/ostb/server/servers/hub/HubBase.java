package ostb.server.servers.hub;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.player.LevelHandler;
import ostb.player.TeamScoreboardHandler;
import ostb.server.ServerLogger;
import ostb.server.servers.hub.crate.Crate;
import ostb.server.servers.hub.crate.KeyExchange;
import ostb.server.servers.hub.crate.KeyFragments;
import ostb.server.servers.hub.items.Features;
import ostb.server.servers.hub.items.GameSelector;
import ostb.server.servers.hub.items.HubSelector;
import ostb.server.servers.hub.items.Notifications;
import ostb.server.servers.hub.items.Profile;
import ostb.server.servers.hub.items.Shop;
import ostb.server.util.FileHandler;

public class HubBase extends ProPlugin {
	private static int hubNumber = 0;
	
	public HubBase(String name) {
		super(name);
		addGroup("24/7");
		addGroup("hub");
		setAllowItemSpawning(true);
		setAllowInventoryClicking(true);
		for(World world : Bukkit.getWorlds()) {
			world.setGameRuleValue("doDaylightCycle", "false");
			world.setTime(12250);
			for(Entity entity : world.getEntities()) {
				if((entity instanceof Item || entity instanceof LivingEntity) && !(entity instanceof Player)) {
					entity.remove();
				}
			}
		}
		hubNumber = Integer.valueOf(OSTB.getServerName().replaceAll("[^\\d.]", ""));
		LevelHandler.enable();
		new Events();
		new Crate();
		new KeyExchange();
		new KeyFragments();
		new GameSelector();
		new Features();
		new Shop();
		new Profile();
		new Notifications();
		new HubSelector();
		new ServerLogger();
		new DailyRewards();
		//new RecentSupporters();
		new TeamScoreboardHandler();
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
	
	@Override
	public void disable() {
		String container = Bukkit.getWorldContainer().getPath();
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/spawn"));
		FileHandler.copyFolder(new File(container + "/../resources/maps/hub"), new File(container + "/spawn"));
		super.disable();
	}
}
