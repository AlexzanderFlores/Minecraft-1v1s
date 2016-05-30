package ostb.gameapi.games.uhc;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerBanEvent;
import ostb.customevents.player.PlayerHeadshotEvent;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.uhc.border.BorderHandler;
import ostb.gameapi.games.uhc.events.WhitelistDisabledEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.AlertHandler;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;
import ostb.server.util.UnicodeUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
    private static boolean moveToBox = false;
    private static boolean firstMinute = false;
    private static boolean moveToCenter = false;
    private static boolean postStart = false;
    private static boolean canBreakBlocks = false;
    private static int alertCounter = 0;
    private ItemStack forceStartItem = null;
    private Random random = null;
    private boolean runOneSecond = true;

    public Events() {
        forceStartItem = new ItemCreator(Material.NAME_TAG).setName("&aForce Start Game").getItemStack();
        random = new Random();
        EventUtil.register(this);
    }

    public static void start() {
        WorldHandler.getWorld().setTime(0);
        new DelayedTask(new Runnable() {
            @Override
            public void run() {
                MessageHandler.alertLine();
                for(Player player : ProPlugin.getPlayers()) {
                    for(PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }
                    if(Ranks.OWNER.hasRank(player)) {
                        String perm = "worldborder.*";
                        PermissionAttachment permission = player.addAttachment(OSTB.getInstance());
                        permission.setPermission(perm, true);
                        player.chat("/wb clear all");
                        permission.unsetPermission(perm);
                        permission.remove();
                        permission = null;
                        if(HostHandler.getMainHost() != null && HostHandler.getMainHost().getName().equals(player.getName())) {
                            SpectatorHandler.add(player);
                        }
                    }
                }
                MessageHandler.alertLine();
                for(Entity entity : WorldHandler.getWorld().getEntities()) {
                    if(entity instanceof Pig || entity instanceof Sheep) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        if(!ScatterHandler.isSaved(livingEntity)) {
                            livingEntity.remove();
                        }
                    }
                }
                ScatterHandler.doneSaving();
                if(OptionsHandler.isRush()) {
                    OSTB.getMiniGame().setCounter(60 * 30);
                    new GracePeriod(60 * 10);
                } else {
                    OSTB.getMiniGame().setCounter(HostedEvent.isEvent() ? 60 * 90 : 60 * 60);
                    new GracePeriod(HostedEvent.isEvent() ? 60 * 20 : 60 * 15);
                }
                firstMinute = true;
                new DelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            player.setFireTicks(0);
                        }
                        postStart = true;
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "heal all");
                        MessageHandler.alert("&cFall/mob damage is now enabled");
                    }
                }, 20 * 15);
                MessageHandler.alert("&cDrowning is now disabled for the next 60 seconds");
                new DelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        firstMinute = false;
                        MessageHandler.alert("&cDrowning damage is now enabled");
                    }
                }, 20 * 60);
                canBreakBlocks = true;
            }
        }, 20 * 5);
    }

    public static boolean getMoveToCenter() {
        return moveToCenter;
    }

    public static void setMoveToCenter(boolean moveToCenter) {
        Events.moveToCenter = moveToCenter;
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20 && runOneSecond) {
            MiniGame miniGame = OSTB.getMiniGame();
            GameStates gameState = miniGame.getGameState();
            if(gameState == GameStates.STARTED) {
                int counter = miniGame.getCounter();
                if(counter <= 0) {
                    runOneSecond = false;
                    WorldHandler.getWorld().setGameRuleValue("doDaylightCycle", "false");
                    WorldHandler.getWorld().setTime(6000);
                    if(HostedEvent.isEvent()) {
                        OSTB.getSidebar().update("&5" + HostedEvent.getEventName());
                    } else {
                        new SurfaceHandler();
                        OSTB.getSidebar().setName("&aGo to 0, 0!");
                    }
                    setMoveToCenter(true);
                    for(Player player : ProPlugin.getPlayers()) {
                        BorderHandler.giveCompass(player);
                    }
                } else {
                    if(GracePeriod.isRunning()) {
                        OSTB.getSidebar().update("&bGrace For " + GracePeriod.getGraceCounterString());
                    } else {
                        OSTB.getSidebar().update("&aIn Game " + miniGame.getCounterAsString());
                        if(!HostedEvent.isEvent()) {
                            if(counter <= 5 || (counter < 60 && counter % 10 == 0)) {
                                MessageHandler.alert("Meetup in &e" + miniGame.getCounterAsString());
                            }
                        }
                    }
                    if(!HostedEvent.isEvent() && (++alertCounter == 10 || alertCounter == 60)) {
                        MessageHandler.alertLine();
                        MessageHandler.alert("");
                        MessageHandler.alert("&4&lIf you have questions please run these:");
                        MessageHandler.alert("");
                        MessageHandler.alert("  " + UnicodeUtil.getUnicode("27A4") + " &b&l/sInfo");
                        MessageHandler.alert("  " + UnicodeUtil.getUnicode("27A4") + " &b&l/info");
                        MessageHandler.alert("  " + UnicodeUtil.getUnicode("27A4") + " &b&l/rules");
                        MessageHandler.alert("");
                        MessageHandler.alertLine();
                    }
                }
            }
        } else if(ticks == 20 * 5) {
            if(moveToBox) {
                World world = WorldHandler.getWorld();
                int counter = 0;
                for(Player player : ProPlugin.getPlayers()) {
                    if(!player.getWorld().getName().equals(world.getName())) {
                        if(++counter >= 10) {
                            break;
                        }
                        player.teleport(new Location(world, 0, 201, 0));
                        if(player.getAllowFlight()) {
                            player.setFlying(false);
                            player.setAllowFlight(false);
                        }
                    }
                }
                if(counter == 0) {
                    moveToBox = false;
                }
            }
        } else if(ticks == 20 * 60) {
            GameStates state = OSTB.getMiniGame().getGameState();
            if((state == GameStates.WAITING || (state == GameStates.STARTING && OSTB.getMiniGame().getCounter() > 30)) && !WhitelistHandler.isWhitelisted()) {
                AlertHandler.alert("&6&l" + TweetHandler.getTeamSize() + " " + TweetHandler.getScenarios() + " UHC OPEN NOW! &c&l/join UHC1" + TweetHandler.getURL());
            }
        }
    }

    @EventHandler
    public void onGameStarting(GameStartingEvent event) {
        World world = WorldHandler.getWorld();
        world.setTime(0);
        for(Entity entity : world.getEntities()) {
            if(!(entity instanceof Player) && entity instanceof Monster) {
                entity.remove();
            }
        }
        Location center = world.getSpawnLocation();
        center.setY(200);
        int radius = 14;
        for(int x = -radius; x <= radius; ++x) {
            for(int z = -radius; z <= radius; ++z) {
                for(int y = 200; y <= 206; ++y) {
                    Block block = world.getBlockAt(x, y, z);
                    boolean inRadius = block.getLocation().toVector().isInSphere(center.toVector(), radius);
                    if((y == 200 || y == 206) && inRadius) {
                        block.setType(Material.STAINED_GLASS);
                    } else if(y > 200 && !inRadius) {
                        try {
                            block.setType(Material.STAINED_GLASS);
                        } catch(Exception e) {
                            Bukkit.getLogger().info(ChatColor.RED + e.getMessage());
                        }
                    }
                }
            }
        }
        moveToBox = true;
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        OSTB.getProPlugin().removeFlags();
        for(Player player : ProPlugin.getPlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999999, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 0));
        }
        new DisconnectHandler();
        new ScatterHandler();
        OSTB.getMiniGame().setResetPlayerUponJoining(false);
        OSTB.getMiniGame().setCounter(60 * 60);
        OSTB.getSidebar().removeScoresBelow(TeamHandler.getMaxTeamSize() + 1);
        new KillLogger();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameStates state = OSTB.getMiniGame().getGameState();
        if(state == GameStates.WAITING || state == GameStates.VOTING) {
            player.teleport(new Location(OSTB.getMiniGame().getLobby(), 0.5, 26, 0.5, -90.0f, -0.0f));
        }
        if(Ranks.OWNER.hasRank(player) && !WhitelistHandler.isWhitelisted() && (state == GameStates.WAITING || state == GameStates.VOTING)) {
            player.getInventory().addItem(forceStartItem);
        }
    }

    @EventHandler
    public void onWhitelistDisable(WhitelistDisabledEvent event) {
        for(Player player : ProPlugin.getPlayers()) {
            player.closeInventory();
            player.getInventory().clear();
            if(Ranks.OWNER.hasRank(player)) {
                player.getInventory().addItem(forceStartItem);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity().getCustomName() != null && event.getEntityType() == EntityType.SHEEP) {
            for(Entity entity : event.getEntity().getNearbyEntities(15, 15, 15)) {
                if(entity instanceof Sheep) {
                    Sheep sheep = (Sheep) entity;
                    sheep.setCustomName(null);
                }
            }
        }
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if(ItemUtil.isItem(player.getItemInHand(), forceStartItem)) {
            if(OSTB.getMiniGame().getGameState() == GameStates.STARTING) {
                MessageHandler.sendMessage(player, "&cThe game is already starting");
            } else {
                OSTB.getMiniGame().setGameState(GameStates.STARTING);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if(postStart) {
            if(event.getEntity() instanceof Player && firstMinute && event.getCause() == DamageCause.DROWNING) {
                Player player = (Player) event.getEntity();
                EffectUtil.playSound(player, Sound.ENDERDRAGON_DEATH);
                MessageHandler.sendMessage(player, "&cGet out of the water, you're drowning!");
                event.setCancelled(true);
            } else if(event.getCause() != DamageCause.ENTITY_ATTACK) {
                event.setCancelled(false);
            }
        } else if(event.getEntity() instanceof Player || event.getCause() == DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(postStart && OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
            if(event.getEntity() instanceof Player) {
                Player damager = null;
                if(event.getDamager() instanceof Player) {
                    damager = (Player) event.getDamager();
                } else if(event.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) event.getDamager();
                    if(projectile.getShooter() instanceof Player) {
                        damager = (Player) projectile.getShooter();
                    }
                }
                if(damager == null) {
                    event.setCancelled(false);
                }
            } else {
                event.setCancelled(false);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        if(!event.getBlock().getWorld().getName().equals("lobby")) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer();
            if(SpectatorHandler.contains(player) && Ranks.isStaff(player)) {
                Player target = (Player) event.getRightClicked();
                player.chat("/invSee " + target.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if(event.getCause() == TeleportCause.NETHER_PORTAL && !OptionsHandler.isNetherEnabled()) {
            MessageHandler.sendMessage(event.getPlayer(), "&cThe Nether is disabled");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerHeadshot(PlayerHeadshotEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBan(PlayerBanEvent event) {
        Player player = Bukkit.getPlayer(event.getUUID());
        if(player != null) {
            player.setHealth(0.0d);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EntityType type = event.getEntityType();
        if(event.getSpawnReason() != SpawnReason.CUSTOM && (type == EntityType.PIG || type == EntityType.SHEEP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(!canBreakBlocks) {
            event.setCancelled(true);
        }
        Block block = event.getBlock();
        if(block.getWorld().getName().equals("lobby")) {
            event.setCancelled(true);
        } else if(!event.isCancelled() && handleAppleSpawning(block)) {
            event.setCancelled(true);
        }
        if(block.getType() == Material.SKULL) {
            Skull skull = (Skull) block.getState();
            ItemStack item = new ItemCreator(Material.SKULL_ITEM, 3).setName(skull.getOwner() + "'s Head").getItemStack();
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(skull.getOwner());
            item.setItemMeta(meta);
            block.getWorld().dropItem(block.getLocation(), item);
            block.setType(Material.AIR);
            event.setCancelled(true);
        }
        /*if(!event.isCancelled()) {
            Material type = block.getType();
			if(type == Material.REDSTONE_ORE || type == Material.GLOWING_REDSTONE_ORE || type == Material.LAPIS_ORE) {
				if(!delayed.contains(event.getPlayer().getName())) {
					final String name = event.getPlayer().getName();
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * 3);
					String blockName = type == Material.LAPIS_ORE ? "Lapis" : "Redstone";
					MessageHandler.sendMessage(event.getPlayer(), "&cCancelling block dropping for " + blockName + " for performance reasons");
				}
				block.setType(Material.AIR);
				ExperienceOrb exp = (ExperienceOrb) block.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
				exp.setExperience(1);
				event.setCancelled(true);
			}
		}*/
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Material type = event.getEntity().getItemStack().getType();
        if(type == Material.EGG || type == Material.SEEDS || type == Material.SULPHUR || type == Material.WOOL) {
            event.setCancelled(true);
        } else if(type == Material.SPIDER_EYE && !OptionsHandler.isNetherEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if(event.getBlockClicked().getLocation().getY() >= 175) {
            MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot empty buckets at this height");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        if(block.getWorld().getName().equals("lobby")) {
            event.setCancelled(true);
        } else if(!event.isCancelled()) {
            handleAppleSpawning(block);
        }
    }

    private boolean handleAppleSpawning(Block block) {
        Material type = block.getType();
        byte data = block.getData();
        if((type == Material.LEAVES && data == 0) || (type == Material.LEAVES && data == 8) || (type == Material.LEAVES_2 && data == 1)) {
            block.setType(Material.AIR);
            if(random.nextInt(100) + 1 <= OptionsHandler.getAppleRates()) {
                block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.APPLE));
                return true;
            }
        }
        return false;
    }
}