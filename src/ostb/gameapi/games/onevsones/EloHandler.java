package ostb.gameapi.games.onevsones;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.games.onevsones.events.BattleEndEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.ChatClickHandler;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class EloHandler implements Listener {
    private static Map<String, Integer> elo = null;
    private static final int starting = 1000;
	
	public EloHandler() {
		elo = new HashMap<String, Integer>();
		new CommandBase("elo", -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(elo.containsKey(sender.getName())) {
					MessageHandler.sendMessage(sender, "Your elo is &e" + elo.get(sender.getName()));
				} else {
					MessageHandler.sendMessage(sender, "&cNo Elo Logged");
				}
				MessageHandler.sendMessage(sender, "More stats: /versusStats");
				return true;
			}
		};
		new RankedMatches();
		EventUtil.register(this);
	}
	
	public static int getElo(Player player) {
		if(elo != null && player != null && elo.containsKey(player.getName())) {
			return elo.get(player.getName());
		}
		return starting;
	}
	
	private static int add(Player player, int amount) {
		amount += getElo(player);
		elo.put(player.getName(), amount < 200 ? 200 : amount);
		return getElo(player);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final String name = event.getPlayer().getName();
		final UUID uuid = event.getPlayer().getUniqueId();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(DB.PLAYERS_ONE_VS_ONE_ELO.isUUIDSet(uuid)) {
					elo.put(name, DB.PLAYERS_ONE_VS_ONE_ELO.getInt("uuid", uuid.toString(), "amount"));
				} else {
					elo.put(name, starting);
					DB.PLAYERS_ONE_VS_ONE_ELO.insert("'" + uuid.toString() + "', '" + starting + "'");
				}
			}
		});
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		String kitName = ChatColor.stripColor(event.getKit().getName().toLowerCase().replace(" ", ""));
		if(kitName.equals("pyro") || kitName.equals("ender") || kitName.equals("tnt") || kitName.equals("onehitwonder") || kitName.equals("quickshot")) {
			return;
		}
		Player winner = event.getWinner();
		Player loser = event.getLoser();
		if(RankedMatches.isPlayingRanked(winner) && RankedMatches.isPlayingRanked(loser)) {
			int elo1 = getElo(loser);
			int elo2 = getElo(winner);
			int K = 32;
			int diff = elo1 - elo2;
			double percentage = 1 / (1 + Math.pow(10, diff / 400));
			int amount = (int) Math.round(K * (1 - percentage));
			if(amount < 1) {
				amount = 1;
			}
			int winnerResult = add(winner, amount);
			int loserResult = add(loser, -amount);
			String newWinner = AccountHandler.getPrefix(winner) + " &6" + winnerResult + " &a(+" + amount + ")";
			String newLoser = AccountHandler.getPrefix(loser) + " &6" + loserResult + " &c(" + amount * -1 + ")";
			for(Player player : new Player [] {winner, loser}) {
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, newWinner);
				MessageHandler.sendMessage(player, newLoser);
				ChatClickHandler.sendMessageToRunCommand(player, " &cClick here", "Click to view elo", "/versusStats", "&aView elo: &f/versusStats [name] &aor");
				MessageHandler.sendLine(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(elo.containsKey(player.getName())) {
			final UUID uuid = player.getUniqueId();
			final String name = player.getName();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.PLAYERS_ONE_VS_ONE_ELO.updateInt("amount", elo.get(name), "uuid", uuid.toString());
					elo.remove(name);
				}
			});
		}
	}
}
