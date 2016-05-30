package ostb.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import anticheat.AntiCheatBase;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GracePeriodEndEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.customevents.player.PrivateMessageEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.uhc.border.BorderHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class HostedEvent implements Listener {
    private static HostedEvent instance = null;
    private static boolean isEvent = false;
    private static List<String> died = null;
    private static final String eventName = "Elite UHC";

    public HostedEvent() {
        instance = this;
        new CommandBase("toggleEvent") {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                if(sender instanceof Player) {
                    Player player = (Player) sender;
                    Player main = HostHandler.getMainHost();
                    if(main == null || main.getUniqueId() != player.getUniqueId()) {
                        MessageHandler.sendMessage(player, "&cYou must be the main UHC host to use this command");
                        return true;
                    }
                }
                if(isEvent) {
                    disable(sender);
                } else {
                    enable(sender);
                }
                return true;
            }
        };
        //enable(Bukkit.getConsoleSender());
    }
    
    public static String getEventName() {
    	return eventName;
    }

    public static boolean isEvent() {
        return isEvent;
    }

    private void enable(CommandSender sender) {
        isEvent = true;
        if(sender != null) {
            MessageHandler.sendMessage(sender, "Event mode is now &eON");
        }
        EventUtil.register(instance);
        AntiCheatBase.setEnabled(false);
        TeamHandler.setMaxTeamSize(2);
    }

    private void disable(CommandSender sender) {
        isEvent = false;
        MessageHandler.sendMessage(sender, "Event mode is now &cOFF");
        HandlerList.unregisterAll(instance);
        AntiCheatBase.setEnabled(true);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(Ranks.isStaff(event.getPlayer()) && !Ranks.OWNER.hasRank(event.getPlayer())) {
            MessageHandler.sendMessage(event.getPlayer(), "&cTo talk in chat during an event you must have " + Ranks.OWNER.getPrefix());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrivateMessage(PrivateMessageEvent event) {
        if(Ranks.isStaff(event.getPlayer()) && !Ranks.OWNER.hasRank(event.getPlayer())) {
            MessageHandler.sendMessage(event.getPlayer(), "&cTo private message during an event you must have " + Ranks.OWNER.getPrefix());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGracePeriodEnd(GracePeriodEndEvent event) {
        new BorderHandler();
        Events.setMoveToCenter(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(died == null) {
            died = new ArrayList<String>();
        }
        died.add(event.getEntity().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final String name = event.getPlayer().getName();
        event.setRespawnLocation(new Location(OSTB.getMiniGame().getLobby(), 0.5, 26, 0.5, -90.0f, -0.0f));
        new DelayedTask(new Runnable() {
            @Override
            public void run() {
                Player player = ProPlugin.getPlayer(name);
                if(player != null) {
                    MessageHandler.sendLine(player, "&e");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendMessage(player, "                    &aThank you for playing &5" + getEventName());
                    MessageHandler.sendMessage(player, "                     &aHosted by &bplay.OSTB.com");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendMessage(player, "");
                    MessageHandler.sendLine(player, "&e");
                    SpectatorHandler.remove(player);
                    player.closeInventory();
                    player.teleport(new Location(OSTB.getMiniGame().getLobby(), 0.5, 26, 0.5, -90.0f, -0.0f));
                }
            }
        }, 20 * 1);
        new DelayedTask(new Runnable() {
            @Override
            public void run() {
                Player player = ProPlugin.getPlayer(name);
                if(player != null) {
                    ProPlugin.sendPlayerToServer(player, "hub");
                }
                died.remove(name);
            }
        }, 20 * 8);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
        if(event.getState() == SpectatorState.ADDED && died != null && died.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
