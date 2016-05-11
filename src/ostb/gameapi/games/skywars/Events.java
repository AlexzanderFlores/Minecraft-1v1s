package ostb.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.game.PostGameStartEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.SpawnPointHandler;
import ostb.gameapi.games.skywars.cages.Cage;
import ostb.gameapi.games.skywars.cages.SmallCage;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.mapeffects.MapEffectHandler;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class Events implements Listener {
	private Map<Team, Location> teamSpawns = null;
	private List<Team> spawnedCages = null;
	
	public Events() {
		teamSpawns = new HashMap<Team, Location>();
		spawnedCages = new ArrayList<Team>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		World world = OSTB.getMiniGame().getMap();
		new MapEffectHandler(world);
		SpawnPointHandler spawnPointHandler = new SpawnPointHandler(world);
		List<Location> spawns = spawnPointHandler.getSpawns();
		List<Player> players = ProPlugin.getPlayers();
		int counter = 0;
		int numberOfSpawns = spawns.size();
		for(Player player : players) {
			if(counter >= numberOfSpawns) {
				counter = 0;
			}
			Location location = spawns.get(counter++);
			Team team = TeamHandler.getTeam(player);
			if(team != null) {
				if(teamSpawns.containsKey(team)) {
					location = teamSpawns.get(team);
				} else {
					teamSpawns.put(team, location);
				}
			}
			player.teleport(location);
		}
		for(Player player : players) {
			Team team = TeamHandler.getTeam(player);
			if(team != null && spawnedCages.contains(team)) {
				continue;
			}
			boolean usedCage = false;
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPlugin() == Plugins.SW && kit.has(player) && kit.getKitType().equals("cage")) {
					kit.execute(player);
					usedCage = true;
					break;
				}
			}
			if(!usedCage) {
				new SmallCage(new ItemCreator(Material.GLASS).setName("Default Cage").setLores(new String [] {}).getItemStack(), 0).execute(player);
			}
			if(team != null && !spawnedCages.contains(team)) {
				spawnedCages.add(team);
			}
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(Cage.getCages() != null) {
			for(Cage cage : Cage.getCages()) {
				cage.remove();
			}
			Cage.getCages().clear();
		}
		MiniGame miniGame = OSTB.getMiniGame();
		miniGame.setAllowFoodLevelChange(true);
		miniGame.setAllowDroppingItems(true);
		miniGame.setAllowPickingUpItems(true);
		miniGame.setDropItemsOnLeave(true);
		miniGame.setAllowBuilding(true);
		miniGame.setAllowEntityCombusting(true);
		miniGame.setAllowPlayerInteraction(true);
		miniGame.setAllowBowShooting(true);
		miniGame.setAllowInventoryClicking(true);
		miniGame.setAllowItemSpawning(true);
		miniGame.setFlintAndSteelUses(4);
		miniGame.setCounter(60 * 8);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MiniGame miniGame = OSTB.getMiniGame();
				miniGame.setAllowEntityDamageByEntities(true);
				miniGame.setAllowEntityDamage(true);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onPostGameStart(PostGameStartEvent event) {
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getKitType().equals("kit")) {
				kit.execute();
			}
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		String type = event.getBlock().getType().toString();
		if(type.contains("LAVA") || type.contains("WATER")) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (byte) 4));
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.AIR));
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getItem().getType() == Material.INK_SACK) {
			event.setCancelled(true);
		}
	}
}
