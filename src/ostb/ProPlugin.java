package ostb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import npc.NPCRegistrationHandler.NPCs;
import ostb.OSTB.Plugins;
import ostb.customevents.ServerRestartEvent;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.game.GameKillEvent;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.AsyncPostPlayerJoinEvent;
import ostb.customevents.player.PlayerHeadshotEvent;
import ostb.customevents.player.PlayerItemFrameInteractEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PostPlayerJoinEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.DB.Databases;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.StringUtil;

public class ProPlugin extends CountDownUtil implements Listener {
	private String name = null;
	private List<String> importedWorlds = null;
	private static List<String> networkingGroups = null;
	private static int numberOfHubs = 0;
	private boolean beta = false;
	private boolean allowEntityDamage = false;
	private boolean allowEntityDamageByEntities = false;
	private boolean allowFoodLevelChange = false;
	private boolean allowDroppingItems = false;
	private boolean allowPickingUpItems = false;
	private boolean allowItemSpawning = false;
	private boolean dropItemsOnLeave = false;
	private boolean allowBuilding = false;
	private boolean allowHealthRegeneration = true;
	private boolean allowDefaultMobSpawning = false;
	private boolean allowEntityCombusting = false;
	private boolean allowBowShooting = false;
	private boolean allowPlayerInteraction = false;
	private boolean allowBlockBurning = false;
	private boolean allowBlockFading = false;
	private boolean allowBlockForming = false;
	private boolean allowBlockFromTo = false;
	private boolean allowBlockGrow = false;
	private boolean allowBlockSpread = false;
	private boolean allowLeavesDecay = false;
	private boolean allowEntityChangeBlock = false;
	private boolean allowEntityCreatePortal = false;
	private boolean allowBedEntering = false;
	private boolean allowHangingBreakByEntity = false;
	private boolean allowEntityExplode = false;
	private boolean autoVanishStaff = false;
	private boolean removeEntitiesUponLoadingWorld = true;
	private boolean resetPlayerUponJoining = true;
	private boolean allowInventoryClicking = false;
	private boolean allowItemBreaking = true;
	private boolean allowArmorBreaking = true;
	private boolean doDaylightCycle = false;
	private int flintAndSteelUses = 0;
	private static boolean restarting = false;
	
	public ProPlugin(String name) {
		OSTB.setProPlugin(this);
		setName(name);
		EventUtil.register(this);
	}
	
	public void disable() {
		try {
			for(NPCs npc : NPCs.values()) {
				npc.unregister();
			}
		} catch(NullPointerException e) {
			
		}
	}
	
	public void resetFlags() {
		setAllowEntityDamage(false);
		setAllowEntityDamageByEntities(false);
		setAllowFoodLevelChange(false);
		setAllowDroppingItems(false);
		setAllowPickingUpItems(false);
		setAllowItemSpawning(false);
		setDropItemsOnLeave(false);
		setAllowBuilding(false);
		setAllowHealthRegeneration(true);
		setAllowDefaultMobSpawning(false);
		setAllowEntityCombusting(false);
		setAllowBowShooting(false);
		setAllowPlayerInteraction(false);
		setAllowBlockBurning(false);
		setAllowBlockFading(false);
		setAllowBlockForming(false);
		setAllowBlockFromTo(false);
		setAllowBlockGrow(false);
		setAllowBlockSpread(false);
		setAllowLeavesDecay(false);
		setAllowEntityChangeBlock(false);
		setAllowEntityCreatePortal(false);
		setAllowBedEntering(false);
		setAllowHangingBreakByEntity(false);
		setAllowEntityExplode(false);
		setAutoVanishStaff(true);
		setRemoveEntitiesUponLoadingWorld(true);
		setResetPlayerUponJoining(true);
		setAllowInventoryClicking(false);
		setAllowItemBreaking(true);
		setFlintAndSteelUses(4);
		setDoDaylightCycle(false);
	}
	
	public void removeFlags() {
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowFoodLevelChange(true);
		setAllowDroppingItems(true);
		setAllowPickingUpItems(true);
		setAllowItemSpawning(true);
		setDropItemsOnLeave(false);
		setAllowBuilding(true);
		setAllowHealthRegeneration(true);
		setAllowDefaultMobSpawning(true);
		setAllowEntityCombusting(true);
		setAllowBowShooting(true);
		setAllowPlayerInteraction(true);
		setAllowBlockBurning(true);
		setAllowBlockFading(true);
		setAllowBlockForming(true);
		setAllowBlockFromTo(true);
		setAllowBlockGrow(true);
		setAllowBlockSpread(true);
		setAllowLeavesDecay(true);
		setAllowEntityChangeBlock(true);
		setAllowEntityCreatePortal(true);
		setAllowBedEntering(true);
		setAllowHangingBreakByEntity(true);
		setAllowEntityExplode(true);
		setAutoVanishStaff(false);
		setRemoveEntitiesUponLoadingWorld(false);
		setResetPlayerUponJoining(false);
		setAllowInventoryClicking(true);
		setAllowItemBreaking(true);
		setFlintAndSteelUses(0);
		setDoDaylightCycle(true);
	}
	
	public static List<String> getGroups() {
		return networkingGroups;
	}
	
	public static void addGroup(String group) {
		if(networkingGroups == null) {
			networkingGroups = new ArrayList<String>();
		}
		networkingGroups.add(group);
	}
	
	public static int getNumberOfHubs() {
		return numberOfHubs;
	}
	
	public String getDisplayName() {
		return this.name;
	}
	
	public String getName() {
		return getDisplayName().toLowerCase().replace(" ", "").replace("_", "");
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getBeta() {
		return this.beta;
	}
	
	public void setBeta(boolean beta) {
		this.beta = beta;
	}
	
	public boolean getAllowEntityDamage() {
		return this.allowEntityDamage;
	}
	
	public void setAllowEntityDamage(boolean allowEntityDamage) {
		this.allowEntityDamage = allowEntityDamage;
	}
	
	public boolean getAllowEntityDamageByEntities() {
		return this.allowEntityDamageByEntities;
	}
	
	public void setAllowEntityDamageByEntities(boolean allowEntityDamageByEntities) {
		this.allowEntityDamageByEntities = allowEntityDamageByEntities;
	}
	
	public boolean getAllowFoodLevelChange() {
		return this.allowFoodLevelChange;
	}
	
	public void setAllowFoodLevelChange(boolean allowFoodLevelChange) {
		this.allowFoodLevelChange = allowFoodLevelChange;
	}
	
	public boolean getAllowDroppingItems() {
		return this.allowDroppingItems;
	}
	
	public void setAllowDroppingItems(boolean allowDroppingItems) {
		this.allowDroppingItems = allowDroppingItems;
	}
	
	public boolean getAllowPickingUpItems() {
		return this.allowPickingUpItems;
	}
	
	public void setAllowPickingUpItems(boolean allowPickingUpItems) {
		this.allowPickingUpItems = allowPickingUpItems;
	}
	
	public boolean getAllowItemSpawning() {
		return this.allowItemSpawning;
	}
	
	public void setAllowItemSpawning(boolean allowItemSpawning) {
		this.allowItemSpawning = allowItemSpawning;
	}
	
	public boolean getDropItemsOnLeave() {
		return this.dropItemsOnLeave;
	}
	
	public void setDropItemsOnLeave(boolean dropItemsOnLeave) {
		this.dropItemsOnLeave = dropItemsOnLeave;
	}
	
	public boolean getAllowBuilding() {
		return this.allowBuilding;
	}
	
	public void setAllowBuilding(boolean allowBuilding) {
		this.allowBuilding = allowBuilding;
	}
	
	public boolean getAllowHealthRegeneration() {
		return this.allowHealthRegeneration;
	}
	
	public void setAllowHealthRegeneration(boolean allowHealthRegeneration) {
		this.allowHealthRegeneration = allowHealthRegeneration;
	}
	
	public boolean getAllowDefaultMobSpawning() {
		return this.allowDefaultMobSpawning;
	}
	
	public void setAllowDefaultMobSpawning(boolean allowDefaultMobSpawning) {
		this.allowDefaultMobSpawning = allowDefaultMobSpawning;
	}
	
	public boolean getAllowEntityCombusting() {
		return this.allowEntityCombusting;
	}
	
	public void setAllowEntityCombusting(boolean allowEntityCombusting) {
		this.allowEntityCombusting = allowEntityCombusting;
	}
	
	public boolean getAllowBowShooting() {
		return this.allowBowShooting;
	}
	
	public void setAllowBowShooting(boolean allowBowShooting) {
		this.allowBowShooting = allowBowShooting;
		if(getAllowBowShooting()) {
			setAllowPlayerInteraction(true);
		}
	}
	
	public boolean getAllowPlayerInteraction() {
		return this.allowPlayerInteraction;
	}
	
	public void setAllowPlayerInteraction(boolean allowPlayerInteraction) {
		this.allowPlayerInteraction = allowPlayerInteraction;
	}
	
	public boolean getAllowBlockBurning() {
		return this.allowBlockBurning;
	}
	
	public void setAllowBlockBurning(boolean allowBlockBurning) {
		this.allowBlockBurning = allowBlockBurning;
	}
	
	public boolean getAllowBlockFading() {
		return this.allowBlockFading;
	}
	
	public void setAllowBlockFading(boolean allowBlockFading) {
		this.allowBlockFading = allowBlockFading;
	}
	
	public boolean getAllowBlockForming() {
		return this.allowBlockForming;
	}
	
	public void setAllowBlockForming(boolean allowBlockForming) {
		this.allowBlockForming = allowBlockForming;
	}
	
	public boolean getAllowBlockFromTo() {
		return this.allowBlockFromTo;
	}
	
	public void setAllowBlockFromTo(boolean allowBlockFromTo) {
		this.allowBlockFromTo = allowBlockFromTo;
	}
	
	public boolean getAllowBlockGrow() {
		return this.allowBlockGrow;
	}
	
	public void setAllowBlockGrow(boolean allowBlockGrow) {
		this.allowBlockGrow = allowBlockGrow;
	}
	
	public boolean getAllowBlockSpread() {
		return this.allowBlockSpread;
	}
	
	public void setAllowBlockSpread(boolean allowBlockSpread) {
		this.allowBlockSpread = allowBlockSpread;
	}
	
	public boolean getAllowLeavesDecay() {
		return this.allowLeavesDecay;
	}
	
	public void setAllowLeavesDecay(boolean allowLeavesDecay) {
		this.allowLeavesDecay = allowLeavesDecay;
	}
	
	public boolean getAllowEntityChangeBlock() {
		return allowEntityChangeBlock;
	}
	
	public void setAllowEntityChangeBlock(boolean allowEntityChangeBlock) { 
		this.allowEntityChangeBlock = allowEntityChangeBlock;
	}
	
	public boolean getAllowEntityCreatePortal() {
		return this.allowEntityCreatePortal;
	}
	
	public void setAllowEntityCreatePortal(boolean allowEntityCreatePortal) {
		this.allowEntityCreatePortal = allowEntityCreatePortal;
	}
	
	public boolean getAllowBedEntering() {
		return this.allowBedEntering;
	}
	
	public void setAllowBedEntering(boolean allowBedEntering) {
		this.allowBedEntering = allowBedEntering;
	}
	
	public boolean getAllowHangingBreakByEntity() {
		return this.allowHangingBreakByEntity;
	}
	
	public void setAllowHangingBreakByEntity(boolean allowHangingBreakByEntity) {
		this.allowHangingBreakByEntity = allowHangingBreakByEntity;
	}
	
	public boolean getAllowEntityExplode() {
		return this.allowEntityExplode;
	}
	
	public void setAllowEntityExplode(boolean allowEntityExplode) {
		this.allowEntityExplode = allowEntityExplode;
	}
	
	public boolean getAutoVanishStaff() {
		return this.autoVanishStaff;
	}
	
	public void setAutoVanishStaff(boolean autoVanishStaff) {
		this.autoVanishStaff = autoVanishStaff;
	}
	
	public boolean getRemoveEntitiesUponLoadingWorld() {
		return this.removeEntitiesUponLoadingWorld;
	}
	
	public void setRemoveEntitiesUponLoadingWorld(boolean removeEntitiesUponLoadingWorld) {
		this.removeEntitiesUponLoadingWorld = removeEntitiesUponLoadingWorld;
	}
	
	public boolean getResetPlayerUponJoining() {
		return this.resetPlayerUponJoining;
	}
	
	public void setResetPlayerUponJoining(boolean resetPlayerUponJoining) {
		this.resetPlayerUponJoining = resetPlayerUponJoining;
	}
	
	public boolean getAllowInventoryClicking() {
		return this.allowInventoryClicking;
	}
	
	public void setAllowInventoryClicking(boolean allowInventoryClicking) {
		this.allowInventoryClicking = allowInventoryClicking;
	}
	
	public boolean getAllowItemBreaking() {
		return this.allowItemBreaking;
	}
	
	public void setAllowItemBreaking(boolean allowItemBreaking) {
		this.allowItemBreaking = allowItemBreaking;
	}
	
	public boolean getAllowArmorBreaking() {
		return this.allowArmorBreaking;
	}
	
	public void setAllowArmorBreaking(boolean allowArmorBreaking) {
		this.allowArmorBreaking = allowArmorBreaking;
	}
	
	public boolean getDoDaylightCycle() {
		return this.doDaylightCycle;
	}
	
	public void setDoDaylightCycle(boolean doDaylightCycle) {
		this.doDaylightCycle = doDaylightCycle;
	}
	
	public int getFlintAndSteelUses() {
		return this.flintAndSteelUses;
	}
	
	public void setFlintAndSteelUses(int flintAndSteelUses) {
		this.flintAndSteelUses = flintAndSteelUses;
	}
	
	public static boolean isServerFull() {
		return Bukkit.getOnlinePlayers().size() >= OSTB.getMaxPlayers();
	}
	
	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!SpectatorHandler.contains(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static void sendPlayerToServer(Player player, String server) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(server);
		player.sendPluginMessage(OSTB.getInstance(), "BungeeCord", out.toByteArray());
	}
	
	public static Player getPlayer(String name) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
	
	public static void resetPlayer(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.getInventory().setHeldItemSlot(0);
		player.updateInventory();
		player.setLevel(0);
		player.setExp(0.0f);
		player.setMaxHealth(20.0d);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setGameMode(GameMode.SURVIVAL);
		if(player.getAllowFlight()) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		if(player.getVehicle() != null) {
			player.leaveVehicle();
		}
		if(player.getPassenger() != null) {
			player.getPassenger().leaveVehicle();
		}
	}
	
	public static void restartServer() {
		restarting = true;
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-ips.json"));
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-ips.txt.converted"));
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-players.json"));
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-players.txt.converted"));
		for(Player player : Bukkit.getOnlinePlayers()) {
			sendPlayerToServer(player, "hub");
		}
		Bukkit.getPluginManager().callEvent(new ServerRestartEvent());
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().shutdown();
			}
		}, 20 * 3);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!getAllowBuilding()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBurn(BlockBurnEvent event) {
		if(!getAllowBlockBurning()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockFade(BlockFadeEvent event) {
		if(!getAllowBlockFading()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockForm(BlockFormEvent event) {
		if(!getAllowBlockForming()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockFromTo(BlockFromToEvent event) {
		if(!getAllowBlockFromTo()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockGrow(BlockGrowEvent event) {
		if(!getAllowBlockGrow()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(event.getCause() != IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!getAllowBuilding()) {
			event.setCancelled(true);
		}
		if(!event.isCancelled() && !getAllowHangingBreakByEntity()) {
			int x = event.getBlock().getLocation().getBlockX();
			int y = event.getBlock().getLocation().getBlockY();
			int z = event.getBlock().getLocation().getBlockZ();
			for(Entity entity : event.getPlayer().getWorld().getEntities()) {
				if(entity instanceof ItemFrame) {
					if(entity.getLocation().getBlockX() == x && entity.getLocation().getBlockY() == y && entity.getLocation().getBlockZ() == z) {
						event.setCancelled(true);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockSpread(BlockSpreadEvent event) {
		if(!getAllowBlockSpread()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onLeavesDecay(LeavesDecayEvent event) {
		if(!getAllowLeavesDecay()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() != SpawnReason.CUSTOM && !getAllowDefaultMobSpawning()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityCombust(EntityCombustEvent event) {
		if(!getAllowEntityCombusting()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if(!getAllowHealthRegeneration()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(!getAllowEntityChangeBlock()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityCreatePortal(EntityCreatePortalEvent event) {
		if(!getAllowEntityCreatePortal()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event) {
		if(!getAllowEntityExplode()) {
			if(event.blockList() != null) {
				event.blockList().clear();
			}
			event.setYield(0.0f);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(restarting) {
			event.setKickMessage("This server is currently restarting");
			event.setResult(Result.KICK_OTHER);
		} else if(ProPlugin.isServerFull() && Ranks.PREMIUM.hasRank(player) && OSTB.getMiniGame().getJoiningPreGame()) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(!Ranks.PREMIUM.hasRank(online)) {
					MessageHandler.sendMessage(online, "You were moved to the hub to make room for a " + AccountHandler.getRank(player).getPrefix());
					MessageHandler.sendMessage(online, "Avoid this with a rank: &b/buy");
					ProPlugin.sendPlayerToServer(online, "hub");
					event.setResult(Result.ALLOWED);
					return;
				}
			}
			event.setResult(Result.KICK_FULL);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHighestPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(event.getResult() == Result.KICK_FULL) {
			if(AccountHandler.getRank(player) == Ranks.PLAYER) {
				event.setKickMessage(ChatColor.YELLOW + "Server is full, ranks allow you to join full servers; " + ChatColor.AQUA + "/buy");
			} else {
				event.setKickMessage(ChatColor.YELLOW + "Server is full, ranks allow you to join full servers; " + ChatColor.AQUA + "/buy");
			}
		} else if(getBeta() && !Ranks.PREMIUM.hasRank(player)) {
			event.setKickMessage(ChatColor.YELLOW + "Server is currently in BETA mode, ranks allow you to join beta servers; " + ChatColor.AQUA + "/buy");
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(getResetPlayerUponJoining()) {
			resetPlayer(player);
			player.teleport(player.getWorld().getSpawnLocation());
		}
		event.setJoinMessage(null);
		player.setTicksLived(1);
		final String name = player.getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					Bukkit.getPluginManager().callEvent(new AsyncPlayerJoinEvent(player));
				}
			}
		});
	}
	
	//TODO: Look into seeing if a post player join event is needed
	@EventHandler(priority = EventPriority.LOW)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		final String name = player.getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					Bukkit.getPluginManager().callEvent(new AsyncPostPlayerJoinEvent(player));
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if(!getAllowBedEntering()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(getDropItemsOnLeave() && !SpectatorHandler.contains(player)) {
			for(ItemStack itemStack : player.getInventory().getContents()) {
				if(itemStack != null && itemStack.getType() != Material.AIR) {
					player.getWorld().dropItem(player.getLocation(), itemStack);
				}
			}
			for(ItemStack itemStack : player.getInventory().getArmorContents()) {
				if(itemStack != null && itemStack.getType() != Material.AIR) {
					player.getWorld().dropItem(player.getLocation(), itemStack);
				}
			}
		}
		final UUID uuid = player.getUniqueId();
		final String name = player.getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getPluginManager().callEvent(new AsyncPlayerLeaveEvent(uuid, name));
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(!getAllowBowShooting()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!getAllowEntityDamage()) {
			event.setCancelled(true);
		}
		if(event.getCause() == DamageCause.VOID) {
			Entity entity = event.getEntity();
			if(entity.getVehicle() != null) {
				entity = entity.getVehicle();
			}
			if(entity.getPassenger() != null) {
				entity.eject();
			}
			entity.teleport(event.getEntity().getWorld().getSpawnLocation());
			if(entity instanceof LivingEntity) {
				LivingEntity livingEntity = (LivingEntity) entity;
				event.setDamage(livingEntity.getHealth());
			}
		}
		if(!getAllowArmorBreaking() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			for(ItemStack armor : player.getInventory().getArmorContents()) {
				armor.setDurability((short) -1);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHighEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getCause() == DamageCause.VOID && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Player killer = player.getKiller();
			if(killer == null || !killer.isOnline() || (SpectatorHandler.isEnabled() && SpectatorHandler.contains(killer))) {
				player.getInventory().clear();
				player.getInventory().setArmorContents(new ItemStack [] {});
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof ItemFrame) {
			if(!getAllowHangingBreakByEntity()) {
				event.setCancelled(true);
			}
			if(event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				ItemFrame itemFrame = (ItemFrame) event.getEntity();
				Bukkit.getPluginManager().callEvent(new PlayerItemFrameInteractEvent(player, itemFrame));
			}
		} else if(!getAllowEntityDamageByEntities()) {
			event.setCancelled(true);
		}
		if(!event.isCancelled() && event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Player player = (Player) event.getEntity();
			Projectile projectile = (Projectile) event.getDamager();
			ProjectileSource source = projectile.getShooter();
			if(!SpectatorHandler.contains(player) && source != null && source instanceof Player && projectile.getLocation().getY() - player.getLocation().getY() >= 1.35f) {
				Player shooter = (Player) source;
				if(!player.getName().equals(shooter.getName())) {
					PlayerHeadshotEvent headshotEvent = new PlayerHeadshotEvent(shooter, event.getDamage());
					Bukkit.getPluginManager().callEvent(headshotEvent);
					if(!headshotEvent.isCancelled()) {
						if(headshotEvent.getDamage() > -1) {
							event.setDamage(headshotEvent.getDamage());
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getKiller() != null && player.getKiller() instanceof Player) {
			Player killer = player.getKiller();
			String message = event.getDeathMessage().replace(player.getName(), AccountHandler.getPrefix(player, false) + ChatColor.WHITE);
			Bukkit.getPluginManager().callEvent(new GameDeathEvent(player, killer));
			Bukkit.getPluginManager().callEvent(new GameKillEvent(killer, player, event.getDeathMessage()));
			if(!player.getName().equals(killer.getName())) {
				event.setDeathMessage(message.replace(killer.getName(), AccountHandler.getPrefix(killer, false) + ChatColor.WHITE));
			}
		} else {
			Bukkit.getPluginManager().callEvent(new GameDeathEvent(player));
			String message = event.getDeathMessage().replace(player.getName(), AccountHandler.getPrefix(player, false));
			String [] split = message.split("entity.");
			message = split[0];
			if(split.length > 1) {
				message += ChatColor.RED + StringUtil.getFirstLetterCap(split[1].replace(".name", ""));
			}
			event.setDeathMessage(message);
		}
		event.setDeathMessage(event.getDeathMessage().split(" using ")[0]);
		final String name = player.getName();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(name != null) {
					PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
					CraftPlayer craftPlayer = (CraftPlayer) player;
					EntityPlayer entityPlayer = craftPlayer.getHandle();
					entityPlayer.playerConnection.a(packet);
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(!getAllowFoodLevelChange()) {
			event.setFoodLevel(20);
		}
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			player.setSaturation(4.0f);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreak(HangingBreakEvent event) {
		if(event.getCause() == RemoveCause.EXPLOSION || event.getCause() == RemoveCause.ENTITY) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if(!getAllowHangingBreakByEntity()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
			Bukkit.getPluginManager().callEvent(new PlayerItemFrameInteractEvent(event.getPlayer(), itemFrame));
			if(!getAllowHangingBreakByEntity()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(!getAllowPickingUpItems()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(!getAllowDroppingItems()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(getAllowPlayerInteraction()) {
			ItemStack item = event.getItem();
			if(item != null && item.getType() == Material.FLINT_AND_STEEL && event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.isCancelled()) {
				int uses = getFlintAndSteelUses();
				if(uses > 0) {
					item.setDurability((short) (item.getDurability() + (item.getType().getMaxDurability() / uses)));
					if(item.getDurability() >= item.getType().getMaxDurability()) {
						event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
					} else {
						event.getPlayer().setItemInHand(item);
					}
				}
			}
		} else {
			event.setCancelled(true);
		}
		if(!getAllowItemBreaking()) {
			ItemStack item = event.getItem();
			if(item != null && item.getType() != Material.FLINT_AND_STEEL) {
				event.getPlayer().getItemInHand().setDurability((short) -1);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemSpawn(ItemSpawnEvent event) {
		if(!getAllowItemSpawning()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		if(getDoDaylightCycle()) {
			world.setGameRuleValue("doDaylightCycle", "true");
		} else {
			world.setGameRuleValue("doDaylightCycle", "false");
		}
		world.setGameRuleValue("keepInventory", "false");
		world.setWeatherDuration(0);
		world.setThunderDuration(0);
		world.setStorm(false);
		world.setThundering(false);
		world.setAutoSave(false);
		world.setTime(6000);
		if(getRemoveEntitiesUponLoadingWorld()) {
			for(Entity entity : world.getEntities()) {
				if(entity instanceof LivingEntity && !(entity instanceof Player)) {
					entity.remove();
				}
			}
		}
		if(importedWorlds == null) {
			importedWorlds = new ArrayList<String>();
		}
		importedWorlds.add(world.getName());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerRestart(ServerRestartEvent event) {
		if(!event.isCancelled()) {
			for(World world : Bukkit.getWorlds()) {
				String path = Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/";
				Bukkit.unloadWorld(world, false);
				try {
					FileHandler.delete(new File(path + "playerdata"));
					FileHandler.delete(new File(path + "stats"));
				} catch(Exception e) {
					
				}
			}
			if(importedWorlds != null && OSTB.getPlugin() != Plugins.BUILDING) {
				for(String world : importedWorlds) {
					FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world));
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!getAllowInventoryClicking()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			for(Databases database : Databases.values()) {
				database.connect();
			}
		} else if(ticks == 20 * 10) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					numberOfHubs = DB.NETWORK_SERVER_STATUS.getSize("game_name", "HUB");
				}
			});
		} else if(ticks == 20 * 60 * 5) {
			try {
				FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/logs"));
			} catch(Exception e) {
				
			}
		}
	}
}