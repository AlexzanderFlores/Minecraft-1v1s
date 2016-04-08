package ostb.server.servers.hub;

import java.util.HashMap;
import java.util.Map;
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

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	private static Random random = null;
	private Map<String, SidebarScoreboardUtil> sidebars = null;
	private static int players = 0;
	private static int oldPlayers = players;
	
	public Events() {
		random = new Random();
		Bukkit.getWorlds().get(0).setSpawnLocation(1684, 6, -1280);
		sidebars = new HashMap<String, SidebarScoreboardUtil>();
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
		final Player player = event.getPlayer();
		if(Ranks.PREMIUM.hasRank(player)) {
			player.setAllowFlight(true);
		}
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack [] {});
		player.getInventory().setHeldItemSlot(0);
		player.teleport(getSpawn());
		SidebarScoreboardUtil sidebar = new SidebarScoreboardUtil(" &aOutsideTheBlock.org ") {
			@Override
			public void update(Player player) {
				if(oldPlayers != players) {
					removeScore(7);
				}
				Ranks rank = AccountHandler.getRank(player);
				setText(new String [] {
					" ",
					"&ePlayers",
					"&b" + players,
					"  ",
					"&eRank",
					rank == Ranks.PLAYER ? "&7None" : rank.getPrefix(),
					"   ",
					"&eHub #" + HubBase.getHubNumber(),
					"    ",
				});
				super.update(player);
			}
		};
		sidebar.update(player);
		sidebars.put(player.getName(), sidebar);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		if(sidebars.containsKey(name)) {
			sidebars.get(name).remove();
			sidebars.remove(name);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == (20 * 5)) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					int players = 0;
					for(String server : DB.NETWORK_POPULATIONS.getAllStrings("server")) {
						players += DB.NETWORK_POPULATIONS.getInt("server", server, "population");
					}
					oldPlayers = Events.players;
					Events.players = players;
				}
			});
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(sidebars.containsKey(player.getName())) {
					sidebars.get(player.getName()).update(player);
				}
			}
		}
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
