package ostb.gameapi.scenarios.scenarios;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.scenarios.Scenario;

public class OreMultipliers extends Scenario {
	private static OreMultipliers instance = null;
	private static int multiplier = 0;
	
	public OreMultipliers() {
		instance = this;
		enable(true);
	}
	
	public static void setMultiplier(int multiplier) {
		if(instance == null) {
			instance = new OreMultipliers();
		}
		OreMultipliers.multiplier = multiplier;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			Block block = event.getBlock();
			if(multiplier >= 2 && block.getType().toString().endsWith("_ORE")) {
				for(int a = 0; a < multiplier - 1; ++a) {
					for(ItemStack drop : block.getDrops()) {
						block.getWorld().dropItem(block.getLocation(), drop);
					}
				}
			}
		}
	}
}
