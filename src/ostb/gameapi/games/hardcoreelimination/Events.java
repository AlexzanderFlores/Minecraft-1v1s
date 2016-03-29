package ostb.gameapi.games.hardcoreelimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.timed.FiveSecondTaskEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	private Map<String, Location> spawns = null;
	private List<String> scattered = null;
	private boolean logSpawns = false;
	
	public Events() {
		spawns = new HashMap<String, Location>();
		scattered = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		MiniGame game = OSTB.getMiniGame();
		if(game.getGameState() == GameStates.STARTED) {
			if(game.getCounter() <= 0) {
				HandlerList.unregisterAll(this);
				new Battles();
			} else {
				if(game.canDisplay()) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						new TitleDisplayer(player, "&cPVP", game.getCounterAsString()).setFadeIn(0).setStay(15).setFadeOut(30).display();
					}
				}
				OSTB.getSidebar().update(game.getCounterAsString());
			}
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			player.teleport(WorldHandler.getWorld().getSpawnLocation());
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		OSTB.getProPlugin().removeFlags();
		OSTB.getMiniGame().setCounter(60 * 10);
		String command = "spreadPlayers 0 0 100 500 false ";
		for(Player player : ProPlugin.getPlayers()) {
			player.setNoDamageTicks(20 * 30);
			MessageHandler.sendMessage(player, "You now have &e30 &xseconds of no damage of any kind");
			command += player.getName() + " ";
		}
		MessageHandler.alert("Scattering all players...");
		logSpawns = true;
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(logSpawns && !spawns.containsKey(player.getName()) && !SpectatorHandler.contains(player)) {
			spawns.put(player.getName(), event.getTo());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		if(spawns != null && !spawns.isEmpty()) {
			String name = null;
			for(String spawn : spawns.keySet()) {
				if(!scattered.contains(spawn)) {
					name = spawn;
					break;
				}
			}
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				scattered.add(name);
				Location location = spawns.get(name);
				player.teleport(location);
				MessageHandler.alert("Scattered " + AccountHandler.getRank(player).getColor() + player.getName());
				if(scattered.size() >= ProPlugin.getPlayers().size()) {
					logSpawns = false;
					scattered.clear();
					scattered = null;
					spawns.clear();
					spawns = null;
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player damager = null;
			if(event.getDamager() instanceof Player) {
				damager = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					damager = (Player) projectile.getShooter();
				}
			}
			if(damager != null) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(GracePeriod.isRunning() && event.getBucket() == Material.LAVA_BUCKET) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = event.getPlayer().getItemInHand();
			if(item != null && item.getType() == Material.FLINT_AND_STEEL) {
				event.setCancelled(true);
			}
		}
	}
}
