package ostb.gameapi.games.kitpvp.shop;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.games.kitpvp.events.InventoryViewClickEvent;
import ostb.player.MessageHandler;
import ostb.server.util.EffectUtil;

public class RepairAnItem extends InventoryViewer {
	private static final int price = 5;
	
	public RepairAnItem() {
		super("Repair an Item");
	}
	
	public static int getPrice() {
		return price;
	}
	
	@EventHandler
	public void onInventoryViewClick(InventoryViewClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			if(coinsHandler.getCoins(player) >= price) {
				coinsHandler.addCoins(player, price * -1);
				ItemStack item = player.getInventory().getItem(event.getSlot());
				if(item.getDurability() < 0) {
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				} else {
					item.setDurability((short) -1);
					InventoryView view = player.getOpenInventory();
					if(view != null && view.getTitle().equals(name)) {
						item = view.getItem(event.getViewSlot());
						item.setDurability((short) -1);
					}
					EffectUtil.playSound(player, Sound.ANVIL_USE);
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have enough coins, get more with &a/vote");
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			}
		}
		}
}
