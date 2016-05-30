package ostb.gameapi.scenarios.scenarios;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.scenarios.Scenario;

public class DoubleOrNothing extends Scenario {
    private static DoubleOrNothing instance = null;

    public DoubleOrNothing() {
        super("Double or Nothing", new ItemStack(Material.DIAMOND, 2));
        instance = this;
        setInfo("Whenever you mine an ore you either get 0x or 2x the amount of drops.");
    }

    public static DoubleOrNothing getInstance() {
        if(instance == null) {
            new DoubleOrNothing();
        }
        return instance;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(!event.isCancelled() && OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
            Block block = event.getBlock();
            Material type = block.getType();
            if(type.toString().toUpperCase().contains("ORE")) {
                if(new Random().nextBoolean()) {
                    for(ItemStack drop : block.getDrops()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }
                } else {
                    block.setType(Material.AIR);
                }
            }
        }
    }
}
