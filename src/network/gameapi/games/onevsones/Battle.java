package network.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import network.Network;
import network.Network.Plugins;
import network.ProPlugin;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerStaffModeEvent;
import network.customevents.player.StatsChangeEvent;
import network.gameapi.competitive.EloHandler;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.events.BattleStartEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Battle implements Listener {
    private List<String> sentMessage = null;
    private List<String> pearlDelay = null;
    private List<Block> placedBlocks = null;
    private List<Item> items = null;
    private Player playerOne = null;
    private Player playerTwo = null;
    private OneVsOneKit kit = null;
    private int map = 0;
    private int timer = 0;
    private int startAt = 3;
    private Location targetLocation = null;
    private boolean started = false;
    private boolean tournament = false;
    private boolean ranked = true;

    public Battle(int map, Location targetLocation, Player playerOne, Player playerTwo, boolean tournament, boolean ranked) {
        this.sentMessage = new ArrayList<String>();
        this.pearlDelay = new ArrayList<String>();
        this.placedBlocks = new ArrayList<Block>();
        this.items = new ArrayList<Item>();
        this.map = map;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.kit = OneVsOneKit.getPlayersKit(playerOne);
        this.targetLocation = targetLocation;
        this.tournament = tournament;
        this.ranked = ranked;
        BattleHandler.addPlayerBattle(playerOne, this);
        BattleHandler.addPlayerBattle(playerTwo, this);
        playerOne.getInventory().remove(Material.SLIME_BALL);
        playerOne.getInventory().remove(Material.MAGMA_CREAM);
        Location locationOne = targetLocation.clone().add(25, 15, 0);
        locationOne.setYaw(-270.0f);
        playerOne.teleport(locationOne);
        for(PotionEffect effect : playerOne.getActivePotionEffects()) {
            playerOne.removePotionEffect(effect.getType());
        }
        playerOne.setAllowFlight(false);
        playerOne.getLocation().setPitch(0.0f);
        if(!HotbarEditor.load(playerOne, kit)) {
        	kit.give(playerOne);
        }
        Location locationTwo = targetLocation.clone().add(-25, 15, 0);
        locationTwo.setYaw(-90.0f);
        MessageHandler.sendMessage(playerOne, "To quit this battle do &e/quit");
        if(playerTwo == null) {
            MessageHandler.sendMessage(playerOne, "There was an odd number of players, you must wait for a match to be available");
        } else {
            playerOne.hidePlayer(playerTwo);
            playerTwo.hidePlayer(playerOne);
            MessageHandler.sendMessage(playerTwo, "To quit this battle do &e/quit");
            playerTwo.teleport(locationTwo);
            for(PotionEffect effect : playerTwo.getActivePotionEffects()) {
                playerTwo.removePotionEffect(effect.getType());
            }
            playerTwo.setAllowFlight(false);
            playerTwo.getLocation().setPitch(0.0f);
            if(!HotbarEditor.load(playerTwo, kit)) {
            	kit.give(playerTwo);
            }
            BattleHandler.addBattle(this);
        }
        BattleHandler.setTargetX(targetLocation, map);
        EventUtil.register(this);
    }

    public boolean contains(Player player) {
        try {
        	return playerOne.getName().equals(player.getName()) || playerTwo.getName().equals(player.getName());
        } catch(NullPointerException e) {
        	return false;
        }
    }

    public Player getCompetitor(Player player) {
        if(playerOne.getName().equals(player.getName())) {
            return playerTwo;
        } else {
            return playerOne;
        }
    }
    
    public Player [] getPlayers() {
    	return new Player [] {playerOne, playerTwo};
    }

    public List<Block> getPlacedBlocks() {
        return this.placedBlocks;
    }

    public OneVsOneKit getKit() {
        return this.kit;
    }

    public int getMapNumber() {
        return this.map;
    }

    public Location getTargetLocation() {
        return this.targetLocation;
    }

    public void incrementTimer() {
        ++this.timer;
    }

    public int getTimer() {
        return this.timer;
    }

    public boolean isRanked() {
        return ranked;
    }

    public boolean isTournament() {
        return tournament;
    }

    public void start() {
        started = true;
        playerOne.showPlayer(playerTwo);
        playerTwo.showPlayer(playerOne);
        OneVsOneKit kit = OneVsOneKit.getPlayersKit(playerOne);
        if(kit != null) {
            String name = kit.getName();
            if(name.equals("One Hit Wonder") || name.equals("Quickshot")) {
                playerOne.setHealth(1.0d);
                playerTwo.setHealth(1.0d);
            }
        }
        Bukkit.getPluginManager().callEvent(new BattleStartEvent(this));
    }

    public boolean isStarted() {
        return this.started;
    }

    public void end(Player loser) {
    	Player winner = getCompetitor(loser);
    	Bukkit.getPluginManager().callEvent(new BattleEndEvent(this));
    	if(ranked) {
        	EloHandler.calculateWin(winner, loser, 1);
    	}
        playerOne.setFireTicks(0);
        playerTwo.setFireTicks(0);
        List<Location> maps = MapProvider.openMaps.get(getMapNumber());
        if(maps == null) {
            maps = new ArrayList<Location>();
        }
        maps.add(targetLocation);
        MapProvider.openMaps.put(getMapNumber(), maps);
        if(Network.getPlugin() == Plugins.ONEVSONE) {
            ProPlugin.resetPlayer(playerOne);
            ProPlugin.resetPlayer(playerTwo);
            if(!tournament) {
                LobbyHandler.spawn(playerOne);
                LobbyHandler.spawn(playerTwo);
            }
        }
        sentMessage.remove(playerOne.getName());
        sentMessage.remove(playerTwo.getName());
        BattleHandler.removePlayerBattle(playerOne);
        BattleHandler.removePlayerBattle(playerTwo);
        playerOne = null;
        playerTwo = null;
        if(placedBlocks != null) {
            for(Block block : placedBlocks) {
                block.setType(Material.AIR);
                block.setData((byte) 0);
            }
            placedBlocks.clear();
            placedBlocks = null;
        }
        BattleHandler.removeBattle(this);
        BattleHandler.removeMapCoord(targetLocation);
        HandlerList.unregisterAll(this);
        for(Item item : items) {
        	if(item != null && !item.isDead()) {
        		item.remove();
        	}
        }
        items.clear();
        items = null;
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if(contains(event.getPlayer())) {
            end(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(contains(player)) {
                String name = kit.getName();
                if((name.equals("Build UHC") || (name.equals("Speed UHC"))) && event.getRegainReason() == RegainReason.SATIATED) {
                	event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(contains(event.getPlayer())) {
        	for(Entity entity : event.getPlayer().getNearbyEntities(5, 5, 5)) {
    			if(entity instanceof Item) {
    				entity.remove();
    			}
    		}
            end(event.getPlayer());
        }
    }

    @EventHandler
    public void onStatsChange(StatsChangeEvent event) {
        if(contains(event.getPlayer()) && !isRanked()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(contains(event.getPlayer()) && event.getBlock().getY() < 10) {
            if(isStarted()) {
                Material type = event.getBlock().getType();
                if(type == Material.TNT) {
                    Player player = event.getPlayer();
                    ItemStack item = event.getItemInHand();
                    int amount = item.getAmount();
                    if(amount <= 1) {
                        player.setItemInHand(new ItemStack(Material.AIR));
                    } else {
                        player.setItemInHand(new ItemStack(type, amount - 1));
                    }
                    TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(event.getBlock().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(tnt.getFuseTicks() / 2);
                } else if(type == Material.FIRE || type == Material.COBBLESTONE) {
                    if(!placedBlocks.contains(event.getBlock())) {
                        placedBlocks.add(event.getBlock());
                    }
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(contains(player) && placedBlocks.contains(event.getBlock())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if(event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if(arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();
                if(contains(player) && getTimer() < startAt) {
                    MessageHandler.sendMessage(player, "&cCannot shoot your bow at this time");
                    event.setCancelled(true);
                }
            }
        } else if(event.getEntity() instanceof EnderPearl) {
        	EnderPearl enderPearl = (EnderPearl) event.getEntity();
        	if(enderPearl.getShooter() instanceof Player) {
        		Player player = (Player) enderPearl.getShooter();
        		if(contains(player) && getTimer() < startAt) {
        			MessageHandler.sendMessage(player, "&cCannot throw Ender Pearls at this time");
                    event.setCancelled(true);
        		} else {
        			final String name = player.getName();
            		if(!pearlDelay.contains(name)) {
            			pearlDelay.add(name);
            			new DelayedTask(new Runnable() {
    						@Override
    						public void run() {
    							pearlDelay.remove(name);
    						}
    					}, 20 * 15);
            		}
        		}
        	}
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();
    	if(pearlDelay.contains(player.getName())) {
    		if(itemStack != null && itemStack.getType() == Material.ENDER_PEARL) {
    			MessageHandler.sendMessage(player, "&cYou can only throw an Ender Pearl once every 15s");
    			event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    	event.setCancelled(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(contains(event.getPlayer()) && getTimer() < startAt) {
        	Location to = event.getTo();
        	Location from = event.getFrom();
        	if(to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) {
        		if(!sentMessage.contains(event.getPlayer().getName())) {
                    sentMessage.add(event.getPlayer().getName());
                }
                event.setTo(event.getFrom());
        	}
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            if(contains(player)) {
                if(isStarted()) {
                    Location to = event.getTo();
                    if(to.getBlockY() > 7 || !player.getLocation().toVector().isInSphere(to.toVector(), 65)) {
                        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                        MessageHandler.sendMessage(player, "&cCannot teleport to that location");
                        event.setCancelled(true);
                    }
                } else {
                    player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                    MessageHandler.sendMessage(player, "&cYou cannot use Ender Pearls at this time");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
    	if(contains(event.getPlayer())) {
    		items.add(event.getItemDrop());
    		event.setCancelled(false);
    	}
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    	if(contains(event.getPlayer())) {
    		event.setCancelled(false);
    	}
    }
    
    @EventHandler
    public void onPlayerStaffMode(PlayerStaffModeEvent event) {
    	if(contains(event.getPlayer())) {
    		event.setCancelled(true);
    	}
    }
}
