package ostb.server.servers.building;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.SpawnPointHandler;
import ostb.player.MessageHandler;
import ostb.server.CommandBase;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;

@SuppressWarnings("deprecation")
public class SGSpawn implements Listener {
	private List<String> setting = null;
	private List<BlockState> blockStates = null;
	private Map<String, Double> y = null;
	private Map<String, Material> materials = null;
	
	public SGSpawn() {
		setting = new ArrayList<String>();
		blockStates = new ArrayList<BlockState>();
		y = new HashMap<String, Double>();
		materials = new HashMap<String, Material>();
		new CommandBase("setsgspawn", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(setting.contains(player.getName())) {
					remove(player);
					World world = player.getWorld();
					List<Location> spawns = new SpawnPointHandler(world).getSpawns();
					Location center = getCenter(world, spawns);
					player.teleport(center);
					spawns.clear();
					ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/spawns.yml");
					for(String key : config.getConfig().getKeys(false)) {
						String [] location = config.getConfig().getString(key).split(",");
						double x = Double.valueOf(location[0]);
						double y = Double.valueOf(location[1]);
						double z = Double.valueOf(location[2]);
						Location loc = new Location(world, x, y, z);
						Location newLocation = loc.setDirection(center.toVector().subtract(loc.toVector()));
						String locString = (((int) newLocation.getX()) + ".5,") + (newLocation.getBlockY() + 1) + "," + (((int) newLocation.getZ()) + ".5," + newLocation.getYaw() + "," + newLocation.getPitch());
						config.getConfig().set(key + "", locString);
						spawns.add(newLocation);
					}
					config.save();
				} else {
					FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/spawns.yml"));
					setting.add(player.getName());
					y.put(player.getName(), player.getLocation().getY());
					materials.put(player.getName(), player.getLocation().add(0, -1, 0).getBlock().getType());
					MessageHandler.sendMessage(player, "Enabled SG spawn setting, walk around the spawn stands");
					MessageHandler.sendMessage(player, "Y = " + player.getLocation().getY());
					MessageHandler.sendMessage(player, "Material = " + materials.get(player.getName()).toString());
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	private void set(Player player) {
		Location location = player.getLocation();
		if(location.getY() != y.get(player.getName())) {
			return;
		}
		Block block = location.add(0, -1, 0).getBlock();
		if(materials.get(player.getName()) != block.getType()) {
			return;
		}
		blockStates.add(block.getState());
		block.setType(Material.BARRIER);
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/spawns.yml");
		int index = -1;
		index = config.getConfig().getKeys(false).size() + 1;
		Bukkit.getLogger().info(location.toString());
		String loc = (((int) location.getX()) + ".5,") + (location.getBlockY() + 1) + "," + (((int) location.getZ()) + ".5");
		config.getConfig().set(index + "", loc);
		config.save();
		MessageHandler.sendMessage(player, "Set spawn " + index);
		EffectUtil.playSound(player, Sound.CLICK);
	}
	
	private void remove(Player player) {
		if(setting.contains(player.getName())) {
			for(BlockState state : blockStates) { 
				state.getBlock().setType(state.getType());
				state.getBlock().setData(state.getData().getData());
			}
			setting.remove(player.getName());
			materials.remove(player.getName());
			MessageHandler.sendMessage(player, "&cDisabled SG spawn setting");
		}
	}
	
	public Location getCenter(World world, List<Location> spawns) {
        double x1 = spawns.get(0).getX();
        double z1 = spawns.get(0).getZ();
        double x2 = spawns.get(12).getX();
        double z2 = spawns.get(12).getZ();
        double x = (spawns.get(0).getX() - x2) / 2;
        double z = (z1 - z2) / 2;
        double centerX = (x1 > x2 ? x1 - x : x2 + x);
        double centerZ = (z1 > z2 ? z1 - z : z2 + z);
        return getGround(new Location(world, centerX, 0, centerZ));
    }
	
	public Location getGround(Location location) {
        location.setY(250);
        while(location.getBlock().getType() == Material.AIR) {
            location.setY(location.getBlockY() - 1);
        }
        return location.add(0, 1, 0);
    }
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(setting.contains(player.getName())) {
			set(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
