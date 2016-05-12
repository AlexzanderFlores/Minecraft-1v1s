package ostb.server.servers.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.events.TimeEvent;
import anticheat.util.AsyncDelayedTask;
import npc.util.DelayedTask;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.DB.Databases;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ImageMap;
import ostb.server.util.StringUtil;

public class RecentSupporters implements Listener {
	private List<ItemFrame> itemFrames = null;
	private List<ArmorStand> nameStands = null;
	private List<ArmorStand> packageStands = null;
	
	public RecentSupporters() {
		itemFrames = new ArrayList<ItemFrame>();
		nameStands = new ArrayList<ArmorStand>();
		packageStands = new ArrayList<ArmorStand>();
		
		World world = Bukkit.getWorlds().get(0);
		
		itemFrames.add(ImageMap.getItemFrame(world, 1679, 14, -1301));
		itemFrames.add(ImageMap.getItemFrame(world, 1683, 14, -1301));
		itemFrames.add(ImageMap.getItemFrame(world, 1687, 14, -1301));
		
		nameStands.add((ArmorStand) world.spawnEntity(itemFrames.get(0).getLocation().clone().add(1, -6.5, 0), EntityType.ARMOR_STAND));
		nameStands.add((ArmorStand) world.spawnEntity(itemFrames.get(1).getLocation().clone().add(1, -6.5, 0), EntityType.ARMOR_STAND));
		nameStands.add((ArmorStand) world.spawnEntity(itemFrames.get(2).getLocation().clone().add(1, -6.5, 0), EntityType.ARMOR_STAND));
		for(ArmorStand armorStand : nameStands) {
			setUpArmorStand(armorStand);
		}
		
		packageStands.add((ArmorStand) world.spawnEntity(nameStands.get(0).getLocation().clone().add(0, -.35, 0), EntityType.ARMOR_STAND));
		packageStands.add((ArmorStand) world.spawnEntity(nameStands.get(1).getLocation().clone().add(0, -.35, 0), EntityType.ARMOR_STAND));
		packageStands.add((ArmorStand) world.spawnEntity(nameStands.get(2).getLocation().clone().add(0, -.35, 0), EntityType.ARMOR_STAND));
		for(ArmorStand armorStand : packageStands) {
			setUpArmorStand(armorStand);
		}
		
		update();
		EventUtil.register(this);
		
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				new CommandBase("test") {
					@Override
					public boolean execute(CommandSender sender, String [] arguments) {
						update();
						return true;
					}
				}.setRequiredRank(Ranks.OWNER);
			}
		}, 20 * 2);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60) {
			update();
		}
	}
	
	private void update() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				MessageHandler.alert("Updating...");
				List<UUID> uuids = new ArrayList<UUID>();
				List<String> packageNames = new ArrayList<String>();
				List<String> names = new ArrayList<String>();
				
				ResultSet resultSet = null;
				try {
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT uuid,package FROM recent_supporters ORDER BY id DESC LIMIT 3").executeQuery();
					while(resultSet.next()) {
						uuids.add(UUID.fromString(resultSet.getString("uuid")));
						packageNames.add(resultSet.getString("package"));
					}
				} catch(SQLException e) {
					Bukkit.getLogger().info(e.getMessage());
				} finally {
					DB.close(resultSet);
				}
				
				for(UUID uuid : uuids) {
					names.add(AccountHandler.getName(uuid));
				}
				for(int a = 0; a < 3; ++a) {
					new ImageMap(itemFrames.get(a), loadImage(names.get(a), a), 3, 4);
					nameStands.get(a).setCustomName(StringUtil.color("&b" + names.get(a)));
					packageStands.get(a).setCustomName(StringUtil.color("&b" + packageNames.get(a)));
				}
				
				uuids.clear();
				uuids = null;
				packageNames.clear();
				packageNames = null;
				names.clear();
				names = null;
				
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						MessageHandler.alert("Image Map Count: " + ImageMap.getImageMaps().size());
						for(ImageMap map : ImageMap.getImageMaps()) {
							map.execute();
						}
					}
				}, 20);
			}
		}, 20 * 3);
	}
	
	private void setUpArmorStand(ArmorStand armorStand) {
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
	}
	
	private String loadImage(String ign, int index) {
		String url = "";
		switch(index) {
		case 0:
			url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=340&wt=20&abg=240&abd=130&ajg=330&ajd=30&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=186";
			break;
		case 1:
			url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=330&wt=30&abg=310&abd=50&ajg=340&ajd=30&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=727";
			break;
		case 2:
			url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=10&w=330&wt=30&abg=330&abd=110&ajg=350&ajd=10&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=761";
			break;
		default:
			return null;
		}
		String path = Bukkit.getWorldContainer().getPath() + "/plugins/" + index + ".png";
		FileHandler.downloadImage(url, path);
		return path;
	}
}
