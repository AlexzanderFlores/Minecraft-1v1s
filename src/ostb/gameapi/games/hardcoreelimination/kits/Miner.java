package ostb.gameapi.games.hardcoreelimination.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Miner extends KitBase {
	public Miner() {
		super(Plugins.HE_KITS, new ItemCreator(Material.STONE_PICKAXE).setName("Miner").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aWood Pickaxe",
			"",
			"&7Unlocked in &bHardcore Elimination Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.COMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().setHelmet(new ItemStack(Material.STONE_PICKAXE));
		}
	}
	
	@Override
	public void execute(Player player) {
		
	}
}
