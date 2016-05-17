package ostb.gameapi.games.domination.mapeffects;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ostb.OSTB;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.mapeffects.MapEffectsBase;
import ostb.server.util.EventUtil;

public class Divided_Kingdom extends MapEffectsBase implements Listener {
	public Divided_Kingdom() {
		super("Divided_Kingdom");
	}

	@Override
	public void execute(World world) {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.getTo().getY() <= 110 && event.getTo().getWorld().getName().equals(OSTB.getMiniGame().getMap().getName())) {
			Player player = event.getPlayer();
			if(!SpectatorHandler.contains(player)) {
				player.setHealth(0);
			}
		}
	}
}
