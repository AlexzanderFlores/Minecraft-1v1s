package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Miner extends KitBase {
	public Miner() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.WOOD_PICKAXE).setName("Miner").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aWood Sword",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aWood Pickaxe",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aWood Axe",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aWood Shovel",
			"",
			"&7Unlocked in &bSky Wars Crate"
		}).getItemStack(), -1);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
			player.getInventory().addItem(new ItemStack(Material.WOOD_PICKAXE));
			player.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
			player.getInventory().addItem(new ItemStack(Material.WOOD_SPADE));
		}
	}
}
