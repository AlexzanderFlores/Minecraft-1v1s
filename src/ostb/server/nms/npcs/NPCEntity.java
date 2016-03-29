package ostb.server.nms.npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_8_R3.EntityLiving;
import ostb.customevents.ServerRestartEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.server.nms.npcs.NPCRegistrationHandler.NPCs;
import ostb.server.nms.npcs.entities.CreeperNPC;
import ostb.server.nms.npcs.entities.SkeletonNPC;
import ostb.server.nms.npcs.entities.SnowmanNPC;
import ostb.server.nms.npcs.entities.VillagerNPC;
import ostb.server.nms.npcs.entities.ZombieNPC;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public abstract class NPCEntity implements Listener {
	private static List<LivingEntity> entities = null;
	private static Map<LivingEntity, NPCEntity> npcEntities = null;
	private static Map<EntityType, Double> nameHeight = null;
	private String name = null;
	private Location starting = null;
	private Location location = null;
	private Location targetView = null;
	//private Location targetPath = null;
	private ItemStack itemStack = null;
	private LivingEntity livingEntity = null;
	private ArmorStand armorStand = null;
	//private boolean pathFinding = false;
	
	public NPCEntity(EntityType entityType, String name, Location location) {
		this(entityType, name, location, new Location(null, 0, 0, 0));
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Material material) {
		this(entityType, name, location, null, material);
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, ItemStack itemStack) {
		this(entityType, name, location, null, itemStack);
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Location target) {
		this(entityType, name, location, target == null || target.getWorld() == null ? null : target, Material.AIR);
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Location target, Material material) {
		this(entityType, name, location, target, new ItemStack(material));
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Location targetView, ItemStack itemStack) {
		if(nameHeight == null) {
			nameHeight = new HashMap<EntityType, Double>();
			nameHeight.put(EntityType.CREEPER, 2.0);
			nameHeight.put(EntityType.SILVERFISH, 1.0);
		}
		NPCs.valueOf(entityType.toString()).register();
		this.name = name == null ? "" : name;
		this.starting = location.clone();
		this.location = location;
		//targetPath = location;
		this.targetView = targetView;
		this.itemStack = itemStack;
		EventUtil.register(this);
		location.getChunk().load();
		location.getWorld().spawnEntity(location, entityType);
		CreatureSpawnEvent.getHandlerList().unregister(this);
	}
	
	public static boolean isNPC(LivingEntity livingEntity) {
		return entities != null && entities.contains(livingEntity);
	}
	
	public static NPCEntity getNPC(LivingEntity livingEntity) {
		if(npcEntities == null) {
			return null;
		} else {
			return npcEntities.get(livingEntity);
		}
	}
	
	public void remove() {
		npcEntities.remove(livingEntity);
		if(livingEntity != null) {
			livingEntity.remove();
			livingEntity = null;
		}
		if(armorStand != null) {
			armorStand.remove();
			armorStand = null;
		}
		HandlerList.unregisterAll(this);
		location = null;
		itemStack = null;
	}
	
	public void setName(String text) {
		if(armorStand == null) {
			armorStand = (ArmorStand) livingEntity.getWorld().spawnEntity(teleportArmorStand(), EntityType.ARMOR_STAND);
			armorStand.setVisible(false);
			armorStand.setGravity(false);
			armorStand.setCustomNameVisible(true);
		}
		armorStand.setCustomName(text);
	}
	
	public String getName() {
		return livingEntity.getCustomName();
	}
	
	public Location getStartingLocation() {
		return starting.clone();
	}
	
	public Location getTargetView() {
		return targetView;
	}
	
	public NPCEntity setTargetView(Location location) {
		targetView = location;
		return this;
	}
	
	/*public Location getTargetPath() {
		return targetPath;
	}
	
	public boolean isAtTargetPath() {
		return getLivingEntity().getLocation().equals(getTargetPath());
	}*/
	
	public LivingEntity getLivingEntity() {
		return livingEntity;
	}
	
	public ArmorStand getArmorStand() {
		return armorStand;
	}
	
	public Location teleportArmorStand() {
		double y = 0;
		EntityType type = getLivingEntity().getType();
		if(type == EntityType.CREEPER) {
			y = -.25;
		} else if(type == EntityType.BAT) {
			y = -1;
		}
		Location location = getLivingEntity().getLocation().add(0, y, 0);
		if(armorStand != null) {
			armorStand.teleport(location);
		}
		return location;
	}
	
	/*public NPCEntity setPathfinder(Location location) {
		return setPathfinder(location, 1.0f);
	}
	
	public NPCEntity setPathfinder(Location location, float speed) {
		CraftLivingEntity craftLivingEntity = (CraftLivingEntity) getLivingEntity();
		EntityLiving entityLiving = craftLivingEntity.getHandle();
		EntityInsentient entityInsentient = (EntityInsentient) entityLiving;
		PathfinderGoalWalkToLocation path = new PathfinderGoalWalkToLocation(entityInsentient, speed, location);
		EntityType type = getLivingEntity().getType();
		if(type == EntityType.CREEPER) {
			EntityCreeper entityCreeper = (EntityCreeper) entityLiving;
			entityCreeper.goalSelector.a(0, path);
		} else if(type == EntityType.SKELETON) {
			EntitySkeleton entitySkeleton = (EntitySkeleton) entityLiving;
			entitySkeleton.goalSelector.a(0, path);
		} else if(type == EntityType.SNOWBALL) {
			EntitySnowman entitySnowman = (EntitySnowman) entityLiving;
			entitySnowman.goalSelector.a(0, path);
		} else if(type == EntityType.VILLAGER) {
			EntityVillager entityVillager = (EntityVillager) entityLiving;
			entityVillager.goalSelector.a(0, path);
		} else if(type == EntityType.ZOMBIE) {
			EntityZombie entityZombie = (EntityZombie) entityLiving;
			entityZombie.goalSelector.a(0, path);
		}
		if(path instanceof PathfinderGoalWalkToLocation) {
			PathfinderGoalWalkToLocation pathFinder = (PathfinderGoalWalkToLocation) path;
			starting = location;
			targetPath = pathFinder.getLocation();
		}
		return this;
	}
	
	private void move() {
		CraftLivingEntity craftLivingEntity = (CraftLivingEntity) getLivingEntity();
		EntityLiving entityLiving = craftLivingEntity.getHandle();
		EntityType type = getLivingEntity().getType();
		double x = getLivingEntity().getLocation().getX();
		double y = getLivingEntity().getLocation().getY();
		double z = getLivingEntity().getLocation().getZ();
		if(type == EntityType.CREEPER) {
			EntityCreeper entityCreeper = (EntityCreeper) entityLiving;
			entityCreeper.move(x, y, z);
		} else if(type == EntityType.SKELETON) {
			EntitySkeleton entitySkeleton = (EntitySkeleton) entityLiving;
			entitySkeleton.move(x, y, z);
		} else if(type == EntityType.SNOWBALL) {
			EntitySnowman entitySnowman = (EntitySnowman) entityLiving;
			entitySnowman.move(x, y, z);
		} else if(type == EntityType.VILLAGER) {
			EntityVillager entityVillager = (EntityVillager) entityLiving;
			entityVillager.move(x, y, z);
		} else if(type == EntityType.ZOMBIE) {
			EntityZombie entityZombie = (EntityZombie) entityLiving;
			entityZombie.move(x, y, z);
		}
	}*/
	
	public abstract void onInteract(Player player);
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(!event.isCancelled() && event.getSpawnReason() == SpawnReason.CUSTOM) {
			Entity entity = event.getEntity();
			World world = entity.getWorld();
			CraftWorld craftWorld = (CraftWorld) world;
			CraftEntity craftEntity = (CraftEntity) entity;
			EntityLiving entityLiving = null;
			if(entity.getType() == EntityType.CREEPER && !(craftEntity.getHandle() instanceof CreeperNPC)) {
				entityLiving = new CreeperNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SKELETON && !(craftEntity.getHandle() instanceof SkeletonNPC)) {
				entityLiving = new SkeletonNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SNOWMAN && !(craftEntity.getHandle() instanceof SnowmanNPC)) {
				entityLiving = new SnowmanNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.VILLAGER && !(craftEntity.getHandle() instanceof VillagerNPC)) {
				entityLiving = new VillagerNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.ZOMBIE && !(craftEntity.getHandle() instanceof ZombieNPC)) {
				entityLiving = new ZombieNPC(craftWorld.getHandle());
			}
			if(entityLiving != null) {
				if(targetView == null) {
					targetView = world.getSpawnLocation();
				}
				location.getChunk().load();
				livingEntity = (LivingEntity) entityLiving.getBukkitEntity();
				livingEntity.setRemoveWhenFarAway(false);
				livingEntity.getEquipment().setItemInHand(itemStack);
				entityLiving.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
				craftWorld.getHandle().removeEntity(craftEntity.getHandle());
				craftWorld.getHandle().addEntity(entityLiving, SpawnReason.CUSTOM);
				if(entities == null) {
					entities = new ArrayList<LivingEntity>();
				}
				entities.add(livingEntity);
				if(npcEntities == null) {
					npcEntities = new HashMap<LivingEntity, NPCEntity>();
				}
				npcEntities.put(livingEntity, this);
				setName(ChatColor.translateAlternateColorCodes('&', name));
				Location loc = livingEntity.getLocation();
				starting.setDirection(loc.getWorld().getSpawnLocation().toVector().subtract(loc.toVector()));
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						final Pig pig = (Pig) location.getWorld().spawnEntity(getLivingEntity().getLocation().add(0.1, 1.5, 0.1), EntityType.PIG);
						pig.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 5));
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								pig.remove();
							}
						}, 20 * 2);
					}
				});
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Creature) {
			Creature creature = (Creature) event.getEntity();
			if(creature.equals(getLivingEntity())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && (event.getEntity().equals(getLivingEntity()) || event.getEntity().equals(getArmorStand()))) {
			Player player = (Player) event.getDamager();
			if(!SpectatorHandler.contains(player)) {
				onInteract(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked().equals(getLivingEntity())) {
			Player player = event.getPlayer();
			if(!SpectatorHandler.contains(player)) {
				onInteract(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if(event.getRightClicked().equals(getArmorStand())) {
			Player player = event.getPlayer();
			if(!SpectatorHandler.contains(player)) {
				onInteract(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		if(event.getEntity() instanceof Creature) {
			Creature creature = (Creature) event.getEntity();
			if(creature.equals(getLivingEntity())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		remove();
	}
}
