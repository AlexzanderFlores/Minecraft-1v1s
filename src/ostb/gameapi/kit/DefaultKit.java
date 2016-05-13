package ostb.gameapi.kit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.AsyncPostPlayerJoinEvent;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class DefaultKit implements Listener {
	private static Map<String, KitBase> defaultKits = null;
	
	public DefaultKit() {
		defaultKits = new HashMap<String, KitBase>();
		EventUtil.register(this);
	}
	
	public static void setDefaultKit(Player player, KitBase kit) {
		defaultKits.put(player.getName(), kit);
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		if(OSTB.getPlugin() == Plugins.HUB) {
			AsyncPostPlayerJoinEvent.getHandlerList().unregister(this);
		} else {
			Player player = event.getPlayer();
			UUID uuid = player.getUniqueId();
			for(String kitName : DB.PLAYERS_DEFAULT_KITS.getAllStrings("kit", new String [] {"uuid", "game"}, new String [] {uuid.toString(), OSTB.getPlugin().getData()})) {
				KitBase kit = null;
				for(KitBase kitBase : KitBase.getKits()) {
					if(kitBase.getName().equals(kitName)) {
						kit = kitBase;
						break;
					}
				}
				if(kit != null) {
					kit.use(player, true);
				}
			}
			Bukkit.getLogger().info("Loading default kits");
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getName();
		if(defaultKits.containsKey(name)) {
			KitBase kit = defaultKits.get(name);
			UUID uuid = event.getUUID();
			String game = kit.getPluginData();
			String [] keys = new String [] {"uuid", "game", "type"};
			String [] values = new String [] {uuid.toString(), game, kit.getKitType()};
			if(DB.PLAYERS_DEFAULT_KITS.isKeySet(keys, values)) {
				DB.PLAYERS_DEFAULT_KITS.updateString("kit", kit.getName(), keys, values);
			} else {
				DB.PLAYERS_DEFAULT_KITS.insert("'" + uuid.toString() + "', '" + game + "', '" + kit.getKitType() + "', '" + kit.getName() + "'");
			}
			Bukkit.getLogger().info("Set " + kit.getName() + " as default kit for " + name);
			defaultKits.remove(name);
		}
	}
}
