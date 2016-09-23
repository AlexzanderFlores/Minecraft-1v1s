package network.server.servers.hub;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import network.Network;
import network.ProPlugin;
import network.player.LevelHandler;
import network.player.TeamScoreboardHandler;
import network.server.ServerLogger;
import network.server.servers.hub.crate.Crate;
import network.server.servers.hub.crate.KeyExchange;
import network.server.servers.hub.crate.KeyFragments;
import network.server.servers.hub.items.Features;
import network.server.servers.hub.items.GameSelector;
import network.server.servers.hub.items.HubSelector;
import network.server.servers.hub.items.Profile;
import network.server.servers.hub.parkours.EndlessParkour;
import network.server.util.FileHandler;

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
		hubNumber = Integer.valueOf(Network.getServerName().replaceAll("[^\\d.]", ""));
		LevelHandler.enable();
		new Events();
		new Crate();
		new KeyExchange();
		new KeyFragments();
		new GameSelector();
		new Features();
		new Profile();
		new HubSelector();
		new ServerLogger();
		new DailyRewards();
		new TeamScoreboardHandler();
		new ParkourNPC();
		new EndlessParkour();
		new RecentSupporters();
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
	
	@Override
	public void disable() {
		String container = Bukkit.getWorldContainer().getPath();
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/spawn"));
		FileHandler.copyFolder(new File("/root/resources/maps/hub"), new File(container + "/spawn"));
		super.disable();
	}
}
