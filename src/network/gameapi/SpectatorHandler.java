package network.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.Network;
import network.ProPlugin;
import network.customevents.player.MouseClickEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.customevents.player.PlayerSpectatorEvent;
import network.customevents.player.PlayerSpectatorEvent.SpectatorState;
import network.gameapi.MiniGame.GameStates;
import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.tasks.DelayedTask;
import network.server.util.EventUtil;
import network.server.util.ItemCreator;
import network.server.util.ItemUtil;
import network.staff.StaffMode;
import npc.NPCEntity;

public class SpectatorHandler implements Listener {
	private static Map<String, Map<Integer, ItemStack>> items = null;
	private static List<String> spectators = null;
	private static List<String> beenTold = null;
	private static ItemStack teleporter = null;
	private static ItemStack settings = null;
	private static ItemStack exit = null;
	private static ItemStack nextGame = null;
	private static boolean enabled = false;
	//private static final double range = 10;
	private static String settingsName = null;
	
	public SpectatorHandler() {
		items = new HashMap<String, Map<Integer, ItemStack>>();
		spectators = new ArrayList<String>();
		beenTold = new ArrayList<String>();
		teleporter = new ItemCreator(Material.WATCH).setName("&aTeleport to Player").getItemStack();
		settings = new ItemCreator(Material.REDSTONE_COMPARATOR).setName("&aSpectator Settings").getItemStack();
		nextGame = new ItemCreator(Material.DIAMOND_SWORD).setName("&aJoin Next Game").getItemStack();
		if(Network.getMiniGame() == null) {
			exit = new ItemCreator(Material.WOOD_DOOR).setName("&aExit Spectating").getItemStack();
		} else {
			exit = new ItemCreator(Material.WOOD_DOOR).setName("&aReturn to Hub").getItemStack();
		}
		enabled = true;
		settingsName = "Spectator Settings";
		new CommandBase("toggleSpectator", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						if(SpectatorHandler.contains(player)) {
							SpectatorHandler.remove(player);
						} else {
							SpectatorHandler.add(player);
						}
					} else {
						MessageHandler.sendPlayersOnly(sender);
					}
				} else if(arguments.length == 1) {
					Player player = ProPlugin.getPlayer(arguments[0]);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
					} else if(SpectatorHandler.contains(player)) {
						SpectatorHandler.remove(player);
					} else {
						SpectatorHandler.add(player);
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("spec", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(contains(player)) {
					Player target = ProPlugin.getPlayer(arguments[0]);
					if(target == null) {
						MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
					} else {
						player.teleport(target);
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou must be a spectator to run this command");
				}
				return true;
			}
		}.setRequiredRank(Ranks.VIP);
		/*Bukkit.getScheduler().runTaskTimer(Network.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(spectators != null && !spectators.isEmpty()) {
					for(Player player : getPlayers()) {
						boolean nearBy = false;
						for(Entity entity : player.getNearbyEntities(range, range, range)) {
							if(entity instanceof LivingEntity || entity instanceof Projectile) {
								if(entity instanceof Player) {
									Player nearPlayer = (Player) entity;
									if(contains(nearPlayer)) {
										continue;
									}
								}
								if(!beenTold.contains(player.getName())) {
									beenTold.add(player.getName());
									MessageHandler.sendMessage(player, "");
									MessageHandler.sendMessage(player, "&cNote: &xIf you get too close to a living entity or a projectile you will go into spectating game mode");
									MessageHandler.sendMessage(player, "");
								}
								nearBy = true;
								player.setGameMode(GameMode.CREATIVE);
								break;
							}
						}
						if(!nearBy && player.getGameMode() == GameMode.CREATIVE) {
							Location location = player.getLocation();
							if(location.getBlock().getType() == Material.AIR && location.add(0, 1, 0).getBlock().getType() == Material.AIR) {
								player.setGameMode(GameMode.CREATIVE);
							}
						}
					}
				}
			}
		}, 20, 20);*/
		EventUtil.register(this);
	}
	
	public void createNPC(Location location) {
		createNPC(location, location.getWorld().getSpawnLocation());
	}
	
	public void createNPC(Location location, Location target) {
		new NPCEntity(EntityType.CREEPER, "&e&nSpectate", location, target) {
			@Override
			public void onInteract(Player player) {
				if(contains(player)) {
					SpectatorHandler.remove(player);
				} else {
					add(player);
				}
			}
		};
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void giveUtilItems(Player player) {
		if(Network.getMiniGame().getAutoJoin()) {
			player.getInventory().setItem(7, exit);
			player.getInventory().setItem(8, nextGame);
		} else {
			player.getInventory().setItem(8, exit);
		}
	}
	
	public static void add(Player player) {
		if(!contains(player)) {
			PlayerSpectatorEvent playerSpectateStartEvent = new PlayerSpectatorEvent(player, SpectatorState.STARTING);
			Bukkit.getPluginManager().callEvent(playerSpectateStartEvent);
			if(!playerSpectateStartEvent.isCancelled()) {
				Map<Integer, ItemStack> item = items.get(player.getName());
				if(item == null) {
					item = new HashMap<Integer, ItemStack>();
				}
				for(int a = 0; a <= 39; ++a) {
					item.put(a, player.getInventory().getItem(a));
				}
				items.put(player.getName(), item);
				spectators.add(player.getName());
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				player.getInventory().setItem(0, teleporter);
				player.getInventory().setItem(1, settings);
				if(Network.getMiniGame() == null) {
					player.getInventory().setItem(8, exit);
				} else {
					giveUtilItems(player);
				}
				player.getInventory().setHeldItemSlot(0);
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.hidePlayer(player);
					if(contains(online)) {
						player.hidePlayer(online);
					}
				}
				player.setGameMode(GameMode.CREATIVE);
				player.setAllowFlight(true);
				player.setFlying(true);
				playerSpectateStartEvent = new PlayerSpectatorEvent(player, SpectatorState.ADDED);
				Bukkit.getPluginManager().callEvent(playerSpectateStartEvent);
			}
		}
	}
	
	public static void remove(final Player player) {
		if(contains(player)) {
			PlayerSpectatorEvent spectateEndEvent = new PlayerSpectatorEvent(player, SpectatorState.END);
			Bukkit.getPluginManager().callEvent(spectateEndEvent);
			if(!spectateEndEvent.isCancelled()) {
				if(StaffMode.contains(player)) {
					StaffMode.toggle(player);
				}
				spectators.remove(player.getName());
				ProPlugin.resetPlayer(player);
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.showPlayer(player);
				}
				player.setGameMode(GameMode.SURVIVAL);
				player.setFlying(false);
				player.setAllowFlight(false);
				if(items.containsKey(player.getName())) {
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							Map<Integer, ItemStack> item = items.get(player.getName());
							for(int a : item.keySet()) {
								player.getInventory().setItem(a, item.get(a));
							}
							items.get(player.getName()).clear();
							items.remove(player.getName());
						}
					});
				}
			}
		}
	}
	
	public static boolean contains(Player player) {
		return isEnabled() && spectators.contains(player.getName());
	}
	
	public static int getNumberOf() {
		if(spectators.isEmpty()) {
			return 0;
		}
		int amount = 0;
		for(Player player : getPlayers()) {
			if(!Ranks.isStaff(player)) {
				++amount;
			}
		}
		return amount;
	}
	
	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(contains(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static boolean wouldSpectate() {
		MiniGame miniGame = Network.getMiniGame();
		if(miniGame == null) {
			return false;
		} else {
			GameStates gameState = miniGame.getGameState();
			return (gameState == GameStates.STARTING && !miniGame.getCanJoinWhileStarting()) || gameState == GameStates.STARTED;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(wouldSpectate()) {
			add(event.getPlayer());
		}
		for(Player spectator : getPlayers()) {
			event.getPlayer().hidePlayer(spectator);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
		beenTold.remove(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				if(event.getCause() == DamageCause.VOID) {
					player.teleport(player.getWorld().getSpawnLocation());
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
		if(event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			if(contains(damager)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(contains(player) || (Network.getMiniGame() != null && Network.getMiniGame().getGameState() == GameStates.ENDING)) {
			ItemStack item = player.getItemInHand();
			if(item != null) {
				if(item.getType() == Material.WATCH) {
					Inventory inventory = ItemUtil.getPlayerSelector(player, item.getItemMeta().getDisplayName());
					if(inventory != null) {
						player.openInventory(inventory);
					}
				} else if(item.getType() == Material.WOOD_DOOR) {
					if(Network.getMiniGame() == null) {
						remove(player);
					} else {
						ProPlugin.sendPlayerToServer(player, "hub");
					}
				} else if(item.getType() == Material.DIAMOND_SWORD) {
					AutoJoinHandler.send(player);
				} else if(item.getType() == Material.REDSTONE_COMPARATOR) {
					Inventory inventory = Bukkit.createInventory(player, 9 * 3, settingsName);
					int [] slot = new int [] {10, 12, 14};
					for(int a = 0; a < slot.length; ++a) {
						inventory.setItem(slot[a], new ItemCreator(Material.FEATHER).setAmount(a + 1).setName("&eFly Speed " + (a + 1)).getItemStack());
					}
					inventory.setItem(16, new ItemCreator(Material.TORCH).setName("&eToggle Night Vision").getItemStack());
					player.openInventory(inventory);
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if(contains(player)) {
				ItemStack item = event.getCurrentItem();
				if(item != null && item.getItemMeta() != null) {
					if(event.getInventory().getName().equals(teleporter.getItemMeta().getDisplayName())) {
						Player target = ProPlugin.getPlayer(item.getItemMeta().getDisplayName());
						if(target == null) {
							MessageHandler.sendMessage(player, "&cThat player is no longer playing");
						} else {
							player.teleport(target);
							MessageHandler.sendMessage(player, "&eNote: &aYou can also teleport with &c/spec <player name>");
						}
					} else if(event.getInventory().getTitle().equals(settingsName)) {
						if(item.getType() == Material.FEATHER) {
							if(Ranks.VIP.hasRank(player)) {
								player.setFlySpeed((float) (item.getAmount() * 0.10));
							} else {
								MessageHandler.sendMessage(player, Ranks.VIP.getNoPermission());
							}
						} else if(item.getType() == Material.TORCH) {
							if(Ranks.VIP.hasRank(player)) {
								if(player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
									player.removePotionEffect(PotionEffectType.NIGHT_VISION);
								} else {
									player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 100));
								}
							} else {
								MessageHandler.sendMessage(player, Ranks.VIP.getNoPermission());
							}
						}
						player.closeInventory();
					}
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(contains(player)) {
			if(Network.getMiniGame() != null && Network.getMiniGame().getUseSpectatorChatChannel()) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!contains(online) && !Ranks.isStaff(online)) {
						event.getRecipients().remove(online);
					}
				}
			}
			event.setFormat(ChatColor.GRAY + "[Spec] " + AccountHandler.getPrefix(player, false) + ": " + event.getMessage());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if(contains(event.getPlayer())) {
			event.setAmount(0);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if(event.getEntered() instanceof Player) {
			Player player = (Player) event.getEntered();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleDamage(VehicleDamageEvent event) {
		if(event.getAttacker() instanceof Player) {
			Player player = (Player) event.getAttacker();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if(event.getAttacker() instanceof Player) {
			Player player = (Player) event.getAttacker();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(Network.getMiniGame() != null && Network.getMiniGame().getPlayersHaveOneLife()) {
			ProPlugin.resetPlayer(event.getPlayer());
			add(event.getPlayer());
			Player killer = event.getPlayer().getKiller();
			if(killer == null) {
				event.setRespawnLocation(Network.getMiniGame().getLobby().getSpawnLocation());
			} else {
				event.setRespawnLocation(killer.getLocation());
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				event.setFoodLevel(20);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(player.getPassenger() != null && player.getPassenger() instanceof Player) {
			Player passenger = (Player) player.getPassenger();
			if(contains(passenger)) {
				MessageHandler.sendMessage(passenger, "&cYou have been moved off this player due to them teleporting");
				player.eject();
			}
		}
		if(contains(player) && player.getVehicle() != null && player.getVehicle() instanceof Player) {
			player.leaveVehicle();
		}
	}
}
