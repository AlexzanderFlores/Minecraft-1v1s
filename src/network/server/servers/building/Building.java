package network.server.servers.building;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

import network.Network;
import network.ProPlugin;
import network.Network.Plugins;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.util.ConfigurationUtil;
import network.server.util.FileHandler;

@SuppressWarnings("deprecation")
public class Building extends ProPlugin {
	public Building() {
		super("Building");
		for(int a = 0; a < 10; ++a) Bukkit.getLogger().info("Building()");
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
		new CommandBase("world", 1, 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments[0].equalsIgnoreCase("tp")) {
					if(arguments.length != 2) {
						MessageHandler.sendMessage(sender, "&f/world tp <world name>");
						return true;
					}
					if(!(sender instanceof Player)) {
						MessageHandler.sendUnknownCommand(sender);
						return true;
					}
					String target = arguments[1];
					World world = Bukkit.getWorld(target);
					if(world == null) {
						File worldFile = new File("/root/" + Network.getServerName().toLowerCase() + "/" + arguments[1]);
						if(worldFile.exists() && worldFile.isDirectory() && new File(worldFile.getAbsolutePath() + "/uid.dat").exists()) {
							world = Bukkit.createWorld(new WorldCreator(arguments[1]));
							if(world == null) {
								MessageHandler.sendMessage(sender, "&cFailed to import " + arguments[1]);
							} else {
								if(sender instanceof Player) {
									Player player = (Player) sender;
									player.teleport(world.getSpawnLocation());
								}
								MessageHandler.sendMessage(sender, "\"" + arguments[1] + "\" imported");
							}
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[1] + " is not a known world, these are worlds:");
							String path = "/root/" + Network.getServerName().toLowerCase() + "/";
							File dir = new File(path);
							for(String fileName : dir.list()) {
								File file = new File(fileName);
								if(file.isDirectory() && new File(file.getAbsolutePath() + "/uid.dat").exists()) {
									MessageHandler.sendMessage(sender, fileName);
								}
							}
						}
					}
					if(world != null) {
						MessageHandler.sendMessage(sender, "Teleporting to \"" + world.getName() + "\"'s spawn...");
						Player player = (Player) sender;
						player.teleport(world.getSpawnLocation());
					}
				} else if(arguments[0].equalsIgnoreCase("list")) {
					if(arguments.length != 1) {
						MessageHandler.sendMessage(sender, "&f/world list");
						return true;
					}
					for(World importedWorld : Bukkit.getWorlds()) {
						MessageHandler.sendMessage(sender, importedWorld.getName());
					}
				} else if(arguments[0].equalsIgnoreCase("setSpawn")) {
					if(sender instanceof Player) {
						if(arguments.length != 1) {
							MessageHandler.sendMessage(sender, "&f/world setSpawn");
							return true;
						}
						Player player = (Player) sender;
						World world = player.getWorld();
						Location location = player.getLocation();
						world.setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
						MessageHandler.sendMessage(sender, "Set the spawn point for \"" + world.getName() + "\"");
					} else {
						MessageHandler.sendPlayersOnly(sender);
					}
				} else if(arguments[0].equalsIgnoreCase("create")) {
					if(arguments.length != 3) {
						MessageHandler.sendMessage(sender, "&f/world create <name> <type>");
						return true;
					}
					World world = null;
					String name = arguments[1];
					String type = arguments[2].toLowerCase();
					if(type.equals("normal")) {
						world = Bukkit.createWorld(new WorldCreator(name));
					} else if(type.equals("nether")) {
						world = Bukkit.createWorld(new WorldCreator(name).environment(Environment.NETHER));
					} else if(type.equals("end")) {
						world = Bukkit.createWorld(new WorldCreator(name).environment(Environment.THE_END));
					} else if(type.equals("flat")) {
						world = Bukkit.createWorld(new WorldCreator(name).type(WorldType.FLAT));
					} else if(type.equals("void")) {
						File worldFolder = new File("/root/" + Network.getServerName().toLowerCase() + "/void");
						if(worldFolder.exists() && worldFolder.isDirectory() && !new File(worldFolder.getAbsolutePath() + "/uid.dat").exists()) {
							String path = worldFolder.getAbsolutePath();
							FileHandler.copyFolder(worldFolder, new File(path.subSequence(0, path.length() - 4) + name));
							world = Bukkit.createWorld(new WorldCreator(name));
						} else {
							MessageHandler.sendMessage(sender, "&cFailed to create void world: No void world on box (Report this to Leet)");
						}
					} else {
						MessageHandler.sendMessage(sender, "&c\"" + type + "\" is not a known world type, known world types are:");
						MessageHandler.sendMessage(sender, "normal");
						MessageHandler.sendMessage(sender, "nether");
						MessageHandler.sendMessage(sender, "end");
						MessageHandler.sendMessage(sender, "flat");
						MessageHandler.sendMessage(sender, "void");
					}
					if(world != null) {
						if(sender instanceof Player) {
							Player player = (Player) sender;
							player.teleport(world.getSpawnLocation());
						}
						MessageHandler.sendMessage(sender, "Created world \"" + name + "\" (" + type + ")");
					}
				} else if(arguments[0].equalsIgnoreCase("delete")) {
					if(arguments.length != 2) {
						MessageHandler.sendMessage(sender, "&f/world delete <world name>");
						return true;
					}
					
				} else {
					return false;
				}
				return true;
			}
		};
	}
	
	@Override
	public void disable() {
		for(int a = 0; a < 10; ++a) Bukkit.getLogger().info("disable()");
		String name = Network.getServerName().toLowerCase();
		FileHandler.delete(new File("/root/" + name + "/plugins/Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File("/root/" + name + "/plugins/Essentials/userdata"));
		super.disable();
	}
	
	private ConfigurationUtil getConfig(Player player, String name, Plugins plugin) {
		return new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/" + plugin.getData() + "/" + name + ".yml");
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