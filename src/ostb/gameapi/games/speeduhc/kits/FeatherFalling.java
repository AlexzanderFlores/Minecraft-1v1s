package ostb.gameapi.games.speeduhc.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ostb.OSTB.Plugins;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class FeatherFalling extends KitBase {
	private static boolean enabled = false;
	
	public FeatherFalling() {
		super(Plugins.SPEED_UHC_KITS, new ItemCreator(Material.DIAMOND_BOOTS).setName("Feather Falling").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aTake .5 less fall damage",
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
		enabled = true;
	}
	
	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(enabled && event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(has(player)) {
				double damage = event.getDamage();
				if(damage > 1) {
					event.setDamage(damage - 1);
				} else {
					event.setCancelled(true);
				}
			}
		}
	}
}
