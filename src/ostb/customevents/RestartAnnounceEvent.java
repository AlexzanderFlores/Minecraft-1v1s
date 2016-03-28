package ostb.customevents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RestartAnnounceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private int counter = 0;
    
    public RestartAnnounceEvent(int counter) {
    	this.counter = counter;
    }
    
    public int getCounter() {
    	return this.counter;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
