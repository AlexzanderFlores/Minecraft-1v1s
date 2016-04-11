package ostb.player;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import ostb.customevents.TimeEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class LevelGiver implements Listener {
	private final Player player;
	
	public LevelGiver(Player player) {
		this.player = player;
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			if(player.isOnline() && !SpectatorHandler.contains(player)) {
				player.setExp(player.getExp() + 0.025f);
				if(player.getExp() > 1.0f) {
					player.setExp(0.0f);
					player.setLevel(player.getLevel() + 1);
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				} else {
					return;
				}
			}
			HandlerList.unregisterAll(this);
		}
	}
}
