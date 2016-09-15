package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Iron extends OneVsOneKit {
    public Iron() {
        super("Iron", Material.IRON_HELMET);
        setArmor(Material.IRON_HELMET);
        setArmor(Material.IRON_CHESTPLATE);
        setArmor(Material.IRON_LEGGINGS);
        setArmor(Material.IRON_BOOTS);
        setItem(0, Material.IRON_SWORD);
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, Material.BOW);
        setItem(8, new ItemStack(Material.ARROW, 6));
    }
}
