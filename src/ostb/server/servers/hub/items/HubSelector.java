package ostb.server.servers.hub.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.util.ItemCreator;

public class HubSelector extends HubItemBase {
	public HubSelector() {
		super(new ItemCreator(Material.WATCH).setName("&eHub Selector"), 8);
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
			GameSelector.open(player, Plugins.HUB);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(Plugins.HUB.getDisplay())) {
			Player player = event.getPlayer();
			ProPlugin.sendPlayerToServer(player, "hub" + event.getItem().getAmount());
			event.setCancelled(true);
		}
	}
}
