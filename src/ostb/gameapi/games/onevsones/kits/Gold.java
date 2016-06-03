package ostb.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Gold extends OneVsOneKit {
    public Gold() {
        super("Gold", Material.GOLD_HELMET);
        setArmor(Material.GOLD_HELMET);
        setArmor(Material.GOLD_CHESTPLATE);
        setArmor(Material.GOLD_LEGGINGS);
        setArmor(Material.GOLD_BOOTS);
        setItem(0, Material.GOLD_SWORD);
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.FLINT_AND_STEEL);
        setItem(3, Material.BOW);
        setItem(8, new ItemStack(Material.ARROW, 6));
    }
}
