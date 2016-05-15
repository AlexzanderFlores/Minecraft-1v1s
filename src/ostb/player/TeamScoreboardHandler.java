package ostb.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerRankChangeEvent;
import ostb.customevents.player.PostPlayerJoinEvent;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class TeamScoreboardHandler implements Listener {
	public TeamScoreboardHandler() {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				new CommandBase("test", true) {
					@Override
					public boolean execute(CommandSender sender, String [] arguments) {
						Player player = (Player) sender;
						Scoreboard scoreboard = player.getScoreboard();
						for(Team team : scoreboard.getTeams()) {
							MessageHandler.sendMessage(player, team.getName() + " &x: " + team.getPrefix() + " &x : " + team.getSize());
						}
						return true;
					}
				};
			}
		}, 20 * 5);
		EventUtil.register(this);
	}
	
	private void set(Player player) {
		remove(player);
		Ranks pRank = AccountHandler.getRank(player);
		for(Player online : Bukkit.getOnlinePlayers()) {
			Ranks oRank = AccountHandler.getRank(online);
			try {
				online.getScoreboard().getTeam(getName(pRank)).addPlayer(player);
			} catch(NullPointerException e) {
				
			}
			try {
				player.getScoreboard().getTeam(getName(oRank)).addPlayer(online);
			} catch(NullPointerException e) {
				
			}
		}
	}
	
	private void remove(Player player) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			Scoreboard scoreboard = online.getScoreboard();
			for(Team team : scoreboard.getTeams()) {
				team.removePlayer(player);
			}
		}
	}
	
	private String getName(Ranks rank) {
		String name = rank.getPrefix().replace(" ", "");
		name = name.substring(0, name.length() - 2);
		return name;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		Scoreboard scoreboard = player.getScoreboard();
		for(Ranks rank : Ranks.values()) {
			Team team = scoreboard.registerNewTeam(getName(rank));
			team.setPrefix(rank.getColor()+"");
		}
		set(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerRankChange(PlayerRankChangeEvent event) {
		set(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
