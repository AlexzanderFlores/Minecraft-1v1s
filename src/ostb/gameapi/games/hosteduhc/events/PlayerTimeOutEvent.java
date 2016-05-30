package ostb.gameapi.games.hosteduhc.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerTimeOutEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;

    public PlayerTimeOutEvent(UUID uuid) {
        this.uuid = uuid;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getPlayer() {
        return this.uuid;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
