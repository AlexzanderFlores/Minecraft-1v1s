package ostb.gameapi.games.battles;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.game.GameKillEvent;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		event.getPlayer().setLevel(event.getPlayer().getLevel() + 1);
		EffectUtil.playSound(event.getPlayer(), Sound.LEVEL_UP);
	}
}
