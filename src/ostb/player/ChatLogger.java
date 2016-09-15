package ostb.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ostb.Network;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.util.EventUtil;
import ostb.server.util.TimeUtil;

public class ChatLogger implements Listener {
	private Map<String, List<String>> playerMessages = null;
	
	public ChatLogger() {
		playerMessages = new HashMap<String, List<String>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(Ranks.isStaff(player) || AccountHandler.getRank(player) == Ranks.YOUTUBER) {
			List<String> messages = new ArrayList<String>();
			if(playerMessages.containsKey(player.getName())) {
				messages = playerMessages.get(player.getName());
			}
			messages.add(event.getMessage().replace("\'", "\""));
			playerMessages.put(player.getName(), messages);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		String name = event.getName();
		if(playerMessages.containsKey(name)) {
			String server = Network.getServerName();
			String time = TimeUtil.getTime();
			Ranks rank = AccountHandler.getRank(uuid);
			for(String message : playerMessages.get(name)) {
				DB.PLAYERS_CHAT_LOGS.insert("'" + uuid.toString() + "', '" + rank.toString() + "', '" + server + "', '" +  time + "', '" + message + "'");
			}
			playerMessages.get(name).clear();
			playerMessages.remove(name);
		}
	}
}
