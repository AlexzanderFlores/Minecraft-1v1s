package ostb.gameapi.games.speeduhc.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ostb.OSTB.Plugins;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Haste extends KitBase {
	public Haste() {
		super(Plugins.SUHCK, new ItemCreator(Material.DIAMOND_PICKAXE).setName("Haste").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aGet Haste for the first 2:30",
			"",
			"&7Unlocked in &bHardcore Elimination Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.RARE;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (20 * 60 * 2) + (20 * 30), 1));
		}
	}
	
	@Override
	public void execute(Player player) {
		
	}
}
