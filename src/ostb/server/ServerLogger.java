package ostb.server;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.ServerRestartEvent;
import ostb.customevents.TimeEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.server.util.EventUtil;

public class ServerLogger implements Listener {
	private static int players = -1;
	private static int max = -1;
	private static GameStates state = null;
	private static boolean shuttingDown = false;
	
	public ServerLogger() {
		EventUtil.register(this);
		updateStatus(false);
	}
	
	private void updateStatus(boolean delete) {
		String game = OSTB.getPlugin().toString();
		String number = OSTB.getServerName().replaceAll("[^\\d.]", "");
		String [] keys = {"game_name", "server_number"};
		String [] values = {game, number};
		if(delete) {
			DB.NETWORK_SERVER_STATUS.delete(keys, values);
		} else {
			int current = Bukkit.getOnlinePlayers().size();
			GameStates gameState = null;
			MiniGame miniGame = OSTB.getMiniGame();
			if(miniGame != null) {
				gameState = miniGame.getGameState();
			}
			int serverMax = OSTB.getMaxPlayers();
			if(current != players || gameState != state || serverMax != max) {
				players = current;
				max = serverMax;
				state = gameState;
				int priority = 2;
				if(miniGame != null) {
					if(ProPlugin.isServerFull()) {
						if(miniGame.getJoiningPreGame()) {
							priority = 1;
						} else {
							priority = 3;
						}
					} else if(!miniGame.getJoiningPreGame()) {
						priority = 3;
					}
				}
				String lore = gameState == null ? "null" : gameState.toString();
				if(DB.NETWORK_SERVER_STATUS.isKeySet(keys, values)) {
					DB.NETWORK_SERVER_STATUS.updateInt("listed_priority", priority, keys, values);
					DB.NETWORK_SERVER_STATUS.updateString("lore", lore, keys, values);
					DB.NETWORK_SERVER_STATUS.updateInt("players", players, keys, values);
					DB.NETWORK_SERVER_STATUS.updateInt("max_players", OSTB.getMaxPlayers(), keys, values);
				} else {
					DB.NETWORK_SERVER_STATUS.insert("'" + game + "', '" + number + "', '" + priority + "', '" + lore + "', '0', '" + OSTB.getMaxPlayers() + "'");
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(!shuttingDown) {
				updateStatus(false);
			}
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		shuttingDown = true;
		updateStatus(true);
	}
}
