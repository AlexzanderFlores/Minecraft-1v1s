package ostb.gameapi.games.hardcoreelimination;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import ostb.player.TitleDisplayer;
import ostb.server.effects.blocks.CylinderUtil;
import ostb.server.effects.blocks.HollowCylinderUtil;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class Battles implements Listener {
	private List<Block> blocks = null;
	private List<BattleData> battles = null;
	
	public Battles() {
		blocks = new ArrayList<Block>();
		battles = new ArrayList<BattleData>();
		HandlerList.unregisterAll(this);
		EventUtil.register(this);
		createArenas();
		OSTB.getSidebar().update("&ePVP");
	}
	
	private class BattleData {
		private String playerOne = null;
		private String playerTwo = null;
		
		public BattleData(String playerOne, String playerTwo) {
			this.playerOne = playerOne;
			this.playerTwo = playerTwo;
			battles.add(this);
		}
		
		public Player getWinner(Player loser) {
			if(playerOne.equals(loser.getName())) {
				playerOne = null;
				return ProPlugin.getPlayer(playerTwo);
			} else if(playerTwo.equals(loser.getName())) {
				playerTwo = null;
				return ProPlugin.getPlayer(playerOne);
			} else {
				return null;
			}
		}
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
		Location [] centers = new Location [toCreate];
		for(int a = 0; a < toCreate; ++a) {
			Location location = new Location(world, 50 * a, 0, 0);
			location.setY(WorldHandler.getGround(location).getBlockY());
			centers[a] = location;
			new HollowCylinderUtil(world, centers[a].getBlockX(), centers[a].getBlockY() + 1, centers[a].getBlockZ(), 5, 1, Material.LEAVES);
			new CylinderUtil(world, location.getBlockX(), location.getBlockY() + 1, location.getBlockZ(), 25, 40, Material.AIR, (byte) 0);
			for(int b = -1; b <= 0; ++b) {
				for(Block block : new CylinderUtil(world, location.getBlockX(), location.getBlockY() + b, location.getBlockZ(), 20, 1, Material.WOOD).getBlocks()) {
					blocks.add(block);
				}
			}
		}
		for(int a = 0; ; a += 2) {
			if(a >= players.size()) {
				break;
			}
			int index = (int) (a / 2 + 0.5);
			Location location = centers[index].clone().add(17, 0, 0);
			location.setYaw(-270.0f);
			location.setPitch(0.0f);
			players.get(a).teleport(location.clone().add(0, 2, 0));
			if(a + 1 < players.size()) {
				location = centers[index].clone().add(-17, 0, 0);
				location.setYaw(-90.0f);
				location.setPitch(0.0f);
				players.get(a + 1).teleport(location);
				new BattleData(players.get(a).getName(), players.get(a + 1).getName());
			}
		}
		for(Location center : centers) {
			byte lastUsed = 0;
			for(int a = 1; a <= 5; ++a) {
				lastUsed = (byte) (lastUsed == 0 ? 1 : 0);
				for(Block block : new HollowCylinderUtil(world, center.getBlockX(), center.getBlockY() + a, center.getBlockZ(), 21, 1, Material.WOOD, lastUsed).getBlocks()) {
					blocks.add(block);
				}
			}
			new CylinderUtil(world, center.getBlockX(), center.getBlockY() + 6, center.getBlockZ(), 20, 1, Material.BARRIER);
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
		if(blocks.contains(event.getBlock())) {
			event.setCancelled(true);
		}
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
		Iterator<BattleData> iterator = battles.iterator();
		while(iterator.hasNext()) {
			BattleData data = iterator.next();
			Player winner = data.getWinner(event.getPlayer());
			if(winner != null) {
				iterator.remove();
				if(battles.isEmpty()) {
					
				}
			}
		}
	}
}
