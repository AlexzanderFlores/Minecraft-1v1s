package ostb.server.servers.building;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;

@SuppressWarnings("deprecation")
public class Building extends ProPlugin {
	public Building() {
		super("Building");
		addGroup("24/7");
		new Events();
		removeFlags();
		setAllowLeavesDecay(false);
		setAllowDefaultMobSpawning(false);
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
				Bukkit.getLogger().info(location.toString());
				String loc = (((int) location.getX()) + ".5,") + (location.getBlockY() + 1) + "," + (((int) location.getZ()) + ".5," + location.getYaw() + ",0.0");
				config.getConfig().set(index + "", loc);
				config.save();
				MessageHandler.sendMessage(player, "Set spawn " + index);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("kitpvp", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				float yaw = location.getYaw();
				float pitch = location.getPitch();
				if(arguments.length == 1) {
					String action = arguments[0];
					if(action.equalsIgnoreCase("setShop")) {
						String target = action.toLowerCase().replace("set", "");
						ConfigurationUtil config = getConfig(player, target, Plugins.KITPVP);
						int index = config.getConfig().getKeys(false).size();
						config.getConfig().set(index + ".x", x + .5);
						config.getConfig().set(index + ".y", y + 1);
						config.getConfig().set(index + ".z", z + .5);
						config.getConfig().set(index + ".yaw", yaw);
						config.getConfig().set(index + ".pitch", pitch);
						if(config.save()) {
							MessageHandler.sendMessage(player, "Set " + target);
						} else {
							MessageHandler.sendMessage(player, "&cError on saving config file");
						}
						return true;
					}
				} else if(arguments.length == 2) {
					String action = arguments[0];
					if(action.equalsIgnoreCase("setSpawns")) {
						String team = arguments[1].toLowerCase();
						if(!team.equals("red") && !team.equals("blue")) {
							MessageHandler.sendMessage(player, "&cUnknown team \"&e" + team + "&c\". Use \"&ered&c\" or \"&eblue&c\"");
							return true;
						}
						String target = action.toLowerCase().replace("set", "");
						ConfigurationUtil config = getConfig(player, target, Plugins.KITPVP);
						config.getConfig().set(team + ".x", x + .5);
						config.getConfig().set(team + ".y", y + 1);
						config.getConfig().set(team + ".z", z + .5);
						config.getConfig().set(team + ".yaw", yaw);
						config.getConfig().set(team + ".pitch", pitch);
						if(config.save()) {
							MessageHandler.sendMessage(player, "Set " + target);
						} else {
							MessageHandler.sendMessage(player, "&cError on saving config file");
						}
						return true;
					}
				}
				MessageHandler.sendMessage(player, "&f/kitpvp setShop");
				MessageHandler.sendMessage(player, "&f/kitpvp setSpawns <red | blue>");
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("dom", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				int x = location.getBlockX();
				int y = location.getBlockY();
				int z = location.getBlockZ();
				float yaw = location.getYaw();
				float pitch = location.getPitch();
				if(arguments.length == 1) {
					String action = arguments[0];
					if(action.equalsIgnoreCase("setRespawnLoc")) {
						String target = action.toLowerCase().replace("set", "");
						ConfigurationUtil config = getConfig(player, target, Plugins.DOM);
						config.getConfig().set("x", x + .5);
						config.getConfig().set("y", y + 1);
						config.getConfig().set("z", z + .5);
						config.getConfig().set("yaw", yaw);
						config.getConfig().set("pitch", pitch);
						if(config.save()) {
							MessageHandler.sendMessage(player, "Set " + target);
						} else {
							MessageHandler.sendMessage(player, "&cError on saving config file");
						}
						return true;
					}
				} else if(arguments.length == 2) {
					String action = arguments[0];
					String team = arguments[1].toLowerCase();
					if(!action.equalsIgnoreCase("setCP") && !team.equals("red") && !team.equals("blue")) {
						MessageHandler.sendMessage(player, "&cUnknown team, please use \"&ered&c\" or \"&eblue&c\"");
						return true;
					}
					if(action.equalsIgnoreCase("setEnchant") || action.equalsIgnoreCase("setAnvil")) {
						String target = action.toLowerCase().replace("set", "");
						Block block = getRegionBlock(player);
						if(block != null) {
							if(block.getType() != Material.AIR) {
								block.setType(Material.AIR);
								MessageHandler.sendMessage(player, "&7Note: Setting block to air, plugin will place block in game");
							}
							ConfigurationUtil config = getConfig(player, target, Plugins.DOM);
							config.getConfig().set(team + ".x", block.getX());
							config.getConfig().set(team + ".y", block.getY());
							config.getConfig().set(team + ".z", block.getZ());
							if(config.save()) {
								MessageHandler.sendMessage(player, "Set " + target + " for the " + team + " team");
							} else {
								MessageHandler.sendMessage(player, "&cError on saving config file");
							}
						}
						return true;
					} else if(action.equalsIgnoreCase("setShop") || action.equalsIgnoreCase("setArmory") || action.equalsIgnoreCase("setSpawn")) {
						String target = action.toLowerCase().replace("set", "");
						ConfigurationUtil config = getConfig(player, target, Plugins.DOM);
						config.getConfig().set(team + ".x", x + .5);
						config.getConfig().set(team + ".y", y + 1);
						config.getConfig().set(team + ".z", z + .5);
						config.getConfig().set(team + ".yaw", yaw);
						config.getConfig().set(team + ".pitch", pitch);
						if(config.save()) {
							MessageHandler.sendMessage(player, "Set " + target + " for the " + team + " team");
						} else {
							MessageHandler.sendMessage(player, "&cError on saving config file");
						}
						return true;
					} else if(action.equalsIgnoreCase("setCP")) {
						try {
							int index = Integer.valueOf(team);
							ConfigurationUtil config = getConfig(player, "command_posts", Plugins.DOM);
							config.getConfig().set(team + ".x", x + .5);
							config.getConfig().set(team + ".y", y);
							config.getConfig().set(team + ".z", z + .5);
							if(config.save()) {
								MessageHandler.sendMessage(player, "Set command post #" + index);
							} else {
								MessageHandler.sendMessage(player, "&cError on saving config file");
							}
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(player, "&f/dom setCP [index]");
						}
						return true;
					}
				}
				MessageHandler.sendMessage(player, "&f/dom setEnchant <red | blue>");
				MessageHandler.sendMessage(player, "&f/dom setAnvil <red | blue>");
				MessageHandler.sendMessage(player, "&f/dom setSpawn <red | blue> [index]");
				MessageHandler.sendMessage(player, "&f/dom setShop <red | blue>");
				MessageHandler.sendMessage(player, "&f/dom setArmory <red | blue>");
				MessageHandler.sendMessage(player, "&f/dom setCP <index>");
				MessageHandler.sendMessage(player, "&f/dom setRespawnLoc");
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
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
	
	private ConfigurationUtil getConfig(Player player, String name, Plugins plugin) {
		return new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/" + plugin.getData() + "/" + name + ".yml");
	}
	
	private Block getRegionBlock(Player player) {
		Region region = getRegion(player);
		if(region == null) {
			MessageHandler.sendMessage(player, "&cPlease use WorldEdit to select a one block region");
		} else if(region.getArea() != 1) {
			MessageHandler.sendMessage(player, "&cYour region must only be one block");
		} else {
			int x = region.getMinimumPoint().getBlockX();
			int y = region.getMinimumPoint().getBlockY();
			int z = region.getMinimumPoint().getBlockZ();
			return player.getWorld().getBlockAt(x, y, z);
		}
		return null;
	}
	
	public static Region getRegion(Player player) {
		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		if(worldEdit == null || !worldEdit.isEnabled()) {
			MessageHandler.sendMessage(player, "&cWorld Edit is not enabled");
		} else {
			try {
				Region region = worldEdit.getSession(player).getRegion();
				return region;
			} catch (IncompleteRegionException e) {
				
			}
		}
		return null;
	}
}
