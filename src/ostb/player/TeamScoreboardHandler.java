package ostb.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerRankChangeEvent;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class TeamScoreboardHandler implements Listener {
	private List<Team> teams = null;
	
	public TeamScoreboardHandler() {
		teams = new ArrayList<Team>();
		for(Ranks rank : Ranks.values()) {
			Team team = OSTB.getScoreboard().registerNewTeam(rank.getPrefix());
			team.setPrefix(team.getName());
			teams.add(team);
		}
		EventUtil.register(this);
	}
	
	private void set(Player player) {
		remove(player);
		for(Team team : teams) {
			if(team.getName().equals(AccountHandler.getRank(player).getPrefix())) {
				team.addPlayer(player);
				break;
			}
		}
	}
	
	private void remove(Player player) {
		for(Team team : teams) {
			team.removePlayer(player);
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