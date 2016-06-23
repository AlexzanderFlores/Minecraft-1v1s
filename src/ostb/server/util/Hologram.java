package ostb.server.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public abstract class Hologram implements Listener {
	private ArmorStand armorStand = null;
	
	public Hologram(Location location, String name) {
		armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
		armorStand.setCustomName(StringUtil.color(name));
		EventUtil.register(this);
	}
	
	public abstract void interact(Player player);
	
	public ArmorStand getArmorStand() {
		return armorStand;
	}
	
	public void setText(String text) {
		armorStand.setCustomName(StringUtil.color(text));
	}
	
	public String getText() {
		return armorStand.getCustomName();
	}
	
	public void remove() {
		if(armorStand != null) {
			armorStand.remove();
			armorStand = null;
		}
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof ArmorStand && event.getEntity().equals(armorStand)) {
			if(event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				interact(player);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if(event.getRightClicked() instanceof ArmorStand && event.getRightClicked().equals(armorStand)) {
			interact(event.getPlayer());
			event.setCancelled(true);
		}
	}
}
