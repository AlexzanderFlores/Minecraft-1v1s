package ostb.gameapi.shops.crates;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ostb.OSTB.Plugins;

public class CrateFinishedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player= null;
    private Plugins plugin = null;
    
    public CrateFinishedEvent(Player player, Plugins plugin) {
    	this.player = player;
    	this.plugin = plugin;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Plugins getPlugin() {
    	return this.plugin;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
