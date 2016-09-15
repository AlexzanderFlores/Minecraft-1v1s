package network.gameapi.games.onevsones.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import network.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Gapple extends OneVsOneKit {
    public Gapple() {
        super("Gapple", new ItemStack(Material.GOLDEN_APPLE, 1));
        ItemStack helmet = new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).getItemStack();
        ItemStack chestplate = new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).getItemStack();
        ItemStack leggings = new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).getItemStack();
        ItemStack boots = new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).getItemStack();
        setArmor(helmet);
        setArmor(chestplate);
        setArmor(leggings);
        setArmor(boots);
        setItem(0, new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 4).getItemStack());
        setItem(1, new ItemCreator(Material.GOLDEN_APPLE, 1).setAmount(16).getItemStack());
        setItem(2, new ItemCreator(new Potion(PotionType.SPEED, 1, true).toItemStack(1)).getItemStack());
        setItem(3, new ItemCreator(new Potion(PotionType.STRENGTH, 1, true).toItemStack(1)).getItemStack());
        setItem(29, new ItemCreator(new Potion(PotionType.SPEED, 1, true).toItemStack(1)).getItemStack());
        setItem(30, new ItemCreator(new Potion(PotionType.STRENGTH, 1, true).toItemStack(1)).getItemStack());
        setItem(5, helmet);
        setItem(6, chestplate);
        setItem(7, leggings);
        setItem(8, boots);
    }
}
