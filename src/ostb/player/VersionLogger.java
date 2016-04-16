package ostb.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.comphenix.protocol.ProtocolLibrary;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class VersionLogger implements Listener {
	public VersionLogger() {
		new CommandBase("versions") {
			@Override
			public boolean execute(final CommandSender sender, String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						int [] versions = new int [] {47, 48};
						Map<Integer, Integer> amounts = new HashMap<Integer, Integer>();
						int total = 0;
						for(int version : versions) {
							int amount = DB.PLAYERS_CHAT_LANGUAGE.getSize("language", version + "");
							total += amount;
							amounts.put(version, amount);
						}
						MessageHandler.sendMessage(sender, "Stats for " + total + " entires over " + versions.length + " versions:");
						for(int version : versions) {
							int percentage = (int) (amounts.get(version) * 100.0 / total + 0.5);
							MessageHandler.sendMessage(sender, version + " &a" + percentage + "%");
						}
						amounts.clear();
						amounts = null;
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER).enableDelay(2);
		if(OSTB.getPlugin() == Plugins.HUB) {
			EventUtil.register(this);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		int version = ProtocolLibrary.getProtocolManager().getProtocolVersion(player);
		player.sendMessage(""+version);
	}
}
