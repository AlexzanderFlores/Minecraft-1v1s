package ostb.gameapi.games.kitpvp.shop;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.games.kitpvp.events.InventoryViewClickEvent;
import ostb.player.MessageHandler;
import ostb.server.util.EffectUtil;

public class EnchantAnItem extends InventoryViewer {
	private static final int price = 20;
	
	public EnchantAnItem(Player player) {
		super("Enchant an Item", player);
	}
	
	public static int getPrice() {
		return price;
	}
	
	@EventHandler
	public void onInventoryViewClick(InventoryViewClickEvent event) {
		Player player = event.getPlayer();
		if(coinsHandler.getCoins(player) >= price) {
			coinsHandler.addCoins(player, price * -1);
			ItemStack item = player.getInventory().getItem(event.getSlot());
			if(item.getEnchantments() == null || item.getEnchantments().isEmpty()) {
				InventoryView view = player.getOpenInventory();
				if(view != null && view.getTitle().equals(name)) {
					item = view.getItem(event.getViewSlot());
					
				}
			} else {
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou do not have enough coins, get more with &a/vote");
			EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
		}
	}
}
