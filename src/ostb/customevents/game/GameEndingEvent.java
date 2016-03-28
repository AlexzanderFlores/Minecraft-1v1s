package ostb.customevents.game;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEndingEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
