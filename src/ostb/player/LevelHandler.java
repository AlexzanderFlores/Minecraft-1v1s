package ostb.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.AsyncPostPlayerJoinEvent;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class LevelHandler implements Listener {
	private static boolean enabled = false;
	private static Map<String, Integer> levels = null;
	private static Map<String, Integer> exps = null;
	private static List<String> toUpdate = null;
	
	public LevelHandler() {
		levels = new HashMap<String, Integer>();
		exps = new HashMap<String, Integer>();
		toUpdate = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static void enable() {
		enabled = true;
	}
	
	public static int getLevel(Player player) {
		return levels.get(player.getName());
	}
	
	public static int getExp(Player player) {
		return exps.get(player.getName());
	}
	
	public static int getNeededForLevelUp(Player player) {
		return getLevel(player) * 1000;
	}
	
	public static int getPercentageDone(Player player) {
		return (int) (getExp(player) * 100.0 / getNeededForLevelUp(player) + 0.5);
	}
	
	public static void add(Player player, int amount) {
		if(!toUpdate.contains(player.getName())) {
			toUpdate.add(player.getName());
		}
		int current = getExp(player) + amount;
		int needed = getNeededForLevelUp(player);
		if(current >= needed) {
			levels.put(player.getName(), getLevel(player) + 1);
			exps.put(player.getName(), current - needed);
		} else {
			exps.put(player.getName(), current);
		}
		if(enabled) {
			player.setLevel(levels.get(player.getName()));
			displayExp(player);
		}
	}
	
	public static void displayExp(Player player) {
		player.setExp(Float.valueOf("0." + getPercentageDone(player)));
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		int level = DB.PLAYERS_LEVELS.getInt("uuid", uuid.toString(), "level");
		int exp = DB.PLAYERS_LEVELS.getInt("uuid", uuid.toString(), "exp");
		levels.put(player.getName(), level);
		exps.put(player.getName(), exp);
		if(enabled) {
			player.setLevel(level);
			displayExp(player);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(toUpdate.contains(name)) {
			UUID uuid = event.getUUID();
			if(levels.containsKey(name)) {
				int level = levels.get(name);
				DB.PLAYERS_LEVELS.updateInt("level", level, "uuid", uuid.toString());
			}
			if(exps.containsKey(name)) {
				int exp = exps.get(name);
				DB.PLAYERS_LEVELS.updateInt("exp", exp, "uuid", uuid.toString());
			}
			toUpdate.remove(name);
		}
		levels.remove(name);
		exps.remove(name);
	}
}
