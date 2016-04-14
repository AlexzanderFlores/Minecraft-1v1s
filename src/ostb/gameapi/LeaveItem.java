package ostb.gameapi;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.MouseClickEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class LeaveItem implements Listener {
	private ItemStack leaveItem = null;
	
	public LeaveItem() {
		leaveItem = new ItemCreator(Material.WOOD_DOOR).setName("&aReturn to Hub").getItemStack();
		EventUtil.register(this);
	}
	
	private boolean isWaiting() {
		GameStates gameState = OSTB.getMiniGame().getGameState();
		return gameState == GameStates.WAITING || gameState == GameStates.VOTING;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(isWaiting()) {
			event.getPlayer().getInventory().setItem(8, leaveItem);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(isWaiting()) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();
			if(item != null && item.equals(leaveItem)) {
				ProPlugin.sendPlayerToServer(player, "hub");
			}
		}
	}
}
