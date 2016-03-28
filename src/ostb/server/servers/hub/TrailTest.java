package ostb.server.servers.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.EventUtil;

public class TrailTest implements Listener {
	private Map<String, List<ArmorStand>> stands = null;
	private int max = 20;
	private Material material = Material.WOOL;
	//private Random random = new Random();

	public TrailTest() {
		stands = new HashMap<String, List<ArmorStand>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if(from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
			Player player = event.getPlayer();
			String name = player.getName();
			Location location = player.getLocation();
			List<ArmorStand> stand = stands.get(name);
			if(stand == null) {
				stand = new ArrayList<ArmorStand>(max);
			}
			ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.add(0, 1, 0), EntityType.ARMOR_STAND);
			armorStand.setVisible(false);
			armorStand.setGravity(false);
			//armorStand.setSmall(true);
			armorStand.setHelmet(new ItemStack(material));
			//armorStand.setHelmet(new ItemStack(material, 1, DyeColor.values()[random.nextInt(DyeColor.values().length)].getData()));
			if(stand.size() >= max) {
				stand.get(0).remove();
				stand.remove(0);
			}
			stand.add(armorStand);
			stands.put(name, stand);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		String name = event.getPlayer().getName();
		if(stands.containsKey(name)) {
			List<ArmorStand> stand = stands.get(name);
			if(stand != null) {
				for(ArmorStand armorStand : stand) {
					armorStand.remove();
				}
				stand.clear();
				stand = null;
			}
			stands.remove(name);
		}
	}
}
