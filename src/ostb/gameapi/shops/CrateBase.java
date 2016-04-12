package ostb.gameapi.shops;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.KitBase;
import ostb.player.MessageHandler;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class CrateBase implements Listener {
	private int [] slots = null;
	private Random random = new Random();
	private Player player = null;
	private String  title = null;
	private List<ItemStack> items = null;
	private int glassSpeed = 2;
	private int tickSpeed = 2;
	private int start = 20;
	private int end = 25;
	private int counter = 0;
	private float pitch = 1000.0f;
	private boolean displaying = false;
	
	public CrateBase(Player player, String  title, List<ItemStack> items) {
		slots = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
		random = new Random();
		this.player = player;
		this.title = title;
		this.items = items;
		Inventory inventory = Bukkit.createInventory(player, 9 * 5, title);
		inventory.setItem(13, new ItemCreator(Material.HOPPER).setName(" ").getItemStack());
		inventory.setItem(31, new ItemCreator(Material.LONG_GRASS, 2).setName(" ").getItemStack());
		player.openInventory(inventory);
		EventUtil.register(this);
	}
	
	private void remove() {
		HandlerList.unregisterAll(this);
		slots = null;
		random = null;
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView.getTitle().equals(title)) {
			player.closeInventory();
		}
		player = null;
		title = null;
		items = null;
	}
	
	public List<ItemStack> getItems() {
		return items;
	}
	
	public void placeGlass() {
		InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView != null && inventoryView.getTitle().equals(title)) {
			byte data = 0;
			if(displaying) {
				do {
					data = (byte) random.nextInt(15);
				} while(data == 8);
			}
			for(int slot : slots) {
				if(!displaying) {
					do {
						data = (byte) random.nextInt(15);
					} while(data == 8);
				}
				inventoryView.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, data).setName(" ").getItemStack());
			}
		} else {
			remove();
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(!displaying) {
			if(ticks == glassSpeed) {
				placeGlass();
			}
			if(ticks == tickSpeed) {
				EffectUtil.playSound(player, Sound.NOTE_PIANO, pitch);
				InventoryView inventoryView = player.getOpenInventory();
				if(inventoryView != null && inventoryView.getTitle().equals(title)) {
					for(int a = start; a < end; ++a) {
						inventoryView.setItem(a, items.get(random.nextInt(items.size())));
					}
				}
			}
			if(ticks == 20) {
				if(counter == 5) {
					tickSpeed = 7;
				} else if(counter == 8) {
					tickSpeed = 14;
				} else if(counter > 10) {
					InventoryView inventoryView = player.getOpenInventory();
					inventoryView.setItem(13, new ItemStack(Material.AIR));
					inventoryView.setItem(31, new ItemStack(Material.AIR));
					displaying = true;
					counter = -1;
				}
				++counter;
			}
		} else {
			if(ticks == 5) {
				placeGlass();
			} else if(ticks == 20) {
				InventoryView inventoryView = player.getOpenInventory();
				if(inventoryView != null && inventoryView.getTitle().equals(title)) {
					if(counter == 1) {
						inventoryView.setItem(20, new ItemStack(Material.AIR));
						inventoryView.setItem(24, new ItemStack(Material.AIR));
						EffectUtil.playSound(player, random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2);
					} else if(counter == 2) {
						inventoryView.setItem(21, new ItemStack(Material.AIR));
						inventoryView.setItem(23, new ItemStack(Material.AIR));
						EffectUtil.playSound(player, random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2);
					} else if(counter == 3) {
						EffectUtil.playSound(player, Sound.LEVEL_UP);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								InventoryView inventoryView = player.getOpenInventory();
								KitBase won = null;
								String wonName = ChatColor.stripColor(inventoryView.getItem(22).getItemMeta().getDisplayName());
								for(KitBase kit : KitBase.getKits()) {
									if(ChatColor.stripColor(kit.getName()).equals(wonName)) {
										won = kit;
										break;
									}
								}
								if(won == null) {
									MessageHandler.sendMessage(player, "&cThere was an error in giving you your kit, please report this (&e\"" + wonName + "&e\"&c)");
								} else {
									won.giveKit(player);
								}
								remove();
							}
						}, 20 * 3);
					}
					++counter;
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer().getName().equals(this.player.getName())) {
			InventoryView inventoryView = player.getOpenInventory();
			if(inventoryView != null && inventoryView.getTitle().equals(title)) {
				remove();
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(event.getPlayer().getName().equals(player.getName())) {
			remove();
		}
	}
}
