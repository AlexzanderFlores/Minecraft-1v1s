package ostb.gameapi.games.domination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameKillEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerAssistEvent;
import ostb.gameapi.EloHandler;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.mapeffects.MapEffectHandler;
import ostb.gameapi.SpawnPointHandler;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.TeamHandler;
import ostb.player.CoinsHandler;
import ostb.player.LevelGiver;
import ostb.player.MessageHandler;
import ostb.player.Particles.ParticleTypes;
import ostb.player.PlayerMove;
import ostb.player.TitleDisplayer;
import ostb.player.Vanisher;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	private Map<ArmorStand, TNTPrimed> tntCountDowns = null;
	private Map<String, Integer> respawningCounters = null;
	private List<String> respawning = null;
	private List<Block> anvils = null;
	private Location redSpawn = null;
	private Location blueSpawn = null;
	private Location respawnLocation = null;
	private CoinsHandler coinsHandler = null;
	
	public Events() {
		tntCountDowns = new HashMap<ArmorStand, TNTPrimed>();
		respawningCounters = new HashMap<String, Integer>();
		respawning = new ArrayList<String>();
		anvils = new ArrayList<Block>();
		EventUtil.register(this);
	}
	
	private Location spawn(Player player) {
		Random random = new Random();
		Team team = OSTB.getMiniGame().getTeamHandler().getTeam(player);
		Location spawn = null;
		if(team.getName().equals("red")) {
			spawn = redSpawn.clone();
		} else if(team.getName().equals("blue")) {
			spawn = blueSpawn.clone();
		}
		int radius = 4;
		double x = random.nextBoolean() ? random.nextInt(radius) : random.nextInt(radius) * -1;
		double z = random.nextBoolean() ? random.nextInt(radius) : random.nextInt(radius) * -1;
		spawn = spawn.add(x, 0, z);
		player.teleport(spawn);
		player.setNoDamageTicks(20 * 10);
		if(player.getAllowFlight()) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}
		return spawn;
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		EventUtil.register(new PlayerMove(false));
		World world = OSTB.getMiniGame().getMap();
		world.setGameRuleValue("keepInventory", "true");
		new MapEffectHandler(world);
		ConfigurationUtil config = new SpawnPointHandler(world, "domination/respawnloc").getConfig();
		double x = config.getConfig().getDouble("x");
		double y = config.getConfig().getDouble("y");
		double z = config.getConfig().getDouble("z");
		float yaw = (float) config.getConfig().getDouble("yaw");
		float pitch = (float) config.getConfig().getDouble("pitch");
		respawnLocation = new Location(world, x, y, z, yaw, pitch);
		config = new SpawnPointHandler(world, "domination/spawn").getConfig();
		x = config.getConfig().getDouble("red.x");
		y = config.getConfig().getDouble("red.y");
		z = config.getConfig().getDouble("red.z");
		yaw = (float) config.getConfig().getDouble("red.yaw");
		pitch = (float) config.getConfig().getDouble("red.pitch");
		redSpawn = new Location(world, x, y, z, yaw, pitch);
		x = config.getConfig().getDouble("blue.x");
		y = config.getConfig().getDouble("blue.y");
		z = config.getConfig().getDouble("blue.z");
		yaw = (float) config.getConfig().getDouble("blue.yaw");
		pitch = (float) config.getConfig().getDouble("blue.pitch");
		blueSpawn = new Location(world, x, y, z, yaw, pitch);
		TeamHandler teamHandler = OSTB.getMiniGame().getTeamHandler();
		Team redTeam = teamHandler.getTeam("red");
		Team blueTeam = teamHandler.getTeam("blue");
		Random random = new Random();
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().clear();
			Team team = teamHandler.getTeam(player);
			if(team == null) {
				if(redTeam.getSize() < blueTeam.getSize()) {
					redTeam.addPlayer(player);
					team = redTeam;
				} else if(blueTeam.getSize() < redTeam.getSize()) {
					blueTeam.addPlayer(player);
					team = blueTeam;
				} else if(random.nextBoolean()) {
					redTeam.addPlayer(player);
					team = redTeam;
				} else {
					blueTeam.addPlayer(player);
					team = blueTeam;
				}
				teamHandler.setTeam(player, team);
				new TitleDisplayer(player, "&eYou joined the", team.getPrefix() + "&eTeam").display();
				MessageHandler.sendMessage(player, "You joined the " + team.getPrefix() + "&xteam");
			}
			spawn(player);
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		PlayerMoveEvent.getHandlerList().unregister(PlayerMove.getInstance());
		MiniGame miniGame = OSTB.getMiniGame();
		miniGame.setCounter(60 * 20);
		miniGame.setAllowPlayerInteraction(true);
		miniGame.setAllowBowShooting(true);
		miniGame.setAllowEntityDamage(true);
		miniGame.setAllowEntityDamageByEntities(true);
		miniGame.setAllowDroppingItems(true);
		miniGame.setPlayersHaveOneLife(false);
		miniGame.setAllowItemSpawning(true);
		miniGame.setAllowPickingUpItems(true);
		miniGame.setAllowInventoryClicking(true);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				coinsHandler = CoinsHandler.getCoinsHandler(Plugins.DOM.getData());
				for(Player player : ProPlugin.getPlayers()) {
					player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
					coinsHandler.getCoins(player);
					if(coinsHandler.isNewPlayer(player)) {
						int amount = 100;
						coinsHandler.addCoins(player, amount, "&xTo help you get started");
					}
				}
			}
		}, 2);
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item.getType() == Material.WORKBENCH) {
			int amount = item.getAmount();
			if(--amount <= 0) {
				player.setItemInHand(new ItemStack(Material.AIR));
			} else {
				item.setAmount(amount);
				player.setItemInHand(item);
			}
			player.openWorkbench(player.getLocation(), true);
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (byte) 4));
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.AIR));
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getInventory().getType() == InventoryType.ENCHANTING) {
			event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (byte) 4));
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new LevelGiver(event.getPlayer());
		EloHandler.calculateWin(event.getPlayer(), event.getKilled(), 1);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(AutoRespawn.useAutoRespawn(player)) {
			event.setRespawnLocation(spawn(player));
		} else {
			event.setRespawnLocation(respawnLocation);
			player.setAllowFlight(true);
			player.setFlying(true);
			respawning.add(player.getName());
			respawningCounters.put(player.getName(), 5);
			Vanisher.add(player);
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 999999999));
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(respawning.contains(event.getPlayer().getName())) {
			Player player = event.getPlayer();
			if(!player.getAllowFlight()) {
				player.setAllowFlight(true);
			}
			if(!player.isFlying()) {
				player.setFlying(true);
			}
			player.teleport(respawnLocation);
		}
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if(event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if(respawning.contains(player.getName())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(respawning.contains(player.getName())) {
			event.setCancelled(true);
		} else if(event.getAction() == Action.LEFT_CLICK_AIR) {
			ItemStack item = player.getItemInHand();
			if(item != null && item.getType() == Material.TNT) {
				TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
				tnt.setVelocity(player.getLocation().getDirection().multiply(1.5d));
				setUpTNT(player, tnt);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.TNT) {
			Player player = (Player) event.getPlayer();
			if(!respawning.contains(player.getName())) {
				TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
				setUpTNT(player, tnt);
			}
			event.setCancelled(true);
		}
	}
	
	private void setUpTNT(Player player, TNTPrimed tnt) {
		tnt.setFuseTicks(tnt.getFuseTicks() / 2);
		ArmorStand armorStand = (ArmorStand) tnt.getWorld().spawnEntity(tnt.getLocation(), EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
		tntCountDowns.put(armorStand, tnt);
		ItemStack item = player.getItemInHand();
		int amount = item.getAmount() - 1;
		if(amount <= 0) {
			player.setItemInHand(new ItemStack(Material.AIR));
		} else {
			item.setAmount(amount);
			player.setItemInHand(item);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			Iterator<ArmorStand> iterator = tntCountDowns.keySet().iterator();
			while(iterator.hasNext()) {
				ArmorStand armorStand = (ArmorStand) iterator.next();
				if(tntCountDowns.containsKey(armorStand)) {
					TNTPrimed tnt = tntCountDowns.get(armorStand);
					if(tnt == null || tnt.isDead()) {
						iterator.remove();
						armorStand.remove();
					} else {
						double dCounter = ((double) tnt.getFuseTicks() / 20);
						String color = "";
						if(dCounter > 1) {
							color = "&b";
						} else if(dCounter > .5) {
							color = "&e";
						} else if(dCounter > .25) {
							color = "&c";
						} else {
							color = "&4";
						}
						String sCounter = "" + dCounter;
						if(sCounter.length() == 3) {
							sCounter += "0";
						}
						armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', color + sCounter));
						armorStand.teleport(tnt.getLocation());
					}
				} else {
					iterator.remove();
					armorStand.remove();
				}
			}
		} else if(ticks == 20) {
			GameStates gameState = OSTB.getMiniGame().getGameState();
			if(gameState == GameStates.STARTED) {
				Iterator<String> iterator = respawningCounters.keySet().iterator();
				while(iterator.hasNext()) {
					String name = iterator.next();
					Player player = ProPlugin.getPlayer(name);
					int counter = respawningCounters.get(name);
					if(--counter <= 0) {
						iterator.remove();
						respawning.remove(name);
						if(player != null) {
							Vanisher.remove(player);
							player.removePotionEffect(PotionEffectType.INVISIBILITY);
							SpectatorHandler.remove(player);
							spawn(player);
							player.updateInventory();
						}
					} else {
						respawningCounters.put(name, counter);
						if(player != null) {
							new TitleDisplayer(player, "&eRespawning in &b" + counter + "s", "&eAuto Respawn Passes: &b/vote").setFadeIn(0).setStay(15).setFadeOut(60).display();
						}
					}
				}
			} else if(gameState == GameStates.STARTING) {
				String prefix = "&e";
				int counter = OSTB.getMiniGame().getCounter();
				if(counter == 18) {
					for(Player player : ProPlugin.getPlayers()) {
						new TitleDisplayer(player, "&cPlease watch chat", "&cFor game info").setFadeOut(20 * 4).display();
					}
				} else if(counter == 16) {
					MessageHandler.alertLine("&e");
					MessageHandler.alert("");
					MessageHandler.alert(prefix + "Use the Shop & Armory NPCs for items");
					MessageHandler.alert("");
					new Shop(OSTB.getMiniGame().getMap(), redSpawn, blueSpawn);
					new Armory(OSTB.getMiniGame().getMap(), redSpawn, blueSpawn);
					EffectUtil.playSound(Sound.LEVEL_UP);
				} else if(counter == 12) {
					MessageHandler.alert(prefix + "Get levels by killing the enemy players");
					MessageHandler.alert("");
					for(Player player : ProPlugin.getPlayers()) {
						new LevelGiver(player, true);
					}
					EffectUtil.playSound(Sound.LEVEL_UP);
				} else if(counter == 8) {
					MessageHandler.alert(prefix + "Use the Enchanting Table & Anvil at your spawn");
					MessageHandler.alert("");
					World world = OSTB.getMiniGame().getMap();
					String path = Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/domination/";
					ConfigurationUtil anvilUtil = new ConfigurationUtil(path + "anvil.yml");
					ConfigurationUtil enchantUtil = new ConfigurationUtil(path + "enchant.yml");
					Random random = new Random();
					for(String team : new String [] {"red", "blue"}) {
						int x = anvilUtil.getConfig().getInt(team + ".x");
						int y = anvilUtil.getConfig().getInt(team + ".y");
						int z = anvilUtil.getConfig().getInt(team + ".z");
						Block block = world.getBlockAt(x, y, z);
						block.setType(Material.ANVIL);
						anvils.add(block);
						EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, block.getLocation());
						ParticleTypes.FIREWORK_SPARK.display(block.getLocation().add(0, 1, 0));
						x = enchantUtil.getConfig().getInt(team + ".x");
						y = enchantUtil.getConfig().getInt(team + ".y");
						z = enchantUtil.getConfig().getInt(team + ".z");
						block = world.getBlockAt(x, y, z);
						block.setType(Material.ENCHANTMENT_TABLE);
						EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, block.getLocation());
						ParticleTypes.FIREWORK_SPARK.display(block.getLocation().add(0, 1, 0));
					}
					EffectUtil.playSound(Sound.LEVEL_UP);
				} else if(counter == 4) {
					MessageHandler.alert(prefix + "Gain control of the Command Posts!");
					MessageHandler.alert("");
					MessageHandler.alertLine("&e");
					EffectUtil.playSound(Sound.LEVEL_UP);
				}
			}
		} else if(ticks == 20 * 5) {
			for(Block block : anvils) {
				block.setType(Material.ANVIL);
				block.setData((byte) 1);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(OSTB.getMiniGame().getGameState() == GameStates.STARTING) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot talk during the starting stage");
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
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.POTION) {
			final Player player = event.getPlayer();
			player.setFireTicks(0);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					player.setItemInHand(new ItemStack(Material.AIR));
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerAssist(PlayerAssistEvent event) {
		Player player = event.getAttacker();
		coinsHandler.addCoins(player, 5, "&x(Assist)");
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if((player.getNoDamageTicks() / 20) > 0) {
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
					MessageHandler.sendMessage(damager, "&cThat player is under spawn protection for &e" + (player.getNoDamageTicks() / 20) + "s");
				}
			}
		}
	}
}
