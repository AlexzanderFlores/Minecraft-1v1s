package ostb.gameapi.games.onevsones.kits;

import org.bukkit.Material;

public class Swordsman extends OneVsOneKit {
    public Swordsman() {
        super("Swordsman", Material.IRON_SWORD);
        setArmor(Material.CHAINMAIL_HELMET);
        setArmor(Material.IRON_CHESTPLATE);
        setArmor(Material.CHAINMAIL_LEGGINGS);
        setArmor(Material.CHAINMAIL_BOOTS);
        setItem(0, Material.IRON_SWORD);
    }
}
