package ostb.player;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Settings implements Listener {
	private static String name = null;
	private static List<String> delayed = null;
	private static final int delay = 2;
	private enum Options {
		PLAYER_VISIBILITY("Player Visibility", DB.HUB_PLAYERS_VANISHED, 13);
		//FRIEND_REQUESTS("Friend Requests", 15);
		//PARTY_REQUESTS("Party Requsts");
		
		private String name = null;
		private DB db = null;
		private int slot = 0;
		
		private Options(String name, DB db, int slot) {
			this.name = name;
			this.db = db;
			this.slot = slot;
		}
		
		public String getName() {
			return this.name;
		}
		
		public DB getDB() {
			return this.db;
		}
		
		public int getSlot() {
			return this.slot;
		}
	}
	
	public Settings() {
		name = "Settings";
		EventUtil.register(this);
	}
	
	public static void open(final Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		player.openInventory(inventory);
		update(player, inventory);
	}
	
	private static void update(Player player) {
		update(player, null);
	}
	
	private static void update(final Player player, final Inventory inventory) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID uuid = player.getUniqueId();
				InventoryView openInventory = player.getOpenInventory();
				boolean update = openInventory.getTitle().equals(name);
				for(Options option : Options.values()) {
					if(option == Options.PLAYER_VISIBILITY) {
						if(option.getDB().isUUIDSet(uuid)) {
							if(update) {
								openInventory.setItem(option.getSlot(), new ItemCreator(Material.INK_SACK, 8).setName("&7" + option.getName() + ": &cDisabled").getItemStack());
							} else {
								inventory.setItem(option.getSlot(), new ItemCreator(Material.INK_SACK, 8).setName("&7" + option.getName() + ": &cDisabled").getItemStack());
							}
						} else {
							if(update) {
								openInventory.setItem(option.getSlot(), new ItemCreator(Material.INK_SACK, 10).setName("&7" + option.getName() + ": &bEnabled").getItemStack());
							} else {
								inventory.setItem(option.getSlot(), new ItemCreator(Material.INK_SACK, 10).setName("&7" + option.getName() + ": &bEnabled").getItemStack());
							}
						}
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			final Player player = event.getPlayer();
			ItemStack item = event.getItem();
			Options selected = null;
			for(Options option : Options.values()) {
				if(option.getSlot() == event.getSlot()) {
					selected = option;
					break;
				}
			}
			if(selected != null) {
				final String name = player.getName();
				if(!delayed.contains(name)) {
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * delay);
					final Options option = selected;
					final byte data = item.getData().getData();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							if(data == 8) {
								if(option == Options.PLAYER_VISIBILITY) {
									option.getDB().deleteUUID(player.getUniqueId());
								} else {
									option.getDB().insert("'" + player.getUniqueId().toString() + "'");
								}
							} else if(data == 10) {
								if(option == Options.PLAYER_VISIBILITY) {
									option.getDB().insert("'" + player.getUniqueId().toString() + "'");
								} else {
									option.getDB().deleteUUID(player.getUniqueId());
								}
							}
							update(player);
						}
					});
				}
			}
			event.setCancelled(true);
		}
	}
}
