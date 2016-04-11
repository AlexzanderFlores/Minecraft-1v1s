package ostb.gameapi.games.hardcoreelimination.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Enchanter extends KitBase {
	private static final int price = 1000;
	
	public Enchanter() {
		super(Plugins.HE_KITS, new ItemCreator(Material.ENCHANTMENT_TABLE).setName("Enchanter").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a3 Sugar Cane",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a1 Leather",
			"",
			"&7Coins: &a" + price
		}).getItemStack(), price, 29);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().setHelmet(new ItemStack(Material.SUGAR_CANE, 3));
			player.getInventory().setHelmet(new ItemStack(Material.LEATHER));
		}
	}
}
