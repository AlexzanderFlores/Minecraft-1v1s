package ostb.gameapi.uhc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class GoldenHeadUtil implements Listener {
	private String name = null;
	
	public GoldenHeadUtil() {
		name = StringUtil.color("&fGolden Head");
		ShapedRecipe ultraApple = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName(name).setGlow(true).getItemStack());
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
        EventUtil.register(this);
	}
	
	@EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if(item != null && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
            if(item.getItemMeta().getDisplayName().equals(name)) {
                Player player = event.getPlayer();
                double newHealth = event.getPlayer().getHealth() + 4.0d;
                player.setHealth(newHealth > player.getMaxHealth() ? player.getMaxHealth() : newHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 0));
            }
        }
    }
}
