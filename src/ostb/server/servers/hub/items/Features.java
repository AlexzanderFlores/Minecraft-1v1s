package ostb.server.servers.hub.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.servers.hub.items.features.Armor;
import ostb.server.servers.hub.items.features.FeatureBase;
import ostb.server.servers.hub.items.features.Gadgets;
import ostb.server.servers.hub.items.features.particles.ArrowTrails;
import ostb.server.servers.hub.items.features.particles.HaloParticles;
import ostb.server.servers.hub.items.features.pets.Pets;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

public class Features extends HubItemBase {
	private static List<String> opened = null;
	private static List<String> delayed = null;
	private static int [] slots;
	private static byte [] colors;
	private static byte last = 0;
	private static final int delay = 1;
	private static String name = null;
	public enum Rarity {
		COMMON("&2Common"), UNCOMMON("&6Uncommon"), RARE("&4Rare");
		
		private String name = null;
		
		private Rarity(String name) {
			this.name = name;
		}
		
		public String getName() {
			return StringUtil.color(this.name);
		}
	}
	
	public Features() {
		super(new ItemCreator(Material.EMERALD).setName("&eFeatures"), 1);
		opened = new ArrayList<String>();
		delayed = new ArrayList<String>();
		name = getName();
		new Pets();
		new HaloParticles();
		new ArrowTrails();
		new Armor();
		new Gadgets();
		slots = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 18, 9};
		colors = new byte [] {3, 4, 5, 6};
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		giveItem(player);
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		final Player player = event.getPlayer();
		if(isItem(player) && !delayed.contains(player.getName())) {
			delayed.add(player.getName());
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(player.getName());
				}
			}, 20 * delay);
			//KeyMerchant.removeSelling(player);
			open(player);
			EffectUtil.playSound(player, Sound.CHEST_OPEN);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(opened.contains(event.getPlayer().getName())) {
			Player player = event.getPlayer();
			String item = ChatColor.stripColor(event.getItemTitle());
			FeatureBase feature = FeatureBase.getFeature(item);
			player.closeInventory();
			if(feature != null) {
				feature.display(player);
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 5) {
			if(opened != null && !opened.isEmpty()) {
				Random random = new Random();
				byte data = colors[0];
				do {
					data = colors[random.nextInt(colors.length)];
				} while(last == data);
				last = data;
				for(String name : opened) {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						InventoryView inventory = player.getOpenInventory();
						for(int slot : slots) {
							inventory.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, (byte) data).setGlow(true).setName(" ").getItemStack());
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		opened.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		opened.remove(event.getPlayer().getName());
	}
	
	public static void open(final Player player) {
		player.closeInventory();
		opened.remove(player.getName());
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Inventory inventory = Bukkit.createInventory(player, 9 * 4, ChatColor.stripColor(name));
				for(FeatureBase feature : FeatureBase.getFeatures()) {
					inventory.setItem(feature.getSlot(), feature.getItemStack(player));
				}
				player.openInventory(inventory);
				opened.add(player.getName());
			}
		});
	}
}
