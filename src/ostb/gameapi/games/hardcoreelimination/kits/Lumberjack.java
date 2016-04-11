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

public class Lumberjack extends KitBase {
	private static final int price = 500;
	
	public Lumberjack() {
		super(Plugins.HE_KITS, new ItemCreator(Material.WOOD_AXE).setName("Lumberjack").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aWood Axe",
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
			player.getInventory().setHelmet(new ItemStack(Material.WOOD_AXE));
		}
	}
}
