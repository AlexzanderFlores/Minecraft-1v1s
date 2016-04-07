package ostb.gameapi.games.hardcoreelimination.modifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class ModifierHandler implements Listener {
	private static Map<String, Integer> votePasses = null;
	
	public ModifierHandler() {
		votePasses = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static int getVotePasses(Player player) {
		return votePasses.get(player.getName());
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		votePasses.put(player.getName(), DB.PLAYERS_HARDCORE_ELIMINATION_VOTES.getInt("uuid", uuid.toString(), "votes"));
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(votePasses.containsKey(name)) {
			UUID uuid = event.getUUID();
			int votes = votePasses.get(name);
			if(votes <= 0 && DB.PLAYERS_HARDCORE_ELIMINATION_VOTES.isUUIDSet(uuid)) {
				DB.PLAYERS_HARDCORE_ELIMINATION_VOTES.deleteUUID(uuid);
			} else {
				DB.PLAYERS_HARDCORE_ELIMINATION_VOTES.updateInt("votes", votes, "uuid", uuid.toString());
			}
		}
	}
}
