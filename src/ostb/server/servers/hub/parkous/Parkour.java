package ostb.server.servers.hub.parkous;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import anticheat.events.AsyncPlayerLeaveEvent;
import anticheat.events.PlayerLeaveEvent;
import anticheat.util.EventUtil;
import de.slikey.effectlib.util.ParticleEffect;
import npc.util.DelayedTask;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.servers.hub.Events;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.servers.hub.ParkourNPC;
import ostb.server.servers.hub.parkous.ParkourStartEvent.ParkourTypes;
import ostb.server.util.CircleUtil;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Parkour implements Listener {
	private List<String> players = null;
	private List<Location> leftCannons = null;
	private List<Location> rightCannons = null;
	private List<Chunk> chunks = null;
	private Map<Integer, Location> woolBlocks = null;
	private Map<ArmorStand, CircleUtil> armorStands = null;
	private Map<Squid, Boolean> squids = null;
	private Map<String, Integer> checkpointPasses = null;
	private Map<String, Location> checkpoints = null;
	private Map<String, SidebarScoreboardUtil> scoreboards = null;
	private Map<String, CountDownUtil> timers = null;
	private ItemStack setCheckpoint = null;
	private ItemStack returnToCheckpoint = null;
	private ItemStack exitParkour = null;
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
		checkpointPasses = new HashMap<String, Integer>();
		checkpoints = new HashMap<String, Location>();
		scoreboards = new HashMap<String, SidebarScoreboardUtil>();
		timers = new HashMap<String, CountDownUtil>();
		setCheckpoint = new ItemCreator(Material.BED).setName("&bSet Checkpoint").getItemStack();
		returnToCheckpoint = new ItemCreator(Material.EYE_OF_ENDER).setName("&bReturn to Checkpoint").getItemStack();
		exitParkour = new ItemCreator(Material.WOOD_DOOR).setName("&cExit Parkour").getItemStack();
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
							if(players.contains(player.getName())) {
								died(player);
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
									if(players.contains(player.getName())) {
										died(player);
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
	
	private void start(Player player) {
		if(!players.contains(player.getName())) {
			Bukkit.getPluginManager().callEvent(new ParkourStartEvent(player, ParkourTypes.COURSE));
			players.add(player.getName());
			if(player.getAllowFlight()) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
			player.getInventory().clear();
			player.getInventory().setItem(0, setCheckpoint);
			player.getInventory().setItem(1, returnToCheckpoint);
			player.getInventory().setItem(8, exitParkour);
			Events.removeSidebar(player);
			timers.put(player.getName(), new CountDownUtil());
			SidebarScoreboardUtil sidebar = new SidebarScoreboardUtil(" &aParkour ") {
				@Override
				public void update(Player player) {
					removeScore(5);
					removeScore(2);
					setText(new String [] {
						" ",
						"&eCheckpoints",
						"&b" + (checkpointPasses.containsKey(player.getName()) ? checkpointPasses.get(player.getName()) : 0),
						"  ",
						"&eTime",
						timers.get(player.getName()).getCounterAsString(ChatColor.AQUA),
						"   "
					});
				}
			};
			scoreboards.put(player.getName(), sidebar);
			player.setScoreboard(sidebar.getScoreboard());
			sidebar.update(player);
		}
	}
	
	private void remove(Player player, boolean teleport) {
		String name = player.getName();
		if(players.contains(name)) {
			players.remove(name);
			if(teleport) {
				player.teleport(ParkourNPC.getCourseLocation());
			}
			HubItemBase.giveItems(player);
			if(Ranks.PREMIUM.hasRank(player)) {
				player.setAllowFlight(true);
			}
			if(scoreboards.containsKey(name)) {
				scoreboards.get(name).remove();
				scoreboards.remove(name);
				Events.giveSidebar(player);
			}
		}
	}
	
	private void died(Player player) {
		if(checkpoints.containsKey(player.getName())) {
			player.teleport(checkpoints.get(player.getName()));
		} else {
			player.teleport(ParkourNPC.getCourseLocation());
		}
	}

	@EventHandler
	public void onParkourStart(ParkourStartEvent event) {
		if(event.getType() == ParkourTypes.COURSE) {
			remove(event.getPlayer(), false);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.PHYSICAL && players.contains(event.getPlayer().getName())) {
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
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		ItemStack item = player.getItemInHand();
		if(this.setCheckpoint.equals(item)) {
			int amount = checkpointPasses.containsKey(name) ? checkpointPasses.get(name) : 0;
			if(amount > 0) {
				--amount;
				checkpoints.put(name, player.getLocation());
				new TitleDisplayer(player, "&bCheckpoint Set").display();
			} else {
				new TitleDisplayer(player, "&cNo Checkpoints", "&cGet some with &a/vote").display();
				amount += 10;
			}
			checkpointPasses.put(name, amount);
		} else if(this.returnToCheckpoint.equals(item)) {
			if(checkpoints.containsKey(name)) {
				player.teleport(checkpoints.get(name));
			} else {
				MessageHandler.sendMessage(player, "&cYou have no checkpoint set");
			}
		} else if(this.exitParkour.equals(item)) {
			remove(player, true);
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
		} else if(ticks == 20) {
			for(String name : scoreboards.keySet()) {
				if(players.contains(name)) {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						timers.get(name).incrementCounter();
						scoreboards.get(name).update(player);
					}
				}
			}
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
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		int y = to.getBlockY();
		if(y == 5 || y == 6) {
			int x = to.getBlockX();
			if(x == 1589) {
				int z = to.getBlockZ();
				if(z >= -1300 && z <= -1298) {
					start(event.getPlayer());
				}
			}
		} else if(y <= 0 && players.contains(event.getPlayer().getName())) {
			died(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		if(chunks.contains(event.getChunk())) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		remove(player, false);
		timers.remove(player);
		if(!Ranks.PREMIUM.hasRank(player)) {
			checkpoints.remove(player.getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		int amount = DB.HUB_PARKOUR_CHECKPOINTS.getInt("uuid", player.getUniqueId().toString(), "amount");
		if(amount > 0) {
			checkpointPasses.put(player.getName(), amount);
		} else {
			checkpointPasses.put(player.getName(), 100);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		UUID uuid = event.getUUID();
		if(checkpointPasses.containsKey(name)) {
			DB.HUB_PARKOUR_CHECKPOINTS.updateInt("amount", checkpointPasses.get(name), "uuid", uuid.toString());
			checkpointPasses.remove(name);
		}
		if(checkpoints.containsKey(name)) {
			Location location = checkpoints.get(name);
			double x = location.getBlockX() + .5;
			double y = location.getBlockY() + .5;
			double z = location.getBlockZ() + .5;
			double yaw = (double) ((int) location.getYaw());
			double pitch = (double) ((int) location.getPitch());
			if(DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.isUUIDSet(uuid)) {
				DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.updateDouble("x", x, "uuid", uuid.toString());
				DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.updateDouble("y", y, "uuid", uuid.toString());
				DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.updateDouble("z", z, "uuid", uuid.toString());
				DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.updateDouble("yaw", yaw, "uuid", uuid.toString());
				DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.updateDouble("pitch", pitch, "uuid", uuid.toString());
			} else {
				DB.HUB_PARKOUR_CHECKPOINT_LOCATIONS.insert("'" + uuid.toString() + "', '" + x + "', '" + y + "', '" + z + "', '" + yaw + "', '" + pitch + "'");
			}
			checkpoints.remove(name);
		}
	}
}
