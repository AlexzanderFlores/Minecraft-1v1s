package ostb.gameapi.games.hardcoreelimination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.player.TitleDisplayer;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class Events implements Listener {
	private Map<String, Location> spawns = null;
	private List<String> scattered = null;
	private boolean logSpawns = false;
	
	public Events() {
		spawns = new HashMap<String, Location>();
		scattered = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			MiniGame game = OSTB.getMiniGame();
			if(game.getGameState() == GameStates.STARTED) {
				int counter = game.getCounter();
				if(counter <= 0) {
					HandlerList.unregisterAll(this);
					new Battles();
				} else {
					if(game.canDisplay()) {
						String time = CountDownUtil.getCounterAsString(counter);
						for(Player player : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(player, "&cPVP", time).setFadeIn(0).setStay(15).setFadeOut(30).display();
						}
					}
				}
			}
			if(spawns != null && !spawns.isEmpty()) {
				String name = null;
				for(String spawn : spawns.keySet()) {
					if(!scattered.contains(spawn)) {
						name = spawn;
						break;
					}
				}
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					scattered.add(name);
					Location location = spawns.get(name);
					player.teleport(location);
					if(scattered.size() >= ProPlugin.getPlayers().size()) {
						ChunkUnloadEvent.getHandlerList().unregister(this);
						PlayerLeaveEvent.getHandlerList().unregister(this);
						logSpawns = false;
						scattered.clear();
						scattered = null;
						spawns.clear();
						spawns = null;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		spawns.remove(event.getPlayer().getName());
		scattered.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			player.teleport(WorldHandler.getWorld().getSpawnLocation());
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		OSTB.getProPlugin().removeFlags();
		OSTB.getMiniGame().setCounter(60 * 10 + 1);
		String command = "spreadPlayers 0 0 100 500 false ";
		for(Player player : ProPlugin.getPlayers()) {
			player.setNoDamageTicks(20 * 30);
			command += player.getName() + " ";
			new TitleDisplayer(player, "&bScattering...").display();
		}
		logSpawns = true;
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		ItemCreator creator = new ItemCreator(event.getCurrentItem());
		Material type = creator.getType();
		if(type == Material.WOOD_SWORD) {
			creator.setType(Material.STONE_SWORD);
		} else if(type == Material.WOOD_PICKAXE) {
			creator.setType(Material.STONE_PICKAXE);
			creator.addEnchantment(Enchantment.DIG_SPEED, 1);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.WOOD_AXE) {
			creator.setType(Material.STONE_AXE);
			creator.addEnchantment(Enchantment.DIG_SPEED, 1);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.WOOD_SPADE) {
			creator.setType(Material.STONE_SPADE);
			creator.addEnchantment(Enchantment.DIG_SPEED, 1);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.IRON_PICKAXE) {
			creator.addEnchantment(Enchantment.DIG_SPEED, 2);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.IRON_AXE) {
			creator.addEnchantment(Enchantment.DIG_SPEED, 2);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.IRON_SPADE) {
			creator.addEnchantment(Enchantment.DIG_SPEED, 2);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.DIAMOND_PICKAXE) {
			creator.addEnchantment(Enchantment.DIG_SPEED, 3);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.DIAMOND_AXE) {
			creator.addEnchantment(Enchantment.DIG_SPEED, 3);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		} else if(type == Material.DIAMOND_SPADE) {
			creator.addEnchantment(Enchantment.DIG_SPEED, 3);
			creator.addEnchantment(Enchantment.DURABILITY, 3);
		}
		event.setCurrentItem(creator.getItemStack());
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(logSpawns && !spawns.containsKey(player.getName()) && !SpectatorHandler.contains(player)) {
			spawns.put(player.getName(), event.getTo());
			event.getTo().getChunk().load(true);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
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
			if(damager != null) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(GracePeriod.isRunning() && event.getBucket() == Material.LAVA_BUCKET) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = event.getPlayer().getItemInHand();
			if(item != null && item.getType() == Material.FLINT_AND_STEEL) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material type = event.getEntity().getItemStack().getType();
		if(type == Material.SAPLING || type == Material.SPIDER_EYE || type == Material.SULPHUR) {
			event.setCancelled(true);
		}
	}
}
