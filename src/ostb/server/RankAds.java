package ostb.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.TimeEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;

public class RankAds implements Listener {
	private String [] alerts = null;
	private int counter = 0;
	
	public RankAds() {
		alerts = new String [] {
			"Ranks give you &cx2 &xcoins!",
			"Ranks allow you to fly in hubs",
			"Ranks bypass all rank ads",
			"Ranks allow you to join full servers",
			"Ranks display your skin on all hubs"
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.PREMIUM.hasRank(player)) {
					MessageHandler.sendMessage(player, "&a&l[TIP] &x" + alerts[counter] + " &b/buy");
				}
			}
			if(++counter >= alerts.length) {
				counter = 0;
			}
		}
	}
}
