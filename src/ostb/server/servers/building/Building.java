package ostb.server.servers.building;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.MouseClickEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;

@SuppressWarnings({"rawtypes", "unchecked"})
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
				String loc = (location.getBlockX() + ".5,") + (location.getBlockY() + 1) + "," + (location.getBlockZ() + ".5,");
				config.getConfig().set(index + "", loc);
				config.save();
				MessageHandler.sendMessage(player, "Set spawn " + index);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("test", 0, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0) {
					Player player = (Player) sender;
					Entity near = getNearestEntityInSight(player, 10);
					if(near == null || !(near instanceof Player)) {
						MessageHandler.sendMessage(player, "&cNo player found in crosshairs");
					} else {
						Player nearPlayer = (Player) near;
						MessageHandler.sendMessage(player, "Player found &b" + nearPlayer.getName());
					}
					//new Campfire(player.getLocation());
					/*Location location = player.getLocation();
					location.setPitch(0.0f);
					float yaw = location.getYaw() - 1;
					if(yaw < - 360) {
						yaw = 0;
					}
					location.setYaw(yaw);
					player.teleport(location);
					if(yaw < 0) {
						yaw *= -1;
					}
					player.setLevel((int) yaw);*/
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
	
	public static Entity getNearestEntityInSight(Player player, int range) {
	    List<Entity> entities = player.getNearbyEntities(range, range, range); //Get the entities within range
	    Iterator<Entity> iterator = entities.iterator(); //Create an iterator
	    while(iterator.hasNext()) {
	        Entity next = iterator.next(); //Get the next entity in the iterator
	        if(!(next instanceof LivingEntity) || next == player) { //If the entity is not a living entity or the player itself, remove it from the list
	            iterator.remove();
	        }
	    }
		List<Block> sight = player.getLineOfSight((Set) null, range); //Get the blocks in the player's line of sight (the Set is null to not ignore any blocks)
	    for(Block block : sight) { //For each block in the list
	        if(block.getType() != Material.AIR) { //If the block is not air -> obstruction reached, exit loop/seach
	            break;
	        }
	        Location low = block.getLocation(); //Lower corner of the block
	        Location high = low.clone().add(1, 1, 1); //Higher corner of the block
	        AxisAlignedBB blockBoundingBox = AxisAlignedBB.a(low.getX(), low.getY(), low.getZ(), high.getX(), high.getY(), high.getZ()); //The bounding or collision box of the block
	        for(Entity entity : entities) { //For every living entity in the player's range
	            //If the entity is truly close enough and the bounding box of the block (1x1x1 box) intersects with the entity's bounding box, return it
	            if(entity.getLocation().distance(player.getEyeLocation()) <= range && ((CraftEntity) entity).getHandle().getBoundingBox().b(blockBoundingBox)) {
	                return entity;
	            }
	        }
	    }
	    return null; //Return null/nothing if no entity was found
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
	
	@EventHandler
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
