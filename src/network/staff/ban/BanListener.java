package network.staff.ban;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import network.anticheat.events.PlayerBanEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.util.EventUtil;

public class BanListener implements Listener {
	public BanListener() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerBan(PlayerBanEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(Ranks.isStaff(player)) {
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, event.getName() + " was MEANT to be banned for " + event.getReason() + " (" + (event.getQueue() ? "true" : "false") + ") &c&lTELL LEET THIS");
				MessageHandler.sendMessage(player, "");
			}
		}
	}
}
