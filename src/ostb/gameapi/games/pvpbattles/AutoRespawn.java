package ostb.gameapi.games.pvpbattles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.player.TitleDisplayer;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class AutoRespawn implements Listener {
	private static Map<String, Integer> passes = null;
	
	public AutoRespawn() {
		passes = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static boolean useAutoRespawn(Player player) {
		String name = player.getName();
		if(passes.containsKey(name)) {
			int amount = passes.get(name);
			if(amount > 0) {
				passes.put(name, --amount);
				new TitleDisplayer(player, "&bYou now have &e" + amount, "&bAuto resawn pass" + (amount == 1 ? "" : "es")).display();
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					UUID uuid = player.getUniqueId();
					if(DB.PLAYERS_PVP_BATTLES_AUTO_RESPAWN.isUUIDSet(uuid)) {
						passes.put(player.getName(), DB.PLAYERS_PVP_BATTLES_AUTO_RESPAWN.getInt("uuid", uuid.toString(), "amount"));
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(passes.containsKey(name)) {
			UUID uuid = event.getUUID();
			DB.PLAYERS_PVP_BATTLES_AUTO_RESPAWN.updateInt("amount", passes.get(name), "uuid", uuid.toString());
			passes.remove(name);
		}
	}
}
