package ostb.gameapi.games.speeduhc.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ostb.OSTB.Plugins;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.SpeedUHCShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class WallBreather extends KitBase {
	private static boolean enabled = false;
	
	public WallBreather() {
		super(Plugins.SUHCK, new ItemCreator(Material.GRAVEL).setName("Wall Breather").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aCannot take suffocation damage",
			"",
			"&7Unlocked in &bSpeed UHC Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
	}
	
	public static Rarity getRarity() {
		return Rarity.RARE;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SpeedUHCShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		enabled = true;
	}
	
	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(enabled && event.getCause() == DamageCause.SUFFOCATION && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(has(player)) {
				event.setCancelled(true);
			}
		}
	}
}
