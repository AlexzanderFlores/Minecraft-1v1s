package ostb.gameapi.games.pvpbattles.mapeffects;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.PHYSICAL) {
			Player player = event.getPlayer();
			player.teleport(player.getLocation().add(0, 1, 0));
			player.setVelocity(player.getLocation().getDirection().normalize().multiply(3));
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.getTo().getY() <= 110) {
			event.getPlayer().setHealth(0);
		}
	}
}
