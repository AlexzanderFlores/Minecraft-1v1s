package ostb.customevents.timed;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FiveMinuteTaskEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
