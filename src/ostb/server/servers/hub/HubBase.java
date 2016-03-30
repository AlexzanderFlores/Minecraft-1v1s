package ostb.server.servers.hub;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.player.LevelHandler;
import ostb.server.ServerLogger;
import ostb.server.servers.hub.crate.Crate;
import ostb.server.servers.hub.items.Features;
import ostb.server.servers.hub.items.GameSelector;
import ostb.server.servers.hub.items.HubSelector;
import ostb.server.servers.hub.items.Notifications;
import ostb.server.servers.hub.items.Profile;
import ostb.server.servers.hub.items.Shop;

public class HubBase extends ProPlugin {
	//private static List<ArmorStand> stands = new ArrayList<ArmorStand>();
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
				if((entity instanceof Item || entity instanceof LivingEntity || entity instanceof ArmorStand) && !(entity instanceof Player)) {
					entity.remove();
				}
			}
		}
		hubNumber = Integer.valueOf(OSTB.getServerName().replaceAll("[^\\d.]", ""));
		LevelHandler.enable();
		new Events();
		new Flag();
		new Crate();
		new GameSelector();
		new Features();
		new Shop();
		new Profile();
		new Notifications();
		new HubSelector();
		new ServerLogger();
		new Tutorial();
		new RewardVillager();
	}
	
	public static int getHubNumber() {
		return hubNumber;
	}
}
