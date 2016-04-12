package ostb.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class LangaugeHandler implements Listener {
	public enum Language {ALL, NONE, ENGLISH, SPANISH}
	private String name = null;
	private Map<String, Language> sendingLanguages = null;
	private Map<String, Language> viewingLanguages = null;
	private List<String> updated = null;
	
	public LangaugeHandler() {
		name = "Langauge Selector";
		sendingLanguages = new HashMap<String, Language>();
		viewingLanguages = new HashMap<String, Language>();
		updated = new ArrayList<String>();
		new CommandBase("lang", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
				
				player.openInventory(inventory);
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		UUID uuid = player.getUniqueId();
		if(DB.PLAYERS_CHAT_LANGUAGE.isUUIDSet(uuid)) {
			sendingLanguages.put(name, Language.valueOf(DB.PLAYERS_CHAT_LANGUAGE.getString("uuid", uuid.toString(), "sending_language")));
			viewingLanguages.put(name, Language.valueOf(DB.PLAYERS_CHAT_LANGUAGE.getString("uuid", uuid.toString(), "viewing_language")));
		} else {
			sendingLanguages.put(name, Language.ALL);
			viewingLanguages.put(name, Language.ALL);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(sendingLanguages.containsKey(name)) {
			if(updated.contains(name)) {
				
			}
			sendingLanguages.remove(name);
		}
		if(viewingLanguages.containsKey(name)) {
			if(updated.contains(name)) {
				
			}
			viewingLanguages.remove(name);
		}
	}
}
