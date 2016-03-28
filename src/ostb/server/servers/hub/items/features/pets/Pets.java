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
	private static int max = 0;
	private static Map<String, Integer> owned = null;
	
	public Pets() {
		super("Pets", 12, new ItemStack(Material.BONE), null, new String [] {
			"",
			"&cUnder development",
			"",
			"&7&mLove pets? We have one of",
			"&7&mthe largest collections around!",
			"",
			"&7&mOwned: &e&mXX&8&m/&e&m" + max + " &7&m(&e&mYY%&7&m)",
			"&7&mCollect from: &e&mZZ",
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
