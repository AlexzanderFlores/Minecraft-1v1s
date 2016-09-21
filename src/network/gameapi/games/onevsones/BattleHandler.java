package network.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import network.Network;
import network.ProPlugin;
import network.Network.Plugins;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.games.onevsones.events.BattleEndEvent;
import network.gameapi.games.onevsones.kits.OneVsOneKit;
import network.player.MessageHandler;
import network.server.CommandBase;
import network.server.util.DoubleUtil;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.UnicodeUtil;

public class BattleHandler implements Listener {
    private static List<Battle> battles = null;
    private static Map<String, Battle> playerBattles = null;
    private static Map<Location, Integer> mapCoords = null; // <target X, map number>
    private final BlockFace[] faces = new BlockFace[]{
            BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
            BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    public BattleHandler() {
        battles = new ArrayList<Battle>();
        playerBattles = new HashMap<String, Battle>();
        mapCoords = new HashMap<Location, Integer>();
        if(Network.getPlugin() == Plugins.ONEVSONE) {
            new CommandBase("quit", true) {
                @Override
                public boolean execute(CommandSender sender, String[] arguments) {
                    Player player = (Player) sender;
                    if(QueueHandler.isInQueue(player)) {
                        QueueHandler.remove(player);
                        ProPlugin.resetPlayer(player);
                        LobbyHandler.spawn(player);
                    } else {
                        Battle battle = getBattle(player);
                        if(battle == null) {
                            MessageHandler.sendMessage(player, "&cNo battle detected, still sending you to the spawn");
                            QueueHandler.remove(player);
                            LobbyHandler.spawn(player);
                        } else {
                            MessageHandler.sendMessage(player, "You were given a death for quiting");
                            Player competitor = battle.getCompetitor(player);
                            if(competitor != null) {
                                MessageHandler.sendMessage(competitor, "You were given a kill for your opponent quiting");
                                StatsHandler.addKill(competitor);
                                StatsHandler.addDeath(player);
                            }
                            battle.end(player);
                        }
                    }
                    return true;
                }
            };
            new CommandBase("test", true) {
                @Override
                public boolean execute(CommandSender sender, String[] arguments) {
                    Player player = (Player) sender;
                    Block block = player.getLocation().getBlock();
                    Battle battle = getBattle(player);
                    if(battle != null && battle.isStarted()) {
                        if(battle.getPlacedBlocks().contains(block)) {
                            MessageHandler.sendMessage(player, "&eYES");
                        } else {
                            MessageHandler.sendMessage(player, "&cNO");
                        }
                    }
                    return true;
                }
            };
        }
        EventUtil.register(this);
    }

    public static List<Battle> getBattles() {
        return battles;
    }

    public static Battle getBattle(Player player) {
        return playerBattles.get(player.getName());
        /*for(Battle battle : battles) {
            if(battle.contains(player)) {
				return battle;
			}
		}
		return null;*/
    }

    public static void addPlayerBattle(Player player, Battle battle) {
        playerBattles.put(player.getName(), battle);
    }

    public static void removePlayerBattle(Player player) {
        playerBattles.remove(player.getName());
    }

    public static boolean isInBattle(Player player) {
        return getBattle(player) != null;
    }

    public static void addBattle(Battle battle) {
        battles.add(battle);
    }

    public static void removeBattle(Battle battle) {
        battles.remove(battle);
    }

    public static int getMapNumber(int targetX) {
        return mapCoords.get(targetX);
    }

    public static void setTargetX(Location targetLocation, int mapNumber) {
        mapCoords.put(targetLocation, mapNumber);
    }

    public static void removeMapCoord(Location targetLocation) {
        mapCoords.remove(targetLocation);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EffectUtil.playSound(player, Sound.ZOMBIE_DEATH);
        Player killer = player.getKiller();
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(killer, player, OneVsOneKit.getPlayersKit(player)));
        if(killer == null) {
            if(Network.getPlugin() == Plugins.ONEVSONE) {
                player.sendMessage(event.getDeathMessage());
            }
        } else {
            EffectUtil.playSound(killer, Sound.LEVEL_UP);
            double health = DoubleUtil.round(((double) killer.getHealth() / 2), 2);
            if(health <= 0) {
                health = 0.10;
            }
            event.setDeathMessage(event.getDeathMessage() + ChatColor.translateAlternateColorCodes('&', " &fwith &c" + health + " &4" + UnicodeUtil.getHeart()));
            if(Network.getPlugin() == Plugins.ONEVSONE) {
                MessageHandler.sendMessage(player, event.getDeathMessage());
                MessageHandler.sendMessage(killer, event.getDeathMessage());
            }
        }
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.blockList() != null) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if(event.getEntity() instanceof Arrow) {
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getCause() == DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if(event.getBlockClicked().getLocation().getY() != 3) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(event.getItemDrop().getItemStack().getType() == Material.POTION) {
            event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if(event.getBlock().getY() == 4) {
            Material type = event.getBlock().getType();
            if(type == Material.WATER || type == Material.STATIONARY_WATER || type == Material.LAVA || type == Material.STATIONARY_LAVA) {
                Block toBlock = event.getToBlock();
                if(toBlock.getType() == Material.AIR && generatesCobble(type, toBlock)) {
                    event.setCancelled(true);
                } else {
                    event.setCancelled(false);
                }
            }
        }
    }

    public boolean generatesCobble(Material type, Block block) {
        Material mirrorID1 = (type == Material.WATER || type == Material.STATIONARY_WATER ? Material.LAVA : Material.WATER);
        Material mirrorID2 = (type == Material.WATER || type == Material.STATIONARY_WATER ? Material.STATIONARY_LAVA : Material.STATIONARY_WATER);
        for(BlockFace face : faces) {
            Block relative = block.getRelative(face, 1);
            if(relative.getType() == mirrorID1 || relative.getType() == mirrorID2) {
                return true;
            }
        }
        return false;
    }
}
