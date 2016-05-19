package ostb.gameapi.games.kitpvp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerSpectator(PlayerSpectatorEvent event) {
		Player player = event.getPlayer();
		if(KitPVP.getKitPVPTeamHandler().getTeam(player) != null && event.getState() == SpectatorState.ADDED) {
			event.setCancelled(true);
		}
	}
}
