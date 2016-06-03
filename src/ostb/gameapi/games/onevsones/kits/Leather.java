package ostb.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Leather extends OneVsOneKit {
    public Leather() {
        super("Leather", Material.LEATHER_HELMET);
        setArmor(Material.LEATHER_HELMET);
        setArmor(Material.LEATHER_CHESTPLATE);
        setArmor(Material.LEATHER_LEGGINGS);
        setArmor(Material.LEATHER_BOOTS);
        setItem(0, Material.WOOD_SWORD);
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, Material.BOW);
        setItem(8, new ItemStack(Material.ARROW, 6));
    }
}
