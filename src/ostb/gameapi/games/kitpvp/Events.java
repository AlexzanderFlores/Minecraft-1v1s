package ostb.gameapi.games.kitpvp;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.game.GameKillEvent;
import ostb.customevents.player.AsyncPostPlayerJoinEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.kitpvp.TeamHandler.KitTeam;
import ostb.gameapi.games.kitpvp.events.TeamSelectEvent;
import ostb.player.CoinsHandler;
import ostb.player.LevelGiver;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	private static boolean paused = false;
	
	public Events() {
		EventUtil.register(this);
	}
	
	public static boolean getPaused() {
		return paused;
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			paused = false;
			for(KitTeam kitTeam : KitTeam.values()) {
				if(kitTeam.getSize() == 0) {
					paused = true;
					break;
				}
			}
			if(!paused) {
				OSTB.getProPlugin().decrementCounter();
			}
			OSTB.getSidebar().update();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setScoreboard(OSTB.getScoreboard());
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData());
		if(coinsHandler != null) {
			coinsHandler.getCoins(player);
			if(coinsHandler.isNewPlayer(player)) {
				coinsHandler.addCoins(player, 25, "&xTo help you get started");
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectator(PlayerSpectatorEvent event) {
		Player player = event.getPlayer();
		if(!Ranks.PREMIUM.hasRank(player)) {
			event.setCancelled(true);
		} else if(KitPVP.getKitPVPTeamHandler().getTeam(player) != null && event.getState() == SpectatorState.ADDED) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			event.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onTeamSelect(TeamSelectEvent event) {
		Player player = event.getPlayer();
		ItemStack helmet = player.getInventory().getHelmet();
		if(helmet == null || helmet.getType() == Material.AIR) {
			player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
		}
		ItemStack chestplate = player.getInventory().getChestplate();
		if(chestplate == null || chestplate.getType() == Material.AIR) {
			player.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
		}
		ItemStack leggings = player.getInventory().getLeggings();
		if(leggings == null || leggings.getType() == Material.AIR) {
			player.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
		}
		ItemStack boots = player.getInventory().getBoots();
		if(boots == null || boots.getType() == Material.AIR) {
			player.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS));
		}
		if(!player.getInventory().contains(Material.STONE_SWORD) && !player.getInventory().contains(Material.IRON_SWORD) && !player.getInventory().contains(Material.DIAMOND_SWORD)) {
			player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new LevelGiver(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getKiller() != null) {
			Player killer = player.getKiller();
			MessageHandler.sendMessage(player, event.getDeathMessage());
			MessageHandler.sendMessage(killer, event.getDeathMessage());
		}
		event.setDeathMessage(null);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		final Player player = event.getPlayer();
		if(!SpectatorHandler.contains(player)) {
			final Inventory inventory = player.getInventory();
			if(event.getPlayer() instanceof Player && inventory.getTitle().startsWith("Chest #")) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = player.getUniqueId();
						DB db = DB.PLAYERS_KITPVP_LOG_OFF_CHEST;
						db.delete("uuid", uuid.toString());
						for(int a = 0; a < inventory.getSize(); ++a) {
							ItemStack item = inventory.getItem(a);
							if(item != null && item.getType() != Material.AIR) {
								String material = item.getType().toString();
								int data = item.getData().getData();
								String enchant = "none";
								if(item.getEnchantments() != null && item.getEnchantments().size() > 0) {
									for(Enchantment enchantment : item.getEnchantments().keySet()) {
										enchant = enchantment.toString() + ":" + item.getEnchantments().get(enchantment);
										break;
									}
								}
								int durability = item.getDurability();
								db.insert("'" + uuid.toString() + "', '" + material + "', '" + data + "', '" + enchant + "', '" + durability + "', '" + a + "'");
							}
						}
					}
				});
			}
		}
	}
}
