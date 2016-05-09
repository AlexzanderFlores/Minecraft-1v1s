package ostb.server.servers.hub.parkous;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import anticheat.util.EventUtil;
import de.slikey.effectlib.util.ParticleEffect;
import ostb.OSTB;
import ostb.player.TitleDisplayer;
import ostb.server.servers.hub.ParkourNPC;
import ostb.server.util.CircleUtil;

@SuppressWarnings("deprecation")
public class Parkour implements Listener {
	private List<String> players = null;
	private List<ArmorStand> armorStands = null;
	
	public Parkour() {
		players = new ArrayList<String>();
		armorStands = new ArrayList<ArmorStand>();
		World world = Bukkit.getWorlds().get(0);
		for(Location location : new Location [] {new Location(world, 1569, 6, -1299), new Location(world, 1533, 17, -1297), new Location(world, 1526, 19, -1299), new Location(world, 1266, 22, -1298)}) {
			final ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			armorStand.setVisible(false);
			armorStand.setHelmet(new ItemStack(Material.NETHERRACK));
			armorStands.add(armorStand);
			new CircleUtil(location, 1, 6) {
				@Override
				public void run(Vector vector, Location location) {
					armorStand.teleport(location);
					ParticleEffect.FLAME.display(location.add(0, 2.20, 0), 15);
				}
			};
		}
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(OSTB.getInstance(), new Runnable() {
			@Override
			public void run() {
				for(ArmorStand armorStand : armorStands) {
					for(Entity entity : armorStand.getNearbyEntities(1, 1, 1)) {
						if(entity instanceof Player) {
							Player player = (Player) entity;
							if(players.contains(player.getName())) {
								player.teleport(ParkourNPC.getCourseLocation());
								player.setFireTicks(20 * 2);
								new TitleDisplayer(player, "&cAvoid the Netherrack!").display();
							}
						}
					}
				}
			}
		}, 1, 1);
		EventUtil.register(this);
	}
}
