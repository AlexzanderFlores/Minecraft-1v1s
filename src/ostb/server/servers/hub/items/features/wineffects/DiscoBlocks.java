package ostb.server.servers.hub.items.features.wineffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;

import ostb.ProPlugin;
import ostb.customevents.timed.OneTickTaskEvent;
import ostb.server.servers.hub.items.features.wineffects.WinEffects.WinEffect;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class DiscoBlocks implements Listener {
	private List<FallingBlock> blocks = null;
	private Random random = null;
	
	public DiscoBlocks() {
		blocks = new ArrayList<FallingBlock>();
		random = new Random();
		EventUtil.register(this);
		final DiscoBlocks instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				HandlerList.unregisterAll(instance);
				for(World world : Bukkit.getWorlds()) {
					for(Entity entity : world.getEntities()) {
						if(entity instanceof FallingBlock) {
							entity.remove();
						}
					}
				}
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			if(WinEffects.getActiveEffect(player) == WinEffect.DISCO_BLOCKS) {
				FallingBlock block = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 3, 0), Material.WOOL, (byte) random.nextInt(15));
				block.setDropItem(false);
				double x = random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1;
				double z = random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1;
				block.setVelocity(new Vector(x, 0.5, z));
				blocks.add(block);
			}
		}
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(blocks.contains(event.getEntity())) {
			event.setCancelled(true);
		}
	}
}
