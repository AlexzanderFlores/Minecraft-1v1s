package ostb.server.servers.building;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.player.MouseClickEvent;
import ostb.gameapi.games.pvpbattles.Armory;
import ostb.gameapi.games.pvpbattles.Shop;
import ostb.player.CoinsHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;

public class Building extends ProPlugin {
	public Building() {
		super("Building");
		addGroup("24/7");
		new Events();
		removeFlags();
		setAllowLeavesDecay(false);
		setAllowDefaultMobSpawning(false);
		new ostb.gameapi.games.pvpbattles.Events();
		new CoinsHandler(DB.PLAYERS_COINS_PVP_BATTLES, Plugins.PVP_BATTLES);
		CoinsHandler.setKillCoins(5);
		CoinsHandler.setWinCoins(20);
		new ArmorStandHelper();
		new CommandBase("setGameSpawn", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/spawns.yml");
				int index = -1;
				if(arguments.length == 0) {
					index = config.getConfig().getKeys(false).size() + 1;
				} else if(arguments.length == 1) {
					try {
						index = Integer.valueOf(arguments[0]);
					} catch(NumberFormatException e) {
						return false;
					}
				}
				String loc = (location.getBlockX() + ".5,") + (location.getBlockY() + 1) + "," + (location.getBlockZ() + ".5,");
				config.getConfig().set(index + "", loc);
				config.save();
				MessageHandler.sendMessage(player, "Set spawn " + index);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("viewArmorStands", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				double radius = 0;
				try {
					radius = Double.valueOf(arguments[0]);
				} catch(NumberFormatException e) {
					return false;
				}
				final List<ArmorStand> nearStands = new ArrayList<ArmorStand>();
				for(Entity entity : player.getNearbyEntities(radius, radius, radius)) {
					if(entity instanceof ArmorStand) {
						ArmorStand armorStand = (ArmorStand) entity;
						if(!armorStand.isVisible()) {
							armorStand.setVisible(true);
							nearStands.add(armorStand);
						}
					}
				}
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						for(ArmorStand armorStand : nearStands) {
							armorStand.setVisible(false);
						}
						nearStands.clear();
					}
				}, 20 * 3);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("test", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 1) {
					Player player = (Player) sender;
					if(arguments[0].equalsIgnoreCase("shop")) {
						new Shop(player.getLocation());
					} else if(arguments[0].equalsIgnoreCase("armory")) {
						new Armory(player.getLocation());
					}
				} else if(arguments.length == 2) {
					String ign = arguments[0];
					String url = "";
					switch(Integer.valueOf(arguments[1])) {
					case 1:
						url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=340&wt=20&abg=240&abd=130&ajg=330&ajd=30&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=186";
						break;
					case 2:
						url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=330&wt=30&abg=310&abd=50&ajg=340&ajd=30&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=727";
						break;
					case 3:
						url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=10&w=330&wt=30&abg=330&abd=110&ajg=350&ajd=10&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=761";
						break;
					default:
						return false;
					}
					FileHandler.downloadImage(url, Bukkit.getWorldContainer().getPath() + "/plugins/test.png");
				}
				return true;
			}
		};
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
	
	//@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		float yaw = event.getTo().getYaw();
		if(yaw < 0) {
			yaw *= -1;
		}
		if(yaw > 360) {
			yaw = 0;
		}
		player.setLevel((int) yaw);
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(player.getInventory().getItemInHand().getType() == Material.SPONGE) {
			player.setVelocity(player.getLocation().getDirection().multiply(5.0d));
		}
	}
}
