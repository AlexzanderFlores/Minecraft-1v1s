package ostb.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.customevents.player.PlayerStaffModeEvent;
import ostb.customevents.player.PlayerStaffModeEvent.StaffModeEventType;
import ostb.gameapi.games.onevsones.kits.OneVsOneKit;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class QueueHandler implements Listener {
    private static List<QueueData> queueData = null;
    private static List<String> waitingForMap = null;

    public QueueHandler() {
        queueData = new ArrayList<QueueData>();
        waitingForMap = new ArrayList<String>();
        EventUtil.register(this);
    }

    public static void add(Player player, OneVsOneKit kit) {
        remove(player);
        PrivateBattleHandler.removeAllInvitesFromPlayer(player);
        new QueueData(player, null, kit, RankedMatches.isPlayingRanked(player));
        MessageHandler.sendMessage(player, "Looking for a game to join with kit " + kit.getName() + "...");
        if (!Ranks.PREMIUM.hasRank(player)) {
            MessageHandler.sendMessage(player, "&cNote: " + Ranks.PREMIUM.getPrefix() + "&eand above get &c5x &efaster queuing time! &b/buy");
        }
    }

    public static void remove(Player player) {
        Iterator<QueueData> iterator = queueData.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getPlayer().equals(player.getName())) {
                iterator.remove();
                MessageHandler.sendMessage(player, "Removed you from your last game search");
                break;
            }
        }
        waitingForMap.remove(player.getName());
    }

    public static boolean isInQueue(Player player) {
        for (QueueData data : queueData) {
            if (data.isPlaying(player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWaitingForMap(Player player) {
        return waitingForMap.contains(player.getName());
    }

    public static void gotMap(Player player) {
        waitingForMap.remove(player.getName());
    }

    private void processQueue(final boolean priority) {
        new AsyncDelayedTask(new Runnable() {
            @Override
            public void run() {
            	for(final QueueData data : queueData) {
        			if (!data.isPrioirty() && data.getCounter() < 5) {
                        Bukkit.getLogger().info("skipping " + data.getPlayer() + " due not being in queue for only " + data.getCounter() + "s");
                        continue;
                    }
        			for(final QueueData comparingData : queueData) {
        				if (!comparingData.isPrioirty() && comparingData.getCounter() < 5) {
                            Bukkit.getLogger().info("skipping " + comparingData.getPlayer() + " due not being in queue for only " + comparingData.getCounter() + "s");
                            continue;
                        }
                        if ((data.isPrioirty() == priority || comparingData.isPrioirty() == priority) && data.canJoin(comparingData) && data.isRanked() == comparingData.isRanked()) {
                        	final Player playerOne;
                            final Player playerTwo;
                            if (data.getForcedPlayer() != null && comparingData.getForcedPlayer() != null) {
                                playerOne = ProPlugin.getPlayer(data.getPlayer());
                                playerTwo = ProPlugin.getPlayer(comparingData.getForcedPlayer());
                                ProPlugin.resetPlayer(playerOne);
                                ProPlugin.resetPlayer(playerTwo);
                            } else {
                                playerOne = ProPlugin.getPlayer(data.getPlayer());
                                playerTwo = ProPlugin.getPlayer(comparingData.getPlayer());
                            }
                            String text = Ranks.PREMIUM.getPrefix() + "&cPerk: &e5x faster queuing time &b/buy";
                            if (AccountHandler.getRank(playerOne) == Ranks.PLAYER) {
                                MessageHandler.sendMessage(playerOne, text);
                            }
                            if (AccountHandler.getRank(playerTwo) == Ranks.PLAYER) {
                                MessageHandler.sendMessage(playerTwo, text);
                            }
                            remove(playerOne);
                            remove(playerTwo);
                            waitingForMap.add(playerOne.getName());
                            waitingForMap.add(playerTwo.getName());
                            new DelayedTask(new Runnable() {
                                @Override
                                public void run() {
                                	queueData.remove(data);
                                	queueData.remove(comparingData);
                                    new MapProvider(playerOne, playerTwo, playerOne.getWorld(), false, true);
                                }
                            });
                            return;
                        }
            		}	
        		}
            }
        });
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 20) {
            for (QueueData data : queueData) {
                data.incrementCounter();
            }
            processQueue(true);
        } else if (ticks == 20 * 5) {
            processQueue(false);
        }
    }

    @EventHandler
    public void onPlayerStaffMode(PlayerStaffModeEvent event) {
        Player player = event.getPlayer();
        if (event.getType() == StaffModeEventType.ENABLE && isInQueue(player)) {
            MessageHandler.sendMessage(player, "&cStaff Mode auto-vanish cancelled: You're in a queue");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
    	if(event.getState() == SpectatorState.ADDED) {
    		remove(event.getPlayer());
    	}
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        remove(event.getPlayer());
    }

    public static class QueueData {
        private boolean priority = false;
        private String player = null;
        private String forcedPlayer = null;
        private OneVsOneKit kit = null;
        private int counter = 0;
        private boolean ranked = false;

        public QueueData(Player player, Player playerTwo, OneVsOneKit kit, boolean ranked) {
            if (Ranks.PREMIUM.hasRank(player) || Ranks.PREMIUM.hasRank(playerTwo)) {
                priority = true;
            }
            this.player = player.getName();
            if (playerTwo != null) {
                this.forcedPlayer = playerTwo.getName();
            }
            this.kit = kit;
            this.ranked = ranked;
            queueData.add(this);
        }

        public boolean canJoin(QueueData data) {
            Player playerOne = ProPlugin.getPlayer(player);
            Player playerTwo = ProPlugin.getPlayer(data.getPlayer());
            if (playerOne.getAddress().getAddress().getHostAddress().equals(playerTwo.getAddress().getAddress().getHostAddress())) {
                return false;
            }
            if (forcedPlayer != null && data.getForcedPlayer() != null) {
                return data.getForcedPlayer().equals(forcedPlayer);
            } else {
                return !this.getPlayer().equals(data.getPlayer()) && this.getKit() == data.getKit();
            }
        }

        public boolean isPrioirty() {
            return this.priority;
        }

        public String getPlayer() {
            return this.player;
        }

        public String getForcedPlayer() {
            return this.forcedPlayer;
        }

        public OneVsOneKit getKit() {
            return this.kit;
        }
        
        public boolean isRanked() {
        	return this.ranked;
        }

        public int getCounter() {
            return this.counter;
        }

        public int incrementCounter() {
            return ++this.counter;
        }

        public boolean isPlaying(Player player) {
            return getPlayer().equals(player.getName()) || (getForcedPlayer() != null && getForcedPlayer().equals(player.getName()));
        }
    }
}
