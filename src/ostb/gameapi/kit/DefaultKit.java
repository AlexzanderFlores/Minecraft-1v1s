package ostb.gameapi.kit;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class DefaultKit implements Listener {
	public DefaultKit() {
		EventUtil.register(this);
	}
	
	public static void setDefaultKit(final Player player, final KitBase kit) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID uuid = player.getUniqueId();
				String game = kit.getPlugin().toString();
				String [] keys = new String [] {"uuid", "game", "type"};
				String [] values = new String [] {uuid.toString(), game, kit.getKitType()};
				if(DB.PLAYERS_DEFAULT_KITS.isKeySet(keys, values)) {
					DB.PLAYERS_DEFAULT_KITS.updateString("kit", kit.getName(), keys, values);
				} else {
					DB.PLAYERS_DEFAULT_KITS.insert("'" + uuid.toString() + "', '" + game + "', '" + kit.getKitType() + "', '" + kit.getName() + "'");
				}
				Bukkit.getLogger().info("Set " + kit.getName() + " as default kit for " + player.getName());
			}
		});
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		for(String kitName : DB.PLAYERS_DEFAULT_KITS.getAllStrings("kit", new String [] {"uuid", "game"}, new String [] {uuid.toString(), OSTB.getPlugin().toString()})) {
			KitBase kit = null;
			for(KitBase kitBase : KitBase.getKits()) {
				if(kitBase.getName().equals(kitName)) {
					kit = kitBase;
					break;
				}
			}
			if(kit != null) {
				kit.use(player);
			}
		}
		Bukkit.getLogger().info("Loading default kits");
	}
}
