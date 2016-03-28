package ostb.gameapi.games.hardcoreelimination;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.TitleDisplayer;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
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
						new TitleDisplayer(player, "&cPVP", game.getCounterAsString()).setFadeIn(0).setStay(15).setFadeOut(60).display();
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
			player.setNoDamageTicks(20 * 15);
			player.teleport(WorldHandler.getWorld().getSpawnLocation());
			command += player.getName() + " ";
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
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
