package ostb.gameapi.games.pvpbattles;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.game.GameKillEvent;
import ostb.player.LevelGiver;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new LevelGiver(event.getPlayer());
	}
}
