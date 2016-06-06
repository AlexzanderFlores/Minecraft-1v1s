package ostb.gameapi.games.uhcskywars;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.game.GameStartingEvent;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		OSTB.getMiniGame().getMap().setGameRuleValue("naturalRegeneration", "false");
	}
}
