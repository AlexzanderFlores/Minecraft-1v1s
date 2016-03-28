package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.skywars.SkyWarsShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Archer extends KitBase {
	private static final int amount = 6;
	private static final int price = 1000;
	
	public Archer() {
		super(Plugins.SKY_WARS, new ItemCreator(Material.BOW).setName("Archer").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aBow",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Arrows",
			"",
			"&7Coins: &a" + price
		}).getItemStack(), price, 19);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemStack(Material.BOW));
			player.getInventory().addItem(new ItemStack(Material.ARROW, amount));
		}
	}
}
