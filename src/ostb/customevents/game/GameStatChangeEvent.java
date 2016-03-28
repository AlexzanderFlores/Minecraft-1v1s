package ostb.customevents.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ostb.gameapi.StatsHandler.GameStats;


public class GameStatChangeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private GameStats gameStats = null;
	private Player player = null;
	
	public GameStatChangeEvent(GameStats gameStats, Player player) {
		this.gameStats = gameStats;
		this.player = player;
	}
	
	public GameStats getGameStats() {
		return gameStats;
	}
	
	public Player getPlayer() {
		return player;
	}
	
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}