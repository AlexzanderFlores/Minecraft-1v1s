package ostb.player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class LanguageChannels implements Listener {
	public enum LanguageChannel {
		ALL, NONE, ENGLISH, SPANISH
	}
	private Map<String, LanguageChannel> channels = null;
	private String name = null;
	
	public LanguageChannels() {
		channels = new HashMap<String, LanguageChannel>();
		name = "Language Selection";
		new CommandBase("lang", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Inventory inventory = Bukkit.createInventory(player, 9 * 4, name);
				
				player.openInventory(inventory);
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if(DB.PLAYERS_CHAT_LANGUAGE.isUUIDSet(uuid)) {
			channels.put(player.getName(), LanguageChannel.valueOf(DB.PLAYERS_CHAT_LANGUAGE.getString("uuid", uuid.toString(), "language")));
		} else {
			String language = null;
			try {
				Object object = null;
				for(Method method : player.getClass().getDeclaredMethods()) {
					if(method.getName().equals("getHandle")) {
						object = method.invoke(player, (Object []) null);
						Field field = object.getClass().getDeclaredField("locale");
						field.setAccessible(true);
						language = (String) field.get(object);
						break;
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(language == null) {
				channels.put(player.getName(), LanguageChannel.ALL);
			} else {
				if(language.equals("en_US") || language.equals("en_GB")) {
					channels.put(player.getName(), LanguageChannel.ENGLISH);
				} else if(language.equals("es_AR") || language.equals("es_ES") || language.equals("es_MX") || language.equals("es_UY") || language.equals("es_VE")) {
					channels.put(player.getName(), LanguageChannel.SPANISH);
				} else {
					channels.put(player.getName(), LanguageChannel.ALL);
				}
			}
			DB.PLAYERS_CHAT_LANGUAGE.insert("'" + uuid.toString() + "', '" + channels.get(player.getName()) + "'");
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		channels.remove(event.getPlayer().getName());
	}
}
