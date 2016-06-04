package ostb.gameapi.games.onevsones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerAFKEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.onevsones.kits.OneVsOneKit;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class LobbyHandler implements Listener {
    private static ItemStack kitSelector = null;
    private static List<String> disabledRequests = null;
    private static List<String> watching = null;
    private static Map<String, OneVsOneKit> kitSelecting = null;
    private String invName = null;

    public LobbyHandler() {
        kitSelector = new ItemCreator(Material.ARROW).setName("&aKit Selector").getItemStack();
        disabledRequests = new ArrayList<String>();
        watching = new ArrayList<String>();
        kitSelecting = new HashMap<String, OneVsOneKit>();
        invName = "Select Type";
        EventUtil.register(this);
    }

    public static Location spawn(Player player) {
        return spawn(player, true);
    }

    public static Location spawn(Player player, boolean giveItems) {
        Location location = new Location(player.getWorld(), 0.5, 5, 0.5, -180.0f, 0.0f);
        Random random = new Random();
        int range = 5;
        location.setX(location.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
        location.setY(location.getY() + 2.5d);
        location.setZ(location.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
        player.teleport(location);
        if(!SpectatorHandler.contains(player)) {
            if(Ranks.PREMIUM.hasRank(player)) {
                player.setAllowFlight(true);
            }
            if(giveItems) {
                giveItems(player);
            }
        }
        return location;
    }

    public static void giveItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
        player.updateInventory();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(0, kitSelector);
    }

    public static boolean isInLobby(Player player) {
        return !BattleHandler.isInBattle(player);
    }

    public static Inventory getKitSelectorInventory(Player player, String name, boolean showUsers) {
        Inventory inventory = Bukkit.createInventory(player, 9, name);
        List<OneVsOneKit> kits = OneVsOneKit.getKits();
        for(int a = 0; a < kits.size(); ++a) {
            OneVsOneKit kit = kits.get(a);
            if(showUsers) {
                inventory.setItem(a, new ItemCreator(kit.getIcon().clone()).setAmount(kit.getUsers()).getItemStack());
            } else {
                inventory.setItem(a, new ItemCreator(kit.getIcon().clone()).setAmount(1).getItemStack());
            }
        }
        return inventory;
    }

    public static void openKitSelection(Player player) {
        player.openInventory(getKitSelectorInventory(player, "Kit Selection", true));
        watching.add(player.getName());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(isInLobby(player)) {
                if(event.getCause() == DamageCause.VOID) {
                    spawn(player, false);
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if(ticks == 20) {
            for(String name : watching) {
                Player player = ProPlugin.getPlayer(name);
                if(player != null) {
                    InventoryView view = player.getOpenInventory();
                    List<OneVsOneKit> kits = OneVsOneKit.getKits();
                    for(int a = 0; a < kits.size(); ++a) {
                        OneVsOneKit kit = kits.get(a);
                        ItemCreator creator = new ItemCreator(kit.getIcon().clone());
                        creator.setAmount(kit.getUsers());
                        if(creator.getAmount() % 2 != 0) {
                            creator.addEnchantment(Enchantment.DURABILITY);
                            creator.addLore("&bPlayer(s) waiting in queue");
                            creator.addLore("&bClick to play");
                            if(kit.getName().equals("UHC")) {
                                creator.setData(3);
                            }
                        }
                        view.setItem(a, creator.getItemStack());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
    	Player player = event.getPlayer();
        if(event.getTitle().equals("Kit Selection") && !PrivateBattleHandler.choosingMapType(event.getPlayer())) {
            player.closeInventory();
            OneVsOneKit kit = OneVsOneKit.getKit(event.getItem());
            if(kit == null) {
                MessageHandler.sendMessage(player, "&cAn error occured when selecting kit, please try again");
            } else {
            	String kitName = ChatColor.stripColor(kit.getName().toLowerCase().replace(" ", ""));
        		if(kitName.equals("pyro") || kitName.equals("ender") || kitName.equals("tnt") || kitName.equals("onehitwonder") || kitName.equals("quickshot")) {
        			run(kit, player);
        		} else {
        			Inventory inventory = Bukkit.createInventory(player, 9 * 3, invName);
        			inventory.setItem(11, new ItemCreator(Material.STAINED_GLASS, 14).setName("&aRanked").setLores(new String [] {"", "&aMatches: &e" + RankedMatches.getMatches(player), ""}).getItemStack());
        			inventory.setItem(15, new ItemCreator(Material.STAINED_GLASS, 5).setName("&aUnranked").getItemStack());
        			player.openInventory(inventory);
        			kitSelecting.put(player.getName(), kit);
        		}
            }
            event.setCancelled(true);
        } else if(event.getTitle().equals("Request a Battle")) {
            final String name = event.getItem().getItemMeta().getDisplayName();
            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    Player player = ProPlugin.getPlayer(name);
                    if(player != null) {
                        player.chat("/battle " + name);
                    }
                }
            });
            player.closeInventory();
            event.setCancelled(true);
        } else if(event.getTitle().equals(invName)) {
        	if(event.getSlot() == 11) {
        		int matches = RankedMatches.getMatches(player);
        		if(matches > 0) {
        			RankedMatches.setPlayingRanked(player);
        			player.getInventory().clear();
                    OneVsOneKit kit = kitSelecting.get(player.getName());
                    run(kit, player);
        		} else {
        			MessageHandler.sendMessage(player, "&cYou do not have any ranked matches, get some with &e/vote");
        		}
        	} else if(event.getSlot() == 15) {
                OneVsOneKit kit = kitSelecting.get(player.getName());
                run(kit, player);
        	}
        	player.closeInventory();
        	event.setCancelled(true);
        }
    }
    
    private void run(OneVsOneKit kit, Player player) {
        player.getInventory().clear();
    	kit.give(player);
        QueueHandler.add(player, kit);
        EffectUtil.playSound(player, Sound.NOTE_PLING);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(!Ranks.OWNER.hasRank(player)) {
            Location to = event.getTo();
            if(player.isFlying() && to.getY() >= 23) {
                event.setTo(event.getFrom());
            }
            if(isInLobby(player) && !SpectatorHandler.contains(player)) {
                int x = to.getBlockX();
                int z = to.getBlockZ();
                if(x >= 35 || x <= -35 || z >= 35 || z <= -35) {
                    event.setTo(event.getFrom());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(isInLobby(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if(isInLobby(player)) {
            ItemStack item = player.getItemInHand();
            if(item.equals(kitSelector)) {
            	openKitSelection(player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        spawn(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(spawn(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
    	if(event.getState() == SpectatorState.END) {
    		final Player player = event.getPlayer();
            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    spawn(player);
                }
            });
    	}
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            EffectUtil.playSound(player, Sound.CHEST_OPEN);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        watching.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = (Projectile) event.getEntity();
        if(projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            if(isInLobby(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerAFK(PlayerAFKEvent event) {
        if(Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
            ProPlugin.sendPlayerToServer(event.getPlayer(), "hub");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        Player player = event.getPlayer();
        disabledRequests.remove(player.getName());
        watching.remove(player.getName());
        OneVsOneKit.removePlayerKit(player);
    }
}
