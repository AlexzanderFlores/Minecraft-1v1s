package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Diamond extends OneVsOneKit {
    public Diamond() {
        super("Diamond", Material.DIAMOND_HELMET);
        setArmor(Material.DIAMOND_HELMET);
        setArmor(Material.DIAMOND_CHESTPLATE);
        setArmor(Material.DIAMOND_LEGGINGS);
        setArmor(Material.DIAMOND_BOOTS);
        setItem(0, Material.DIAMOND_SWORD);
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, Material.BOW);
        setItem(8, new ItemStack(Material.ARROW, 6));
    }
}
