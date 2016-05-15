package ostb.gameapi.games.speeduhc.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.SpeedUHCShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Enchanter extends KitBase {
	public Enchanter() {
		super(Plugins.SUHCK, new ItemCreator(Material.ENCHANTMENT_TABLE).setName("Enchanter").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a3 Sugar Cane",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a1 Leather",
			"",
			"&7Unlocked in &bSpeed UHC Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1, 29);
	}
	
	public static Rarity getRarity() {
		return Rarity.UNCOMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SpeedUHCShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().setHelmet(new ItemStack(Material.SUGAR_CANE, 3));
			player.getInventory().setHelmet(new ItemStack(Material.LEATHER));
		}
	}
	
	@Override
	public void execute(Player player) {
		
	}
}
