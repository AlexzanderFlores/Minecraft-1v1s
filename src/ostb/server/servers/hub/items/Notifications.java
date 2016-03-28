package ostb.server.servers.hub.items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.customevents.timed.TenTickTaskEvent;
import ostb.player.MessageHandler;
import ostb.server.DB;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Notifications extends HubItemBase {
	private List<String> queue = null;
	private List<String> hasNotifications = null;
	
	public Notifications() {
		super(new ItemCreator(Material.WOOL, (byte) 8).setName("&eNotifications"), 7);
		queue = new ArrayList<String>();
		hasNotifications = new ArrayList<String>();
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		giveItem(player);
		queue.add(player.getName());
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			MessageHandler.sendMessage(player, "&cYou have no notifications");
			player.updateInventory();
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(queue != null && !queue.isEmpty()) {
			String name = queue.get(0);
			queue.remove(0);
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				final UUID uuid = player.getUniqueId();
				final String playerName = player.getName();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.PLAYERS_NOTIFICATIONS.isUUIDSet(uuid)) {
							hasNotifications.add(playerName);
						}
					}
				});
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(String name : hasNotifications) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				String itemName = getName();
				if(player.getInventory().getItem(getSlot()).getData().getData() == 8) {
					player.getInventory().setItem(getSlot(), new ItemCreator(Material.WOOL, 14).setName(itemName).getItemStack());
				} else {
					player.getInventory().setItem(getSlot(), new ItemCreator(Material.WOOL, 8).setName(itemName).getItemStack());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		queue.remove(event.getPlayer().getName());
		hasNotifications.remove(event.getPlayer().getName());
	}
}
