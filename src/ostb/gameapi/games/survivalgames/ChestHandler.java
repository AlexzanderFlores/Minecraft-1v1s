package ostb.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.customevents.TimeEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.player.TitleDisplayer;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class ChestHandler implements Listener {
    private List<Block> oppenedChests = null;
    private List<Material> possibleItems = null;
    private static int restockCounter = 60 * 10;
    private enum Rarity {
        COMMON, UNCOMMON, RARE
    }

    public ChestHandler() {
        oppenedChests = new ArrayList<Block>();
        possibleItems = new ArrayList<Material>();
        addItem(Material.LEATHER_HELMET, Rarity.COMMON);
        addItem(Material.LEATHER_CHESTPLATE, Rarity.COMMON);
        addItem(Material.LEATHER_LEGGINGS, Rarity.COMMON);
        addItem(Material.LEATHER_BOOTS, Rarity.COMMON);
        addItem(Material.GOLD_HELMET, Rarity.COMMON);
        addItem(Material.GOLD_CHESTPLATE, Rarity.COMMON);
        addItem(Material.GOLD_LEGGINGS, Rarity.COMMON);
        addItem(Material.GOLD_BOOTS, Rarity.COMMON);
        addItem(Material.WOOD_AXE, Rarity.COMMON);
        addItem(Material.WOOD_SWORD, Rarity.COMMON);
        addItem(Material.GOLD_AXE, Rarity.COMMON);
        addItem(Material.GOLD_SWORD, Rarity.COMMON);
        addItem(Material.STONE_AXE, Rarity.COMMON);
        addItem(Material.STONE_SWORD, Rarity.COMMON);
        addItem(Material.EXP_BOTTLE, Rarity.COMMON);
        addItem(Material.FISHING_ROD, Rarity.COMMON);
        addItem(Material.RAW_FISH, Rarity.COMMON);
        addItem(Material.COOKED_FISH, Rarity.COMMON);
        addItem(Material.COOKIE, Rarity.COMMON);
        addItem(Material.PORK, Rarity.COMMON);
        addItem(Material.GRILLED_PORK, Rarity.COMMON);
        addItem(Material.RAW_BEEF, Rarity.COMMON);
        addItem(Material.COOKED_CHICKEN, Rarity.COMMON);
        addItem(Material.BAKED_POTATO, Rarity.COMMON);
        addItem(Material.BREAD, Rarity.COMMON);
        addItem(Material.APPLE, Rarity.COMMON);
        addItem(Material.PUMPKIN_PIE, Rarity.COMMON);
        addItem(Material.CARROT_ITEM, Rarity.COMMON);
        addItem(Material.CAKE, Rarity.COMMON);
        addItem(Material.ARROW, Rarity.COMMON);
        addItem(Material.FLINT, Rarity.COMMON);
        addItem(Material.STICK, Rarity.COMMON);
        addItem(Material.FEATHER, Rarity.COMMON);
        addItem(Material.GOLD_INGOT, Rarity.COMMON);
        addItem(Material.CHAINMAIL_HELMET, Rarity.UNCOMMON);
        addItem(Material.CHAINMAIL_CHESTPLATE, Rarity.UNCOMMON);
        addItem(Material.CHAINMAIL_LEGGINGS, Rarity.UNCOMMON);
        addItem(Material.CHAINMAIL_BOOTS, Rarity.UNCOMMON);
        addItem(Material.IRON_AXE, Rarity.UNCOMMON);
        addItem(Material.WEB, Rarity.UNCOMMON);
        addItem(Material.BOW, Rarity.RARE);
        addItem(Material.STONE_SWORD, Rarity.RARE);
        addItem(Material.FLINT_AND_STEEL, Rarity.RARE);
        addItem(Material.GOLDEN_CARROT, Rarity.RARE);
        addItem(Material.IRON_HELMET, Rarity.RARE);
        addItem(Material.IRON_LEGGINGS, Rarity.RARE);
        addItem(Material.IRON_BOOTS, Rarity.RARE);
        addItem(Material.IRON_INGOT, Rarity.RARE);
        EventUtil.register(this);
    }

    private void addItem(Material material, Rarity rarity) {
        for(int a = 0; a < (rarity == Rarity.COMMON ? 4 : rarity == Rarity.UNCOMMON ? 3 : rarity == Rarity.RARE ? 2 : 1); ++a) {
            possibleItems.add(material);
        }
    }

    public static int getRestockCounter() {
		return restockCounter;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 && OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
			if(--restockCounter <= 0) {
				TimeEvent.getHandlerList().unregister(this);
				oppenedChests.clear();
				for(Player player : Bukkit.getOnlinePlayers()) {
					new TitleDisplayer(player, "&eChests Restocked").display();
					EffectUtil.playSound(player, Sound.CHEST_OPEN);
				}
			}
		}
	}

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryType type = event.getInventory().getType();
        if(type != InventoryType.CHEST && type != InventoryType.WORKBENCH && type != InventoryType.ENCHANTING) {
            event.setCancelled(true);
        } else if(type == InventoryType.ENCHANTING) {
        	event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (byte) 4));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(oppenedChests != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !SpectatorHandler.contains(event.getPlayer())) {
            Block block = event.getClickedBlock();
            if((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) && !oppenedChests.contains(block)) {
                if(OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
                    oppenedChests.add(block);
                    Chest chest = (Chest) block.getState();
                    chest.getInventory().clear();
                    Random random = new Random();
                    int numberOfTimes = random.nextInt(7) + 2;
                    for(int a = 0; a < numberOfTimes; ++a) {
                        Material type = null;
                        type = possibleItems.get(random.nextInt(possibleItems.size()));
                        ItemStack itemStack = new ItemStack(type);
                        if(type == Material.ARROW || type == Material.GOLD_INGOT || type == Material.STICK || type == Material.FEATHER || type.isEdible()) {
                            if(type == Material.ARROW) {
                                itemStack = new ItemStack(type, random.nextInt(5) + 3);
                            } else {
                                itemStack = new ItemStack(type, random.nextInt(2) + 2);
                            }
                        }
                        if(chest instanceof DoubleChest) {
                            DoubleChest doubleChest = (DoubleChest) chest;
                            DoubleChestInventory inventory = (DoubleChestInventory) doubleChest.getInventory();
                            inventory.setItem(random.nextInt(inventory.getSize()), itemStack);
                        } else {
                            chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), itemStack);
                        }
                    }
                }
            }
        }
    }
}
