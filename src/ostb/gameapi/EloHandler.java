package ostb.gameapi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class EloHandler implements Listener {
	private static Map<String, Integer> elo = null;
	
	public EloHandler() {
		elo = new HashMap<String, Integer>();
		new CommandBase("elo", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String target = null;
				if(arguments.length == 0) {
					target = sender.getName();
				} else if(Ranks.PREMIUM_PLUS.hasRank(sender)) {
					target = arguments[0];
				} else {
					MessageHandler.sendMessage(sender, Ranks.PREMIUM_PLUS.getNoPermission());
					return true;
				}
				if(elo.containsKey(target)) {
					MessageHandler.sendMessage(sender, target + " has an elo of &e" + elo.get(target));
				} else {
					MessageHandler.sendMessage(sender, "&c" + target + " has no elo logged on this server");
				}
				return true;
			}
		}.setRequiredRank(Ranks.PREMIUM);
		EventUtil.register(this);
	}
	
	public static void calculateWin(Player winner, Player loser, int display) {
		int elo1 = getElo(loser);
		int elo2 = getElo(winner);
		int K = 32;
		int diff = elo1 - elo2;
		double percentage = 1 / (1 + Math.pow(10, diff / 400));
		int amount = (int) Math.round(K * (1 - percentage));
		if(amount < 1) {
			amount = 1;
		}
		//int draw = (int) Math.round(K * (0.5 - percentage));
		int winnerResult = add(winner, amount);
		int loserResult = add(loser, -amount);
		if(display > 0) { // 0 = don't display, 1 = display change, 2 = display change + new value
			String newWinner = AccountHandler.getPrefix(winner) + (display == 2 ? " &6" + winnerResult : "") + " &a(+" + amount + ")";
			String newLoser = AccountHandler.getPrefix(loser) + (display == 2 ? " &6" + loserResult : "") + " &c(" + amount * -1 + ")";
			for(Player player : new Player [] {winner, loser}) {
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, newWinner);
				MessageHandler.sendMessage(player, newLoser);
				MessageHandler.sendLine(player);
			}
		}
	}
	
	public static int getElo(Player player) {
		return elo == null || !elo.containsKey(player.getName()) ? 0 : elo.get(player.getName());
	}
	
	public static int add(Player player, int amount) {
		elo.put(player.getName(), getElo(player) + amount);
		return elo.get(player.getName());
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		String name = event.getName();
		if(elo.containsKey(name)) {
			DB db = StatsHandler.getEloDB();
			if(db != null) {
				int amount = elo.get(name);
				if(db.isUUIDSet(uuid)) {
					db.updateInt("elo", amount, "uuid", uuid.toString());
				} else {
					db.insert("'" + uuid.toString() + "', '" + amount + "'");
				}
			}
			elo.remove(name);
		}
	}
}
