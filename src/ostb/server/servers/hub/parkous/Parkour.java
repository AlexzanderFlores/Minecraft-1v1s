package ostb.server.servers.hub.parkous;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import anticheat.util.EventUtil;
import de.slikey.effectlib.util.ParticleEffect;
import npc.util.DelayedTask;
import ostb.OSTB;
import ostb.customevents.TimeEvent;
import ostb.player.TitleDisplayer;
import ostb.server.servers.hub.ParkourNPC;
import ostb.server.util.CircleUtil;
import ostb.server.util.EffectUtil;

@SuppressWarnings("deprecation")
public class Parkour implements Listener {
	private List<String> players = null;
	private List<Location> leftCannons = null;
	private List<Location> rightCannons = null;
	private List<Chunk> chunks = null;
	private Map<Integer, Location> woolBlocks = null;
	private Map<ArmorStand, CircleUtil> armorStands = null;
	private Map<Squid, Boolean> squids = null;
	private final double range = .15;
	private final double speed = 1.5;
	private int squidCounter = -1;
	
	public Parkour() {
		players = new ArrayList<String>();
		leftCannons = new ArrayList<Location>();
		rightCannons = new ArrayList<Location>();
		chunks = new ArrayList<Chunk>();
		woolBlocks = new HashMap<Integer, Location>();
		armorStands = new HashMap<ArmorStand, CircleUtil>();
		squids = new HashMap<Squid, Boolean>();
		World world = Bukkit.getWorlds().get(0);
		leftCannons.add(new Location(world, 1495, 21, -1281));
		leftCannons.add(new Location(world, 1490, 20, -1282));
		leftCannons.add(new Location(world, 1485, 19, -1283));
		leftCannons.add(new Location(world, 1480, 19, -1282));
		leftCannons.add(new Location(world, 1474, 21, -1281));
		leftCannons.add(new Location(world, 1468, 22, -1280));
		rightCannons.add(new Location(world, 1495, 21, -1318));
		rightCannons.add(new Location(world, 1490, 20, -1317));
		rightCannons.add(new Location(world, 1485, 19, -1316));
		rightCannons.add(new Location(world, 1480, 19, -1317));
		rightCannons.add(new Location(world, 1474, 21, -1318));
		rightCannons.add(new Location(world, 1468, 22, -1319));
		for(int a = 0, x = 1444; x <= 1459; ++a, ++x) {
			woolBlocks.put(a, new Location(world, x, 20, -1299));
		}
		spawnArmorStands(-1);
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(OSTB.getInstance(), new Runnable() {
			@Override
			public void run() {
				for(ArmorStand armorStand : armorStands.keySet()) {
					for(Entity entity : armorStand.getNearbyEntities(range, range, range)) {
						if(entity instanceof Player) {
							Player player = (Player) entity;
							if(!players.contains(player.getName())) {
								player.teleport(ParkourNPC.getCourseLocation());
								player.setFireTicks(20 * 2);
								player.damage(0);
								new TitleDisplayer(player, "&cAvoid the Netherrack!").display();
							}
						}
					}
				}
				try {
					Iterator<Squid> iterator = squids.keySet().iterator();
					while(iterator.hasNext()) {
						Squid squid = iterator.next();
						if(squid.getTicksLived() >= 20 * 5 || squid.getVehicle() == null || !(squid.getVehicle() instanceof ArmorStand)) {
							if(squid.getVehicle() != null) {
								squid.getVehicle().remove();
							}
							iterator.remove();
							squid.remove();
						} else {
							double range = 2;
							for(Entity entity : squid.getNearbyEntities(range, range, range)) {
								if(entity instanceof Player) {
									Player player = (Player) entity;
									if(!players.contains(player.getName())) {
										player.teleport(ParkourNPC.getCourseLocation());
										player.setFireTicks(20 * 2);
										player.damage(0);
										new TitleDisplayer(player, "&cAvoid the Squids!").display();
									}
								}
							}
							ArmorStand armorStand = (ArmorStand) squid.getVehicle();
							Location location = armorStand.getLocation().add(0, 0, squids.get(squid) ? -speed : speed);
							squid.leaveVehicle();
							armorStand.teleport(location);
							armorStand.setPassenger(squid);
						}
					}
				} catch(ConcurrentModificationException e) {
					
				}
			}
		}, 1, 1);
		EventUtil.register(this);
	}
	
	private void spawnArmorStands(int delay) {
		World world = Bukkit.getWorlds().get(0);
		for(ArmorStand armorStand : armorStands.keySet()) {
			armorStands.get(armorStand).delete();
			armorStand.remove();
		}
		armorStands.clear();
		Random random = new Random();
		for(final Location location : new Location [] {new Location(world, 1569, 6, -1299), new Location(world, 1533, 16, -1296.5), new Location(world, 1526, 18, -1298.5)}) {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					location.getChunk().load(true);
					final ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
					armorStand.setGravity(false);
					armorStand.setVisible(false);
					armorStand.setHelmet(new ItemStack(Material.NETHERRACK));
					armorStands.put(armorStand, new CircleUtil(location, 1, 6) {
						@Override
						public void run(Vector vector, Location location) {
							if(!chunks.contains(location.getChunk())) {
								chunks.add(location.getChunk());
								location.getChunk().load(true);
							}
							armorStand.teleport(location);
							ParticleEffect.FLAME.display(location.add(0, 2.20, 0), 15);
						}
					});
				}
			}, delay == -1 ? random.nextInt(20 * 3) + 1 : delay);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.PHYSICAL && !players.contains(event.getPlayer().getName())) {
			Location location = event.getPlayer().getLocation();
			int x = location.getBlockX();
			int y = location.getBlockY();
			int z = location.getBlockZ();
			if(x == 1498 && y == 19 && z == -1301) {
				squidCounter = 15;
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 10) {
			Random random = new Random();
			int index = random.nextInt(woolBlocks.size());
			Location location = woolBlocks.get(index);
			int z = location.getBlockZ();
			if(z == -1298) {
				--z;
			} else if(z == -1299) {
				if(random.nextBoolean()) {
					--z;
				} else {
					++z;
				}
			} else if(z == -1300) {
				++z;
			}
			Block block = location.getBlock();
			block.setType(Material.AIR);
			block.setData((byte) 0);
			location.setZ(z);
			block = location.getBlock();
			block.setType(Material.WOOL);
			block.setData((byte) random.nextInt(16));
			woolBlocks.put(index, block.getLocation());
		} else if(ticks == 20 * 2 && --squidCounter > 0) {
			Random random = new Random();
			float volume = 2.5f;
			if(random.nextBoolean()) {
				Location leftLoc = leftCannons.get(random.nextInt(leftCannons.size()));
				EffectUtil.playSound(Sound.EXPLODE, leftLoc, volume);
				Squid leftSquid = (Squid) leftLoc.getWorld().spawnEntity(leftLoc, EntityType.SQUID);
				leftSquid.setFireTicks(999999999);
				ArmorStand armorStand = (ArmorStand) leftLoc.getWorld().spawnEntity(leftLoc, EntityType.ARMOR_STAND);
				armorStand.setGravity(false);
				armorStand.setVisible(false);
				armorStand.setPassenger(leftSquid);
				squids.put(leftSquid, true);
			} else {
				Location rightLoc = rightCannons.get(random.nextInt(rightCannons.size()));
				EffectUtil.playSound(Sound.EXPLODE, rightLoc, volume);
				Squid rightSquid = (Squid) rightLoc.getWorld().spawnEntity(rightLoc, EntityType.SQUID);
				rightSquid.setFireTicks(999999999);
				ArmorStand armorStand = (ArmorStand) rightLoc.getWorld().spawnEntity(rightLoc, EntityType.ARMOR_STAND);
				armorStand.setGravity(false);
				armorStand.setVisible(false);
				armorStand.setPassenger(rightSquid);
				squids.put(rightSquid, false);
			}
		} else if(ticks == 20 * 60) {
			spawnArmorStands(1);
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		if(chunks.contains(event.getChunk())) {
			event.setCancelled(false);
		}
	}
}
