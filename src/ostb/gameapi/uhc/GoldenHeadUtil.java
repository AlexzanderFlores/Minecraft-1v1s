package ostb.gameapi.uhc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;

import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class GoldenHeadUtil {
	public GoldenHeadUtil() {
		ShapedRecipe ultraApple = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName("&fGolden Head").setGlow(true).getItemStack());
        ultraApple.shape("012", "345", "678");
        ultraApple.setIngredient('0', Material.GOLD_INGOT);
        ultraApple.setIngredient('1', Material.GOLD_INGOT);
        ultraApple.setIngredient('2', Material.GOLD_INGOT);
        ultraApple.setIngredient('3', Material.GOLD_INGOT);
        ultraApple.setIngredient('4', Material.SKULL_ITEM, 3);
        ultraApple.setIngredient('5', Material.GOLD_INGOT);
        ultraApple.setIngredient('6', Material.GOLD_INGOT);
        ultraApple.setIngredient('7', Material.GOLD_INGOT);
        ultraApple.setIngredient('8', Material.GOLD_INGOT);
        Bukkit.getServer().addRecipe(ultraApple);
	}
}
