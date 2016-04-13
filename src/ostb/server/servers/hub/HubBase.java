package ostb.server.servers.hub;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.crates.HardcoreEliminationCrate;
import ostb.gameapi.crates.SkyWarsCrate;
import ostb.player.LevelHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.ServerLogger;
import ostb.server.servers.hub.crate.Beacon;
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
		new KeyExchange();
		new KeyFragments();
		new GameSelector();
		new Features();
		new Shop();
		new Profile();
		new Notifications();
		new HubSelector();
		new ServerLogger();
		new Tutorial();
		new DailyRewards();
		new ParkourNPC();
		new EndlessParkour();
		new CommandBase("giveKey", 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				int amount = Integer.valueOf(arguments[1]);
				String type = arguments[2];
				if(type.equalsIgnoreCase("voting")) {
					Beacon.giveKey(uuid, amount, arguments[2]);
				} else if(type.equalsIgnoreCase("sky_wars")) {
					SkyWarsCrate.giveKey(uuid, amount);
				} else if(type.equalsIgnoreCase("hardcore_elimination")) {
					HardcoreEliminationCrate.giveKey(uuid, amount);
				} else {
					MessageHandler.sendMessage(sender, "Unknown key type, use:");
					MessageHandler.sendMessage(sender, "voting");
					MessageHandler.sendMessage(sender, "sky_wars");
					MessageHandler.sendMessage(sender, "hardcore_elimination");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
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
