package network.gameapi.games.onevsones.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import network.gameapi.games.onevsones.Battle;
import network.gameapi.games.onevsones.kits.OneVsOneKit;

public class BattleEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player loser = null;
    private Player winner = null;
    private OneVsOneKit kit = null;
    private Battle battle = null;

    public BattleEndEvent(Player winner, Player loser, OneVsOneKit kit, Battle battle) {
        this.winner = winner;
        this.loser = loser;
        this.kit = kit;
        this.battle = battle;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getWinner() {
        return this.winner;
    }

    public Player getLoser() {
        return this.loser;
    }

    public OneVsOneKit getKit() {
        return this.kit;
    }

    public Battle getBattle() {
    	return this.battle;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}