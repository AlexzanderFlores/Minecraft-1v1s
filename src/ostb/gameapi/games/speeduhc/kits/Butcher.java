package ostb.gameapi.games.speeduhc.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import ostb.OSTB.Plugins;
import ostb.customevents.game.PostGameStartEvent;
import ostb.gameapi.kit.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Butcher extends KitBase {
	private static final int amount = 5;
	private List<Pig> pigs = null;
	
	public Butcher() {
		super(Plugins.SUHCK, new ItemCreator(Material.GRILLED_PORK).setName("Butcher").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Pigs spawn near you",
			"",
			"&7Unlocked in &bHardcore Elimination Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1, 20);
	}
	
	public static Rarity getRarity() {
		return Rarity.UNCOMMON;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		
	}
	
	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onPostGameStart(PostGameStartEvent event) {
		Random random = new Random();
		for(Player player : getPlayers()) {
			double x = random.nextInt(10);
			double z = random.nextInt(10);
			if(random.nextBoolean()) {
				x *= -1;
			}
			if(random.nextBoolean()) {
				z *= -1;
			}
			Location location = player.getLocation().add(x, 10, z);
			Pig pig = (Pig) player.getWorld().spawnEntity(location, EntityType.PIG);
			if(pigs == null) {
				pigs = new ArrayList<Pig>();
			}
			pigs.add(pig);
		}
		final Butcher instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				HandlerList.unregisterAll(instance);
			}
		}, 20 * 10);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntityType() == EntityType.PIG && pigs != null && event.getCause() == DamageCause.FALL) {
			Pig pig = (Pig) event.getEntity();
			if(pigs.contains(pig)) {
				event.setCancelled(true);
			}
		}
	}
}
