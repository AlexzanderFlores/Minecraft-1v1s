package ostb.server.servers.hub.items.features.wineffects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.servers.hub.items.features.wineffects.WinEffects.WinEffect;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class FlyEnderDragon implements Listener {
	public FlyEnderDragon() {
		EventUtil.register(this);
		for(Player player : ProPlugin.getPlayers()) {
			if(WinEffects.getActiveEffect(player) == WinEffect.FLY_ENDER_DRAGON) {
				EnderDragon enderDragon = (EnderDragon) player.getWorld().spawnEntity(player.getLocation(), EntityType.ENDER_DRAGON);
				enderDragon.setPassenger(player);
			}
		}
		final FlyEnderDragon instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(World world : Bukkit.getWorlds()) {
					for(Entity entity : world.getEntities()) {
						if(entity instanceof EnderDragon) {
							entity.remove();
						}
					}
				}
				HandlerList.unregisterAll(instance);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long time = event.getTicks();
		if(time == 1) {
			for(Player player : ProPlugin.getPlayers()) {
				if(WinEffects.getActiveEffect(player) == WinEffect.FLY_ENDER_DRAGON) {
					if(player.getVehicle() != null && player.getVehicle() instanceof EnderDragon) {
						EnderDragon dragon = (EnderDragon) player.getVehicle();
						dragon.setVelocity(player.getLocation().getDirection());
						Location location = dragon.getLocation();
						CraftEnderDragon craftEnderDragon = (CraftEnderDragon) dragon;
						craftEnderDragon.getHandle().setPositionRotation(location.getX(), location.getY(), location.getZ(), player.getLocation().getYaw() + 180, player.getLocation().getPitch() * -1);
					} else {
						for(Entity near : player.getNearbyEntities(10, 10, 10)) {
							if(near instanceof EnderDragon) {
								EnderDragon dragon = (EnderDragon) near;
								if(dragon.getPassenger() == null) {
									dragon.setPassenger(player);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(WinEffects.getActiveEffect(player) == WinEffect.FLY_ENDER_DRAGON) {
			for(Entity near : player.getNearbyEntities(5, 5, 5)) {
				if(near instanceof EnderDragon) {
					EnderDragon dragon = (EnderDragon) near;
					if(dragon.getPassenger() == null) {
						dragon.remove();
					}
				}
			}
		}
	}
}
