package ostb.server.servers.hub;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	private static Random random = null;
	
	public Events() {
		random = new Random();
		Bukkit.getWorlds().get(0).setSpawnLocation(1684, 6, -1280);
		EventUtil.register(this);
	}
	
	public static Location getSpawn() {
		int range = 6;
		Location location = Bukkit.getWorlds().get(0).getSpawnLocation();
		location.setYaw(-180.0f);
		location.setPitch(0.0f);
		location.setX(location.getX() + (random.nextBoolean() ? random.nextInt(range) : random.nextInt(range) * -1));
		location.setZ(location.getZ() + (random.nextBoolean() ? random.nextInt(range) : random.nextInt(range) * -1));
		return location;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PREMIUM.hasRank(player)) {
			player.setAllowFlight(true);
		}
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack [] {});
		player.getInventory().setHeldItemSlot(0);
		player.teleport(getSpawn());
		/*new CircleUtil(player, .85, 6) {
			@Override
			public void run(Vector vector, Location location) {
				ParticleEffect.FIREWORKS_SPARK.display(location.add(0, 1.90, 0), 20);
			}
		};*/
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID) {
			event.getEntity().teleport(getSpawn());
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}
}
