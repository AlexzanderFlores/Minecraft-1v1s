package network.gameapi.games.onevsones.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import network.gameapi.games.onevsones.Battle;

public class BattleEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Battle battle = null;

    public BattleEndEvent(Battle battle) {
        this.battle = battle;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    public Battle getBattle() {
    	return this.battle;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}