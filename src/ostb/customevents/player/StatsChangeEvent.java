package ostb.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StatsChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private boolean cancelled = false;
    private StatsType type = null;
    public enum StatsType {
    	WIN, LOSS, KILL, DEATH
    }
    
    public StatsChangeEvent(Player player, StatsType type) {
    	this.player = player;
    	this.type = type;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public void setType(StatsType type) {
    	this.type = type;
    }
    
    public StatsType getType() {
    	return type;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
