package ostb.server.servers.hub.items.features.wineffects;

import java.util.Random;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFirework;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.EntityFireworks;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.server.servers.hub.items.features.wineffects.WinEffects.WinEffect;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class FireworkFrenzy implements Listener {
	private Random random = null;
	
	public FireworkFrenzy() {
		random = new Random();
		EventUtil.register(this);
		final FireworkFrenzy instance = this;
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
		if(ticks == 5) {
			for(Player player : ProPlugin.getPlayers()) {
				if(WinEffects.getActiveEffect(player) == WinEffect.FIREWORK_FRENZY) {
					Firework firework = EffectUtil.launchFirework(player.getLocation().add(0, 2, 0));
					Vector vector = firework.getLocation().getDirection();
					vector.setX(random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1);
					vector.setY(0.5);
					vector.setZ(random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1);
					firework.setVelocity(vector);
					CraftFirework craftFirework = (CraftFirework) firework;
					EntityFireworks entityFireworks = craftFirework.getHandle();
					entityFireworks.expectedLifespan = 10;
				}
			}
		}
	}
}
