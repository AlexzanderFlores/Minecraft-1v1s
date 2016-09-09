package ostb.server.servers.hub.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;

public class Shop extends HubItemBase {
	private String name = null;
	
	public Shop() {
		super(new ItemCreator(Material.CHEST).setName("&eShop"), 2);
		name = ChatColor.stripColor(getName());
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
		Player player = event.getPlayer();
		if(isItem(player)) {
			open(player);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		String title = event.getTitle();
		String name = ChatColor.stripColor(event.getItemTitle());
		if(title.equals(ChatColor.stripColor(getName()))) {
			EffectUtil.playSound(player, Sound.CHEST_OPEN);
			/*if(name.equals("Sky Wars")) {
				SkyWarsShop.getInstance().openShop(player);
			}*/
			event.setCancelled(true);
		} else if(title.startsWith("Shop - ")) {
			if(name.equals("Back")) {
				EffectUtil.playSound(player, Sound.CHEST_CLOSE);
				open(player);
			}
			event.setCancelled(true);
		}
	}
	
	private void open(Player player) {
		EffectUtil.playSound(player, Sound.CHEST_OPEN);
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, ChatColor.stripColor(name));
		ItemStack item = new ItemCreator(Material.GRASS).setName("&bSky Wars").setLores(new String [] {
			"",
			"&7Click to view Shop",
			""
		}).getItemStack();
		inventory.setItem(13, item);
		player.openInventory(inventory);
	}
}
