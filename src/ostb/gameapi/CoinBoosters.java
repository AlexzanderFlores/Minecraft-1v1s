package ostb.gameapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.CoinGiveEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class CoinBoosters implements Listener {
	private Map<String, Integer> boosters = null;
	private ItemStack item = null;
	private String user = null;
	private final String command = "Get boosters with &6/booster";
	
	public CoinBoosters() {
		boosters = new HashMap<String, Integer>();
		item = new ItemCreator(Material.DIAMOND).setName("&eClick to Enable x2 Coin Booster").setGlow(true).getItemStack();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(OSTB.getMiniGame().getUseCoinBoosters() && OSTB.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			UUID uuid = player.getUniqueId();
			String [] keys = new String [] {"uuid", "game_name"};
			String [] values = new String [] {uuid.toString(), OSTB.getPlugin().getData()};
			boosters.put(player.getName(), DB.PLAYERS_COIN_BOOSTERS.getInt(keys, values, "amount"));
			player.getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		final Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(this.item.equals(item)) {
			int amount = boosters.get(player.getName());
			if(amount > 0) {
				if(user == null) {
					user = AccountHandler.getPrefix(player);
					MessageHandler.alert(user + " &xhas enabled a x2 coin booster for " + OSTB.getPlugin().getDisplay() + "! " + command);
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							int amount = boosters.get(player.getName()) - 1;
							String [] keys = new String [] {"uuid", "game_name"};
							String [] values = new String [] {player.getUniqueId().toString(), OSTB.getPlugin().getData()};
							if(amount <= 0) {
								DB.PLAYERS_COIN_BOOSTERS.delete(keys, values);
							} else {
								DB.PLAYERS_COIN_BOOSTERS.updateInt("amount", amount, keys, values);
							}
						}
					});
				} else {
					MessageHandler.sendMessage(player, user + " &chas already enabled a coin booster for " + OSTB.getPlugin().getDisplay() + ". " + command);
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have any coin boosters! " + command);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCoinGive(CoinGiveEvent event) {
		if(user != null) {
			MessageHandler.sendMessage(event.getPlayer(), user + " &xhas an active x2 Coins booster! " + command);
			event.setAmount(event.getAmount() * 2);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		boosters.remove(event.getPlayer().getName());
	}
}
