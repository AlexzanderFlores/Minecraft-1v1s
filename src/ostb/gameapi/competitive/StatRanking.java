package ostb.gameapi.competitive;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.game.GameDeathEvent;
import ostb.gameapi.MiniGame;
import ostb.player.MessageHandler;
import ostb.server.DB;
import ostb.server.DB.Databases;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class StatRanking implements Listener {
	public StatRanking() {
		if(OSTB.getMiniGame() != null && !OSTB.getMiniGame().getPlayersHaveOneLife()) {
			return;
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		MiniGame miniGame = OSTB.getMiniGame();
		if(miniGame == null || !miniGame.getPlayersHaveOneLife()) {
			return;
		}
		final Player player = event.getPlayer();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int rank = -1;
				int kills = 0;
				DB db = DB.PLAYERS_STATS_SKY_WARS;
				ResultSet resultSet = null;
				try {
					Connection connection = Databases.PLAYERS.getConnection();
					connection.prepareStatement("SET @uuid = '" + player.getUniqueId().toString() + "'").executeQuery();
					connection.prepareStatement("SET @kills = (SELECT kills FROM " + db.getName() + " WHERE uuid = @uuid)").executeQuery();
					resultSet = connection.prepareStatement("SELECT kills,(SELECT COUNT(*) FROM " + db.getName() + " WHERE kills >= @kills) AS rank FROM " + db.getName() + " WHERE uuid = @uuid").executeQuery();
					while(resultSet.next()) {
						kills = resultSet.getInt("kills");
						rank = resultSet.getInt("rank");
					}
					connection.prepareStatement("SET @uuid = NULL, @kills = NULL").executeQuery();
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(resultSet);
				}
				MessageHandler.sendMessage(player, "&a&lRanking: &xYou now have &b" + kills + " &xkill" + (kills == 1 ? "" : "s") + " bringing you to rank &b#" + rank);
			}
		}, 20);
	}
}
