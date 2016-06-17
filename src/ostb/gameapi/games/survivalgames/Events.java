package ostb.gameapi.games.survivalgames;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpawnPointHandler;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.mapeffects.MapEffectHandler;
import ostb.player.MessageHandler;
import ostb.player.Particles.ParticleTypes;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class Events implements Listener {
    public static World arena = null;
    private List<Material> allowedToBreak = null;
    private List<Material> cannotSpawn = null;

    public Events() {
        allowedToBreak = Arrays.asList(
                Material.MELON_BLOCK, Material.WEB, Material.CAKE_BLOCK, Material.LONG_GRASS, Material.POTATO, Material.DEAD_BUSH,
                Material.CROPS, Material.CARROT, Material.LEAVES, Material.LEAVES_2, Material.VINE, Material.FIRE, Material.DOUBLE_PLANT
        );
        cannotSpawn = Arrays.asList(
                Material.SEEDS, Material.SAPLING, Material.INK_SACK, Material.SADDLE, Material.LEATHER, Material.STRING, Material.WOOD,
                Material.LEAVES, Material.LEAVES_2
        );
        EventUtil.register(this);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 20) {
            MiniGame miniGame = OSTB.getMiniGame();
            GameStates gameState = miniGame.getGameState();
            if (gameState == GameStates.WAITING) {
                ParticleTypes.HAPPY_VILLAGER.display(new Location(OSTB.getMiniGame().getLobby(), -6, 27.5, -42));
            } else if (gameState == GameStates.STARTING) {
                if (miniGame.getCounter() == 15) {
                    new MapEffectHandler(arena);
                } else if (miniGame.getCounter() == 5) {
                    for (Player player : ProPlugin.getPlayers()) {
                        player.getInventory().remove(Material.SNOW_BALL);
                    }
                }
            } else if (gameState == GameStates.STARTED) {
                if (miniGame.getCounter() <= 0) {
                    TimeEvent.getHandlerList().unregister(this);
                    //TODO: DM
                } else if (miniGame.getCounter() > 0) {
                    if (!GracePeriod.isRunning()) {
                        if (miniGame.getCounter() <= 5 || (miniGame.getCounter() < 60 && miniGame.getCounter() % 10 == 0)) {
                            MessageHandler.alert("Deathmatch in &e" + miniGame.getCounterAsString());
                        }
                    }
                    if (miniGame.canDisplay()) {
                        EffectUtil.playSound(Sound.CLICK);
                    }
                    OSTB.getSidebar().update("&aIn Game " + miniGame.getCounterAsString());
                }
            }
        }
    }

    @EventHandler
    public void onGameStarting(GameStartingEvent event) {
    	new SpawnPointHandler(OSTB.getMiniGame().getMap());
        new BelowNameHealthScoreboardUtil();
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        PlayerMoveEvent.getHandlerList().unregister(this);
        MiniGame miniGame = OSTB.getMiniGame();
        miniGame.setAllowFoodLevelChange(true);
        miniGame.setAllowDroppingItems(true);
        miniGame.setAllowPickingUpItems(true);
        miniGame.setDropItemsOnLeave(true);
        miniGame.setAllowEntityCombusting(true);
        miniGame.setAllowPlayerInteraction(true);
        miniGame.setAllowInventoryClicking(true);
        miniGame.setCounter(60 * 20);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!SpectatorHandler.contains(event.getPlayer())) {
            MiniGame miniGame = OSTB.getMiniGame();
            Location to = event.getTo();
            if (miniGame.getGameState() == GameStates.STARTING && to.getWorld().getName().equals(arena.getName())) {
                Location from = event.getFrom();
                if (to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) {
                    event.setTo(from);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (allowedToBreak.contains(event.getBlock().getType())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        event.setCancelled(false);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.TNT) {
        	event.getBlock().setType(Material.AIR);
            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(tnt.getFuseTicks() / 2);
            event.setCancelled(false);
        } else if (allowedToBreak.contains(block.getType())) {
        	event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH) // Priority is high so the death match cancellation will cancel this
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL && !event.isCancelled()) {
            event.getPlayer().teleport(event.getTo()); // Teleport manually to prevent damaging the player
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (cannotSpawn.contains(event.getEntity().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if (OSTB.getMiniGame().getGameState() != GameStates.STARTED) {
            event.setCancelled(true);
            event.getPlayer().closeInventory();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        checkForDeathmatchStart();
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        checkForDeathmatchStart();
    }

    private void checkForDeathmatchStart() {
        new DelayedTask(new Runnable() {
            @Override
            public void run() {
                if (ProPlugin.getPlayers().size() <= 4 && OSTB.getProPlugin().getCounter() > 61) {
                    OSTB.getProPlugin().setCounter(61);
                }
            }
        }, 10);
    }
}
