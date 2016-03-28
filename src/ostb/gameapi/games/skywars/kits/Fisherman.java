package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.skywars.SkyWarsShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Fisherman extends KitBase {
	private static final int amount = 1;
	private static final int price = 500;
	
	public Fisherman() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.FISHING_ROD).setName("Fisherman").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aKnockback " + amount + " Fishingrod",
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
			player.getInventory().addItem(new ItemCreator(Material.FISHING_ROD).addEnchantment(Enchantment.KNOCKBACK).getItemStack());
		}
	}
}
