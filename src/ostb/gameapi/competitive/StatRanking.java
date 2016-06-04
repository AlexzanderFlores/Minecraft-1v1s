package ostb.gameapi.competitive;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.game.GameDeathEvent;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class StatRanking implements Listener {
	public StatRanking() {
		if(OSTB.getMiniGame() != null && !OSTB.getMiniGame().getPlayersHaveOneLife()) {
			return;
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		//final Player player = event.getPlayer();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				
			}
		}, 20);
	}
}
