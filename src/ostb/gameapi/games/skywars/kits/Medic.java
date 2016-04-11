package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

@SuppressWarnings("deprecation")
public class Medic extends KitBase {
	private static final int amount = 2;
	private static final int price = 800;
	
	public Medic() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(new Potion(PotionType.REGEN, 1, true).toItemStack(1)).setName("Medic").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Splash Regen Potions",
			"",
			"&7Coins: &a" + price
		}).getItemStack(), price);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new Potion(PotionType.REGEN, 1, true).toItemStack(2));
		}
	}
}
