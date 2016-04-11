package ostb.gameapi.games.hardcoreelimination.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class WallBreather extends KitBase {
	private static boolean enabled = false;
	
	public WallBreather() {
		super(Plugins.HE_KITS, new ItemCreator(Material.GRAVEL).setName("Wall Breather").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aCannot take suffocation damage",
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
		EventUtil.register(this);
		enabled = true;
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
