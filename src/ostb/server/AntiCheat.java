package ostb.server;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.events.PlayerBanEvent;
import ostb.player.account.AccountHandler;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class AntiCheat implements Listener {
	public AntiCheat() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerBan(final PlayerBanEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String name = event.getName();
				if(name == null) {
					name = AccountHandler.getName(event.getUUID());
				}
				String reason = event.getReason();
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + name + " " + reason);
			}
		});
	}
}
