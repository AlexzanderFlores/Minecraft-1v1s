package ostb.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerRankChangeEvent;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class TeamScoreboardHandler implements Listener {
	public TeamScoreboardHandler() {
		EventUtil.register(this);
	}
	
	private void set(Player player) {
		remove(player);
		for(Player online : Bukkit.getOnlinePlayers()) {
			Scoreboard scoreboard = online.getScoreboard();
			for(Ranks rank : Ranks.values()) {
				Team team = scoreboard.getTeam(rank.getPrefix());
				if(team == null) {
					String name = rank.getPrefix().replace(" ", "");
					name = name.substring(0, name.length() - 2);
					Bukkit.getLogger().info("\"" + name + "\"");
					team = scoreboard.registerNewTeam(name);
				}
				if(team.getPrefix().equals(AccountHandler.getRank(player).getPrefix())) {
					team.addPlayer(player);
				}
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
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
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