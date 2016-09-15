package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import network.server.util.ItemCreator;

public class UHC extends OneVsOneKit {
    public UHC() {
        super("UHC", Material.GOLDEN_APPLE);
        setArmor(new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
        setArmor(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).getItemStack());
        setArmor(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
        setArmor(new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
        setItem(0, new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack());
        setItem(1, Material.FISHING_ROD);
        setItem(2, Material.LAVA_BUCKET);
        setItem(3, Material.WATER_BUCKET);
        setItem(4, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE, 1).getItemStack());
        setItem(5, new ItemCreator(Material.GOLDEN_APPLE).setAmount(3).getItemStack());
        setItem(6, new ItemCreator(Material.COBBLESTONE).setAmount(64).getItemStack());
        setItem(7, Material.DIAMOND_PICKAXE);
        setItem(8, new ItemStack(Material.ARROW, 40));
    }
}
