package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Pyro extends KitBase {
	private static final int amount = 2;
	
	public Pyro() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.FLINT_AND_STEEL).setName("Pyro").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Flint and Steel (4 uses)",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.COMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			for(int a = 0; a < amount; ++a) {
				player.getInventory().addItem(new ItemStack(Material.FLINT_AND_STEEL));
			}
		}
	}
}
