package ostb.player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class LanguageLogger implements Listener {
	private enum Languages {ENGLISH, SPANISH, OTHER}
	
	public LanguageLogger() {
		new CommandBase("lang") {
			@Override
			public boolean execute(final CommandSender sender, String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Map<Languages, Integer> amounts = new HashMap<Languages, Integer>();
						int total = 0;
						for(Languages language : Languages.values()) {
							int amount = DB.PLAYERS_CHAT_LANGUAGE.getSize("language", language.toString());
							total += amount;
							amounts.put(language, amount);
						}
						MessageHandler.sendMessage(sender, "Stats for " + total + " entries over " + Languages.values().length + " languages:");
						for(Languages language : Languages.values()) {
							int percentage = (int) (amounts.get(language) * 100.0 / total + 0.5);
							MessageHandler.sendMessage(sender, language.toString() + " &a" + percentage + "%");
						}
						amounts.clear();
						amounts = null;
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER).enableDelay(2);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
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
		if(language.equals("en_US") || language.equals("en_GB")) {
			language = Languages.ENGLISH.toString();
		} else if(language.equals("es_AR") || language.equals("es_ES") || language.equals("es_MX") || language.equals("es_UY") || language.equals("es_VE")) {
			language = Languages.SPANISH.toString();
		} else {
			language = Languages.OTHER.toString();
		}
		UUID uuid = player.getUniqueId();
		if(DB.PLAYERS_CHAT_LANGUAGE.isUUIDSet(uuid)) {
			DB.PLAYERS_CHAT_LANGUAGE.updateString("language", language, "uuid", uuid.toString());
		} else {			
			DB.PLAYERS_CHAT_LANGUAGE.insert("'" + uuid.toString() + "', '" + language + "'");
		}
	}
}
