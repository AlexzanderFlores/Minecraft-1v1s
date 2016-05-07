package ostb.server.servers.building;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Building extends ProPlugin {
	private Map<String, Giant> recentGiant = new HashMap<String, Giant>();
	private Map<String, ArmorStand> armorStands = new HashMap<String, ArmorStand>();
	
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
				String loc = (location.getBlockX() + ".5,") + (location.getBlockY() + 1) + "," + (location.getBlockZ() + ".5,");
				config.getConfig().set(index + "", loc);
				config.save();
				MessageHandler.sendMessage(player, "Set spawn " + index);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("pvpBattles", -1, true) {
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
						ConfigurationUtil config = getConfig(player, target);
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
							ConfigurationUtil config = getConfig(player, target);
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
						ConfigurationUtil config = getConfig(player, target);
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
					} else if(action.equalsIgnoreCase("setFlag")) {
						ConfigurationUtil config = getConfig(player, "flag");
						config.getConfig().set(team + ".x", x + .5);
						config.getConfig().set(team + ".y", y);
						config.getConfig().set(team + ".z", z + .5);
						if(config.save()) {
							MessageHandler.sendMessage(player, "Set the " + team + " team flag");
						} else {
							MessageHandler.sendMessage(player, "&cError on saving config file");
						}
						return true;
					} else if(action.equalsIgnoreCase("setCP")) {
						try {
							int index = Integer.valueOf(team);
							ConfigurationUtil config = getConfig(player, "command_posts");
							config.getConfig().set(team + ".x", x + .5);
							config.getConfig().set(team + ".y", y);
							config.getConfig().set(team + ".z", z + .5);
							if(config.save()) {
								MessageHandler.sendMessage(player, "Set command post #" + index);
							} else {
								MessageHandler.sendMessage(player, "&cError on saving config file");
							}
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(player, "&f/pvpBattles setCP [index]");
						}
						return true;
					}
				}
				MessageHandler.sendMessage(player, "&f/pvpBattles setEnchant <red | blue>");
				MessageHandler.sendMessage(player, "&f/pvpBattles setAnvil <red | blue>");
				MessageHandler.sendMessage(player, "&f/pvpBattles setSpawn <red | blue> [index]");
				MessageHandler.sendMessage(player, "&f/pvpBattles setShop <red | blue>");
				MessageHandler.sendMessage(player, "&f/pvpBattles setArmory <red | blue>");
				MessageHandler.sendMessage(player, "&f/pvpBattles setFlag <red | blue>");
				MessageHandler.sendMessage(player, "&f/pvpBattles setCP <index>");
				MessageHandler.sendMessage(player, "&f/pvpBattles setRespawnLoc");
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
				Player player = (Player) sender;
				if(arguments.length == 0) {
					Giant giant = (Giant) player.getWorld().spawnEntity(player.getLocation(), EntityType.GIANT);
					//giant.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 999999999));
					giant.setNoDamageTicks(999999999);
					giant.getEquipment().setItemInHand(player.getItemInHand());
					recentGiant.put(player.getName(), giant);
					ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
					armorStand.setGravity(false);
					//armorStand.setVisible(false);
					armorStand.setPassenger(giant);
					armorStands.put(player.getName(), armorStand);
				}
				if(arguments.length == 1) {
					if(arguments[0].equalsIgnoreCase("enchant")) {
						ItemStack inHand = player.getItemInHand();
						if(inHand != null && inHand.getType() != Material.AIR) {
							ItemStack item = new ItemCreator(inHand).setGlow(true).getItemStack();
							player.setItemInHand(item);
						}
					} else if(arguments[0].equalsIgnoreCase("getDistance")) {
						if(armorStands.containsKey(player.getName())) {
							ArmorStand armorStand = armorStands.get(player.getName());
							if(armorStand == null || armorStand.isDead()) {
								MessageHandler.sendMessage(player, "&cNo Recent Armor Stand");
							} else {
								Location pLoc = player.getLocation();
								Location aLoc = armorStand.getLocation();
								double x = pLoc.getX() - aLoc.getX();
								double y = pLoc.getY() - aLoc.getY();
								double z = pLoc.getZ() - aLoc.getZ();
								MessageHandler.sendMessage(player, "Distance: (" + x + ", " + y + ", " + z + ")");
							}
						} else {
							MessageHandler.sendMessage(player, "&cNo Recent Armor Stand");
						}
					}
				}
				if(arguments.length == 2) {
					player.getInventory().addItem(new ItemCreator(Material.valueOf(arguments[0]), Byte.valueOf(arguments[1])).setGlow(true).getItemStack());
					//Block block = getRegionBlock(player);
					//MessageHandler.sendMessage(player, block.getType() + ":" + block.getData());
				}
				if(arguments.length == 2) {
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
	
	private ConfigurationUtil getConfig(Player player, String name) {
		return new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/pvpbattles/" + name + ".yml");
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
	
	private Region getRegion(Player player) {
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
	
	@EventHandler
	public void onTime(TimeEvent event) {
		super.onTime(event);
		long ticks = event.getTicks();
		if(ticks == 1) {
			for(String name : armorStands.keySet()) {
				Player player = ProPlugin.getPlayer(name);
				if(player != null && (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)) {
					ArmorStand armorStand = armorStands.get(name);
					if(armorStand != null && !armorStand.isDead()) {
						Location pLoc = player.getLocation();
						Location aLoc = armorStand.getLocation();
						int x = (int) (pLoc.getX() - aLoc.getX());
						int y = (int) (pLoc.getY() - aLoc.getY());
						int z = (int) (pLoc.getZ() - aLoc.getZ());
						MessageHandler.sendMessage(player, "Distance: (" + x + ", " + y + ", " + z + ")");
						Giant giant = null;
						if(armorStand.getPassenger() != null) {
							giant = (Giant) armorStand.getPassenger();
							armorStand.eject();
						}
						armorStand.teleport(player.getLocation().add(0, -7, -3));
						if(giant != null) {
							armorStand.setPassenger(giant);
						}
					}
				}
			}
		}
	}
}
