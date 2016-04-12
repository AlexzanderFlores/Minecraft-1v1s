package ostb.gameapi.shops.crates;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ostb.OSTB.Plugins;
import ostb.server.servers.hub.items.features.FeatureItem;

public class CrateFinishedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player= null;
    private Plugins plugin = null;
    private FeatureItem won = null;
    
    public CrateFinishedEvent(Player player, Plugins plugin, FeatureItem won) {
    	this.player = player;
    	this.plugin = plugin;
    	this.won = won;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Plugins getPlugin() {
    	return this.plugin;
    }
    
    public FeatureItem getItemWon() {
    	return this.won;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
