package ostb.server.servers.building;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.KitSelection;
import ostb.gameapi.games.pvpbattles.kits.Bomber;
import ostb.gameapi.games.pvpbattles.kits.Default;
import ostb.gameapi.games.pvpbattles.kits.Healer;
import ostb.gameapi.games.pvpbattles.kits.Ninja;
import ostb.gameapi.games.pvpbattles.kits.Tracker;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;

public class Building extends ProPlugin {
	private KitSelection selection = null;
	private ArmorStand armorStand = null;
	
	public Building() {
		super("Building");
		addGroup("24/7");
		new Events();
		removeFlags();
		setAllowLeavesDecay(false);
		setAllowDefaultMobSpawning(false);
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
		new CommandBase("test", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					if(selection == null) {
						selection = new KitSelection(player);
					} else {
						selection.delete();
						selection = null;
						return true;
					}
					selection.update();
				} else {
					String cmd = arguments[0];
					if(cmd.equalsIgnoreCase("spawn")) {
						if(armorStand != null) {
							armorStand.remove();
							armorStand = null;
						}
						armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
						armorStand.setArms(true);
					} else if(cmd.equalsIgnoreCase("remove") && armorStand != null) {
						armorStand.remove();
						armorStand = null;
					} else if(cmd.equalsIgnoreCase("leftArm")) {
						armorStand.setLeftArmPose(new EulerAngle(Double.valueOf(arguments[1]), Double.valueOf(arguments[2]), Double.valueOf(arguments[3])));
					} else if(cmd.equalsIgnoreCase("rightArm")) {
						armorStand.setRightArmPose(new EulerAngle(Double.valueOf(arguments[1]), Double.valueOf(arguments[2]), Double.valueOf(arguments[3])));
					}
				}
				return true;
			}
		};
		new Default();
		new Bomber();
		new Ninja();
		new Healer();
		new Tracker();
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
}
