package ostb.gameapi.scenarios.scenarios;

import org.bukkit.block.Block;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

import anticheat.util.EventUtil;

public class AppleRates implements Listener {
	private int rates = 0;
	
	public AppleRates(int rates) {
		this.rates = rates;
		EventUtil.register(this);
	}
	
	private boolean spawn() {
		return (new Random().nextInt(100) + 1) >= rates;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Material type = block.getType();
		if((type == Material.LEAVES || type == Material.LEAVES_2) && spawn()) {
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
		}
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		if(spawn()) {
			Block block = event.getBlock();
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
		}
	}
}
