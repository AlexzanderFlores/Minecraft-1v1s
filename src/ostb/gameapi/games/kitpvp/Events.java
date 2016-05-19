package ostb.gameapi.games.kitpvp;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import ostb.customevents.game.CounterDecrementEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.gameapi.games.kitpvp.TeamHandler.KitTeam;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	private static boolean paused = false;
	
	public Events() {
		EventUtil.register(this);
	}
	
	public static boolean getPaused() {
		return paused;
	}
	
	@EventHandler
	public void onCounterDecrement(CounterDecrementEvent event) {
		for(KitTeam kitTeam : KitTeam.values()) {
			if(kitTeam.getSize() == 0) {
				paused = true;
				event.setCancelled(true);
				return;
			}
		}
		paused = false;
	}
	
	@EventHandler
	public void onPlayerSpectator(PlayerSpectatorEvent event) {
		Player player = event.getPlayer();
		if(!Ranks.PREMIUM.hasRank(player)) {
			event.setCancelled(true);
		} else if(KitPVP.getKitPVPTeamHandler().getTeam(player) != null && event.getState() == SpectatorState.ADDED) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			event.getEntity().remove();
		}
	}
}
