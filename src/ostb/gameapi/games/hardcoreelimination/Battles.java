package ostb.gameapi.games.hardcoreelimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameDeathEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.server.effects.blocks.CylinderUtil;
import ostb.server.effects.blocks.HollowCylinderUtil;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class Battles implements Listener {
	private List<Block> blocks = null;
	private Map<String, String> battles = null;
	
	public Battles() {
		blocks = new ArrayList<Block>();
		battles = new HashMap<String, String>();
		HandlerList.unregisterAll(this);
		EventUtil.register(this);
		createArenas();
		OSTB.getSidebar().update("&ePVP");
	}
	
	private void createArenas() {
		World world = WorldHandler.getWorld();
		for(Entity entity : world.getEntities()) {
			if((entity instanceof LivingEntity || entity instanceof Item) && !(entity instanceof Player)) {
				entity.remove();
			}
		}
		List<Player> players = ProPlugin.getPlayers();
		int toCreate = (int) (players.size() + 1 / 2 + 0.5);
		Location [] centers = new Location[toCreate];
		for(int a = 0; a < toCreate; ++a) {
			Location location = new Location(world, 50 * a, 0, 0);
			location.setY(WorldHandler.getGround(location).getBlockY());
			centers[a] = location;
			new CylinderUtil(world, location.getBlockX(), location.getBlockY() + 1, location.getBlockZ(), 25, world.getMaxHeight() - location.getBlockY(), Material.AIR);
			new CylinderUtil(world, location.getBlockX(), 1, location.getBlockZ(), 25, location.getBlockY() - 1, Material.SPONGE);
			byte lastUsed = 0;
			for(int b = -1; b <= 5; ++b) {
				lastUsed = (byte) (lastUsed == 0 ? 1 : 0);
				if(b <= 0) {
					for(Block block : new CylinderUtil(world, location.getBlockX(), location.getBlockY() + b, location.getBlockZ(), 21, 1, Material.WOOD, lastUsed).getBlocks()) {
						blocks.add(block);
					}
				} else {
					for(Block block : new HollowCylinderUtil(world, location.getBlockX(), location.getBlockY() + b, location.getBlockZ(), 21, 1, Material.WOOD, lastUsed).getBlocks()) {
						if(block.getType() != Material.AIR) {
							blocks.add(block);
						}
					}
				}
			}
			new CylinderUtil(world, location.getBlockX(), location.getBlockY() + 6, location.getBlockZ(), 20, 1, Material.BARRIER);
		}
		for(int a = 0; ; a += 2) {
			if(a >= players.size()) {
				break;
			}
			int index = (int) (a / 2 + 0.5);
			Location location = centers[index].clone().add(17, 0, 0);
			location.setYaw(-270.0f);
			location.setPitch(0.0f);
			players.get(a).teleport(location.clone().add(0, 2.5, 0));
			String nameOne = players.get(a).getName();
			battles.put(nameOne, null);
			if(a + 1 < players.size()) {
				location = centers[index].clone().add(-17, 0, 0);
				location.setYaw(-90.0f);
				location.setPitch(0.0f);
				players.get(a + 1).teleport(location.clone().add(0, 2.5, 0));
				String nameTwo = players.get(a + 1).getName();
				battles.put(nameOne, nameTwo);
				battles.put(nameTwo, nameOne);
			}
		}
		final Battles instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					new TitleDisplayer(player, "&cPVP", "&eEnabled").setFadeIn(15).setStay(20).setFadeIn(20).display();
				}
				PlayerMoveEvent.getHandlerList().unregister(instance);
				ProjectileLaunchEvent.getHandlerList().unregister(instance);
				ItemSpawnEvent.getHandlerList().unregister(instance);
				PlayerDropItemEvent.getHandlerList().unregister(instance);
				PlayerPickupItemEvent.getHandlerList().unregister(instance);
				EntityDamageEvent.getHandlerList().unregister(instance);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer())) {
			Location to = event.getTo();
			Location from = event.getFrom();
			if(to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) {
				event.setTo(from);
			}
		}
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(blocks.contains(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		Material type = event.getBlock().getType();
		if(blocks.contains(event.getBlock()) && type != Material.STATIONARY_WATER && type != Material.STATIONARY_LAVA) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if(event.blockList() != null && !event.blockList().isEmpty()) {
			Iterator<Block> iterator = event.blockList().iterator();
			while(iterator.hasNext()) {
				if(blocks.contains(iterator.next())) {
					iterator.remove();
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		if(event.blockList() != null && !event.blockList().isEmpty()) {
			Iterator<Block> iterator = event.blockList().iterator();
			while(iterator.hasNext()) {
				if(blocks.contains(iterator.next())) {
					iterator.remove();
				}
			}
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.NATURAL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		battles.remove(event.getPlayer().getName());
		battles.put(event.getKiller().getName(), null);
		for(String battle : battles.keySet()) {
			if(battles.get(battle) != null) {
				return;
			}
		}
		MessageHandler.alert("Starting next round...");
		createArenas();
	}
}
