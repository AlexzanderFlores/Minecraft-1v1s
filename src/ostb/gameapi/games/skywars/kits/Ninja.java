package ostb.gameapi.games.skywars.kits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.MouseClickEvent.ClickType;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Ninja extends KitBase {
	private static final int amount = 10;
	private static boolean enabled = false;
	private List<String> delayed = null;
	private List<Item> stars = null;
	private static final long delay = 5;
	private Random random = null;
	
	public Ninja() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.NETHER_STAR).setName("Ninja").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " Throwing Stars",
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aClick to throw star",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aStars deal 0 or .5 damage each",
			"",
			"&7Unlocked in &bSky Wars Crate",
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
			player.getInventory().addItem(new ItemCreator(Material.NETHER_STAR).setAmount(amount).setName("&fThrowing Star").getItemStack());
		}
		delayed = new ArrayList<String>();
		stars = new ArrayList<Item>();
		random = new Random();
		if(!enabled) {
			EventUtil.register(this);
		}
		enabled = true;
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(enabled && event.getClickType() == ClickType.RIGHT_CLICK) {
			Player player = event.getPlayer();
			if(!delayed.contains(player.getName())) {
				final String name = player.getName();
				delayed.add(name);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(name);
					}
				}, delay);
				ItemStack itemStack = player.getItemInHand();
				if(itemStack != null && itemStack.getType() == Material.NETHER_STAR) {
					Location location = player.getLocation().add(0, 1.5, 0).add(player.getLocation().getDirection().multiply(1.5d));
					Item star = player.getWorld().dropItem(location, new ItemStack(Material.NETHER_STAR));
					star.setVelocity(player.getLocation().getDirection().multiply(2.0d));
					stars.add(star);
					int amount = itemStack.getAmount() - 1;
					if(amount <= 0) {
						player.setItemInHand(new ItemStack(Material.AIR));
					} else {
						player.setItemInHand(new ItemCreator(itemStack).setAmount(amount).getItemStack());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			if(enabled) {
				Iterator<Item> iterator = stars.iterator();
				while(iterator.hasNext()) {
					Item item = iterator.next();
					if(item.isOnGround() || item.getLocation().getY() <= 0) {
						item.remove();
						iterator.remove();
					} else {
						for(Entity near : item.getNearbyEntities(0.5, 0.5, 0.5)) {
							if(near instanceof Player) {
								Player nearPlayer = (Player) near;
								if(random.nextBoolean()) {
									nearPlayer.damage(1.0d);
								} else {
									nearPlayer.damage(0.0d);
								}
								nearPlayer.setVelocity(item.getVelocity().multiply(.50d));
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDespawn(ItemDespawnEvent event) {
		event.setCancelled(false);
	}
}
