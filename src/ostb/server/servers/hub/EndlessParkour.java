package ostb.server.servers.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class EndlessParkour implements Listener {
	private List<String> delayed = null;
	private Map<String, Block> blocks = null;
	private int counter = 0;
	private Random random = null;
	
	public EndlessParkour() {
		blocks = new HashMap<String, Block>();
		delayed = new ArrayList<String>();
		random = new Random();
		EventUtil.register(this);
	}
	
	private boolean start(Player player) {
		if(counter <= 0) {
			counter = 10;
			blocks.put(player.getName(), Bukkit.getWorlds().get(0).getBlockAt(1586, 4, -1263));
			place(player.getName());
			Location location = blocks.get(player.getName()).getLocation().add(0, 1, 0);
			location.setYaw(-270.0f);
			location.setPitch(0.0f);
			player.teleport(location);
			return true;
		} else if(!delayed.contains(player.getName())) {
			final String name = player.getName();
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * 5);
		}
		return false;
	}
	
	private void place(String name) {
		final Block oldBlock = blocks.get(name);
		int offsetZ = random.nextBoolean() ? random.nextInt(1) + 1 : (random.nextInt(1) + 1) * -1;
		Block newBlock = oldBlock.getRelative(-5, 0, offsetZ);
		newBlock.setType(Material.STAINED_GLASS);
		newBlock.setData((byte) 5);
		for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1)}) {
			newBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()).setType(Material.STAINED_GLASS);
			newBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()).setData((byte) 5);
		}
		blocks.put(name, newBlock);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				oldBlock.setType(Material.AIR);
				oldBlock.setData((byte) 0);
				for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1)}) {
					oldBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()).setType(Material.AIR);
					oldBlock.setData((byte) 0);
				}
			}
		}, 20 * 2);
	}
	
	private void remove(Player player) {
		if(blocks.containsKey(player.getName())) {
			Block block = blocks.get(player.getName());
			blocks.remove(player.getName());
			block.setType(Material.AIR);
			block.setData((byte) 0);
			for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1)}) {
				block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()).setType(Material.AIR);
				block.setData((byte) 0);
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 18) {
			for(String name : blocks.keySet()) {
				place(name);
			}
		} else if(ticks == 20) {
			--counter;
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		int x = to.getBlockX();
		if(x == 1588) {
			int y = to.getBlockY();
			if(y == 5) {
				int z = to.getBlockZ();
				if(z >= -1264 && z <= -1262) {
					if(!start(event.getPlayer())) {
						
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && event.getCause() == DamageCause.VOID) {
			Player player = (Player) event.getEntity();
			remove(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
