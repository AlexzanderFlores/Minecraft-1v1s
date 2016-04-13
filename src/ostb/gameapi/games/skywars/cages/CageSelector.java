package ostb.gameapi.games.skywars.cages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.gameapi.KitBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class CageSelector implements Listener {
	private String name = null;
	private ItemStack item = null;
	private Map<String, Integer> pages = null;
	
	public CageSelector() {
		name = "Cage Selector";
		item = new ItemCreator(Material.IRON_FENCE).setName("&b" + name).getItemStack();
		pages = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private int getPage(Player player) {
		if(!pages.containsKey(player.getName())) {
			pages.put(player.getName(), 1);
		}
		return pages.get(player.getName());
	}
	
	private void open(Player player) {
		open(player, getPage(player));
	}
	
	private void open(final Player player, final int page) {
		final Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		player.openInventory(inventory);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				pages.put(player.getName(), page);
				if(page == 1) {
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && kit.getKitType().equals("small_cage")) {
							inventory.setItem(kit.getSlot() - 18, kit.getIcon(player));
						}
					}
				} else if(page == 2) {
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && kit.getKitType().equals("big_cage")) {
							inventory.setItem(kit.getSlot() - 18, kit.getIcon(player));
						}
					}
				}
				if(page > 1) {
					inventory.setItem(18, new ItemCreator(Material.ARROW).setName("&bPage #" + (page - 1)).getItemStack());
				}
				if(page < 2) {
					inventory.setItem(26, new ItemCreator(Material.ARROW).setName("&bPage #" + (page + 1)).getItemStack());
				}
			}
		});
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(OSTB.getMiniGame().getJoiningPreGame()) {
			event.getPlayer().getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item.equals(this.item)) {
			event.setCancelled(true);
			open(player);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.ARROW) {
				if(event.getSlot() == 18) {
					open(player, getPage(player) - 1);
					return;
				} else if(event.getSlot() == 26) {
					open(player, getPage(player) + 1);
					return;
				}
			}
			for(KitBase kit : KitBase.getKits()) {
				String name = ChatColor.stripColor(event.getItemTitle());
				if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && name.startsWith(kit.getName()) && kit.getSlot() == event.getSlot()) {
					if(kit.use(player)) {
						EffectUtil.playSound(player, Sound.LEVEL_UP);
						player.closeInventory();
					} else {
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		HandlerList.unregisterAll(this);
		name = null;
		item = null;
		pages.clear();
		pages = null;
	}
}
