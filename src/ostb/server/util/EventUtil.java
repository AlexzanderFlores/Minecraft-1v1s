package ostb.server.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import ostb.OSTB;

public class EventUtil {
	public static void register(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, OSTB.getInstance());
	}
}
