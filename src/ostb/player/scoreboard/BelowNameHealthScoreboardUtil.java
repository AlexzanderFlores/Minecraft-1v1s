package ostb.player.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.server.util.UnicodeUtil;

public class BelowNameHealthScoreboardUtil extends BelowNameScoreboardUtil {
	public BelowNameHealthScoreboardUtil() {
		this(OSTB.getSidebar().getScoreboard());
	}
	
	public BelowNameHealthScoreboardUtil(Scoreboard scoreboard) {
		super(scoreboard, "showhealth", "health", ChatColor.RED + UnicodeUtil.getHeart());
		for(Player player : OSTB.getProPlugin() == null ? Bukkit.getOnlinePlayers() : ProPlugin.getPlayers()) {
			setScore(player.getName(), (int) player.getHealth());
			player.setHealth(player.getMaxHealth() - 2.0d);
			player.setHealth(player.getMaxHealth());
		}
	}
}
