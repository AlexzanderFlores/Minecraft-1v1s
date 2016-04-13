package ostb.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import ostb.ProPlugin;
import ostb.OSTB.Plugins;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.CoinUpdateEvent;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class CoinsHandler implements Listener {
	private static Map<Plugins, CoinsHandler> handlers = new HashMap<Plugins, CoinsHandler>();
	private DB table = null;
	//private Plugins plugin = null;
	private Map<String, Integer> coins = null;
	//private boolean boosterEnabled = false;
	
	public CoinsHandler(DB table, Plugins plugin) {
		if(!handlers.containsKey(plugin)) {
			this.table = table;
			//this.plugin = plugin;
			coins = new HashMap<String, Integer>();
			handlers.put(plugin, this);
			new CommandBase("addCoins", 3) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					String name = arguments[0];
					String game = arguments[1];
					int amount = Integer.valueOf(arguments[2]);
					Plugins plugin = null;
					try {
						plugin = Plugins.valueOf(game);
					} catch(Exception e) {
						for(Plugins plugins : Plugins.values()) {
							MessageHandler.sendMessage(sender, plugins.toString());
						}
						return true;
					}
					if(handlers.containsKey(plugin)) {
						Player player = ProPlugin.getPlayer(name);
						if(player == null) {
							MessageHandler.sendMessage(sender, "&c" + name + " is not online");
						} else {
							CoinsHandler handler = handlers.get(plugin);
							handler.addCoins(player, amount);
						}
					} else {
						MessageHandler.sendMessage(sender, "&cThere is no handler for " + plugin.toString());
					}
					return true;
				}
			};
			EventUtil.register(this);
		}
	}
	
	public static CoinsHandler getCoinsHandler(Plugins plugin) {
		return handlers.get(plugin);
	}
	
	public int getCoins(Player player) {
		if(!coins.containsKey(player.getName())) {
			coins.put(player.getName(), table.getInt("uuid", player.getUniqueId().toString(), "coins"));
		}
		return coins.get(player.getName());
	}
	
	public void addCoins(Player player, int amount) {
		addCoins(player, amount, true);
	}
	
	public void addCoins(Player player, int amount, boolean multiply) {
		if(amount <= 0) {
			multiply = false;
		}
		String multiplier = multiply ? Ranks.PLAYER.getColor() + "(x1 Multiplier)" : "";
		if(multiply) {
			if(Ranks.PREMIUM_PLUS.hasRank(player)) {
				amount *= 3;
				multiplier = AccountHandler.getRank(player).getColor() + "(x3 Multiplier)";
			} else if(Ranks.PREMIUM.hasRank(player)) {
				amount *= 2;
				multiplier = Ranks.PREMIUM.getColor() + "(x2 Multiplier)";
			}
		}
		MessageHandler.sendMessage(player, (amount >= 0 ? "&6+" : "&c") + amount + " Coins " + multiplier);
		coins.put(player.getName(), amount + coins.get(player.getName()));
		Bukkit.getPluginManager().callEvent(new CoinUpdateEvent(player));
	}
	
	public ItemStack getItemStack(Player player) {
		return new ItemCreator(Material.GOLD_INGOT).setName("&7Coins: &a" + getCoins(player)).setLores(new String [] {
			"",
			"&eGet more coins daily through &a/vote",
			""
		}).getItemStack();
	}
	
	/*@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					
				}
			});
		}
	}*/
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(coins.containsKey(name)) {
			UUID uuid = event.getUUID();
			int amount = coins.get(name);
			if(table.isUUIDSet(uuid)) {
				table.updateInt("coins", amount, "uuid", uuid.toString());
			} else {
				table.insert("'" + uuid.toString() + "', '" + amount + "'");
			}
			coins.remove(name);
		}
	}
}
