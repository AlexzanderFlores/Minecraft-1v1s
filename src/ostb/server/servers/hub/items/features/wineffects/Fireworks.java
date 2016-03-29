package ostb.server.servers.hub.items.features.wineffects;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.server.servers.hub.items.features.wineffects.WinEffects.WinEffect;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class Fireworks implements Listener {
	public Fireworks() {
		EventUtil.register(this);
		final Fireworks instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				HandlerList.unregisterAll(instance);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 15) {
			for(Player player : ProPlugin.getPlayers()) {
				if(WinEffects.getActiveEffect(player) == WinEffect.FIREWORKS) {
					EffectUtil.launchFirework(player.getLocation().add(0, 2, 0));
				}
			}
		}
	}
}
