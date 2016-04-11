package ostb.gameapi.games.hardcoreelimination.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Haste extends KitBase {
	public Haste() {
		super(Plugins.HE_KITS, new ItemCreator(Material.DIAMOND_PICKAXE).setName("Haste").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aGet Haste for the first 2:30",
			"",
			"&7Unlocked in &bHardcore Elimination Crate"
		}).getItemStack(), -1);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (20 * 60 * 2) + (20 * 30), 1));
		}
	}
}
