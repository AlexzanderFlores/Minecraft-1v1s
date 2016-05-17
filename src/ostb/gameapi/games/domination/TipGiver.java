package ostb.gameapi.games.domination;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import anticheat.events.TimeEvent;
import ostb.OSTB;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.MessageHandler;
import ostb.server.util.EventUtil;

public class TipGiver implements Listener {
	private List<String> tips = null;
	private int index = 0;
	
	public TipGiver() {
		tips = new ArrayList<String>();
		tips.add("Get exp bottles in the shop to use to &eEnchant &xand &eGet Armor");
		tips.add("Get more coins daily with &e/vote");
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60 && OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
			if(index >= tips.size()) {
				index = 0;
			}
			MessageHandler.alert("&bTIP: &x" + tips.get(index++));
		}
	}
}
