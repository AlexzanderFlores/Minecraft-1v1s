package ostb.gameapi.games.uhcskywars;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.PlayerOpenNewChestEvent;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		OSTB.getMiniGame().getMap().setGameRuleValue("naturalRegeneration", "false");
		for(Player player : ProPlugin.getPlayers()) {
			player.setMaxHealth(player.getMaxHealth() * 2);
			player.setHealth(player.getMaxHealth());
		}
	}
	
	@EventHandler
	public void onPlayerOpenNewChest(PlayerOpenNewChestEvent event) {
		Chest chest = event.getChest();
		Inventory inventory = chest.getInventory();
		Random random = new Random();
		int slot = 0;
		do {
			slot = random.nextInt(inventory.getSize());
		} while(inventory.getItem(slot) != null);
		if(random.nextBoolean()) {
			inventory.setItem(slot, new ItemStack(Material.APPLE, 2));
		} else {
			inventory.setItem(slot, new ItemStack(Material.GOLD_INGOT, 8));
		}
	}
}
