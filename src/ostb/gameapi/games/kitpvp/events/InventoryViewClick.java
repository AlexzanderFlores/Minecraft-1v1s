package ostb.gameapi.games.kitpvp.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InventoryViewClick extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private int slot = 0;
    
    public InventoryViewClick(Player player, int slot) {
    	this.player = player;
    	this.slot = slot;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public int getSlot() {
    	return this.slot;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
