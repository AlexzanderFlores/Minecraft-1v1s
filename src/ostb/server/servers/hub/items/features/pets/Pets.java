package ostb.server.servers.hub.items.features.pets;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.servers.hub.items.features.FeatureBase;

public class Pets extends FeatureBase {
	private static int max = 96;
	private static Map<String, Integer> owned = null;
	
	public Pets() {
		super("Pets", 12, new ItemStack(Material.BONE), null, new String [] {
			"",
			"&cUnder development",
			"",
			"&7Love pets? We have one of",
			"&7the largest collections around!",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			""
		});
		owned = new HashMap<String, Integer>();
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			owned.put(player.getName(), 0);
		}
		return owned.get(player.getName());
	}
	
	@Override
	public int getMax() {
		return max;
	}
	
	@Override
	public void display(Player player) {
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		owned.remove(event.getPlayer().getName());
	}
}
