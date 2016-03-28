package ostb.server.servers.hub.items.features.wineffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import ostb.ProPlugin;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.customevents.timed.OneTickTaskEvent;
import ostb.server.servers.hub.items.features.wineffects.WinEffects.WinEffect;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class FireDiscoItems implements Listener {
	private List<Item> items = null;
	private Random random = null;
	
	public FireDiscoItems() {
		items = new ArrayList<Item>();
		random = new Random();
		EventUtil.register(this);
		final FireDiscoItems instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				HandlerList.unregisterAll(instance);
				for(World world : Bukkit.getWorlds()) {
					for(Entity entity : world.getEntities()) {
						if(entity instanceof Item) {
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
			if(WinEffects.getActiveEffect(player) == WinEffect.FIRE_DISCO_ITEMS) {
				Item item = player.getWorld().dropItem(player.getLocation().add(0, 3, 0), new ItemStack(Material.WOOL, 1, (byte) random.nextInt(15)));
				double x = random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1;
				double z = random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1;
				item.setVelocity(new Vector(x, 0.5, z));
				item.setFireTicks(20 * 5);
				items.add(item);
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if(event.getEntity().getItemStack().getType() == Material.WOOL) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Item item : items) {
			if(item.getTicksLived() > 10) {
				item.remove();
			}
		}
	}
}
