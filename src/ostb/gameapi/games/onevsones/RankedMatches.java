package ostb.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.gameapi.games.onevsones.events.BattleEndEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class RankedMatches implements Listener {
	private static Map<String, Integer> matches = null;
	private static List<String> ranked = null;
	
	public RankedMatches() {
		matches = new HashMap<String, Integer>();
		ranked = new ArrayList<String>();
		new CommandBase("giveMatches", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = ProPlugin.getPlayer(arguments[0]);
				if(player != null) {
					int amount = matches.get(player.getName()) + Integer.valueOf(arguments[1]);
					matches.put(player.getName(), amount);
					MessageHandler.sendMessage(player, "Vote perk: &e+" + arguments[1] + " ranked matches");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static int getMatches(Player player) {
		if(Ranks.PREMIUM.hasRank(player)) {
			return 999;
		}
		return matches.get(player.getName());
	}
	
	public static boolean isPlayingRanked(Player player) {
		return ranked != null && ranked.contains(player.getName());
	}
	
	public static void setPlayingRanked(Player player) {
		if(!ranked.contains(player.getName())) {
			matches.put(player.getName(), getMatches(player) - 1);
			ranked.add(player.getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		matches.put(event.getPlayer().getName(), DB.PLAYERS_ONE_VS_ONE_RANKED.getInt("uuid", event.getPlayer().getUniqueId().toString(), "amount"));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBattleEnd(BattleEndEvent event) {
		ranked.remove(event.getWinner().getName());
		ranked.remove(event.getLoser().getName());
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(matches.containsKey(name)) {
			UUID uuid = event.getUUID();
			int amount = matches.get(name);
			if(DB.PLAYERS_ONE_VS_ONE_RANKED.isUUIDSet(uuid)) {
				if(amount <= 0) {
					DB.PLAYERS_ONE_VS_ONE_RANKED.deleteUUID(uuid);
				} else {
					DB.PLAYERS_ONE_VS_ONE_RANKED.updateInt("amount", amount, "uuid", uuid.toString());
				}
			} else if(amount > 0) {
				DB.PLAYERS_ONE_VS_ONE_RANKED.insert("'" + uuid.toString() + "', '" + amount + "'");
			}
			matches.remove(name);
			ranked.remove(name);
		}
	}
}
