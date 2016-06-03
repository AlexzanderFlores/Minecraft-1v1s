package ostb.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Chain extends OneVsOneKit {
    public Chain() {
        super("Chain", Material.CHAINMAIL_HELMET);
        setArmor(Material.CHAINMAIL_HELMET);
        setArmor(Material.CHAINMAIL_CHESTPLATE);
        setArmor(Material.CHAINMAIL_LEGGINGS);
        setArmor(Material.CHAINMAIL_BOOTS);
        setItem(0, Material.STONE_SWORD);
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, Material.BOW);
        setItem(8, new ItemStack(Material.ARROW, 6));
    }
}
