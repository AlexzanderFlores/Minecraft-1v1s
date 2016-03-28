package ostb.customevents.player;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerViewStatsEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player viewer = null;
    private UUID targetUUID = null;
    private String targetName = null;
    
    public PlayerViewStatsEvent(Player viewer, UUID targetUUID, String targetName) {
    	this.viewer = viewer;
    	this.targetUUID = targetUUID;
    	this.targetName = targetName;
    }
    
    public Player getPlayer() {
    	return this.viewer;
    }
    
    public UUID getTargetUUID() {
    	return this.targetUUID;
    }
    
    public String getTargetName() {
    	return this.targetName;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
