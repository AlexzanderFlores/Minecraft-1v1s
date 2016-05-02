package ostb.gameapi.games.pvpbattles;

import java.util.HashMap;
import java.util.Iterator;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameKillEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpawnPointHandler;
import ostb.gameapi.TeamHandler;
import ostb.player.LevelGiver;
import ostb.player.MessageHandler;
import ostb.player.PlayerMove;
import ostb.player.TitleDisplayer;
import ostb.player.Particles.ParticleTypes;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	private Map<ArmorStand, TNTPrimed> tntCountDowns = null;
	private Location redSpawn = null;
	private Location blueSpawn = null;
	
	public Events() {
		tntCountDowns = new HashMap<ArmorStand, TNTPrimed>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		EventUtil.register(new PlayerMove(false));
		World world = OSTB.getMiniGame().getMap();
		ConfigurationUtil config = new SpawnPointHandler(world, "pvpbattles/spawn").getConfig();
		double x = config.getConfig().getDouble("red.x");
		double y = config.getConfig().getDouble("red.y");
		double z = config.getConfig().getDouble("red.z");
		float yaw = (float) config.getConfig().getDouble("red.yaw");
		float pitch = (float) config.getConfig().getDouble("red.pitch");
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
				new TitleDisplayer(player, "&eYou joined the", team.getPrefix() + " &eTeam").display();
				MessageHandler.sendMessage(player, "You joined the " + team.getPrefix() + " &xteam");
			}
			Location spawn = null;
			if(team.getName().equals("red")) {
				spawn = redSpawn.clone();
			} else if(team.getName().equals("blue")) {
				spawn = blueSpawn.clone();
			}
			int radius = 4;
			x = random.nextBoolean() ? random.nextInt(radius) : random.nextInt(radius) * -1;
			z = random.nextBoolean() ? random.nextInt(radius) : random.nextInt(radius) * -1;
			player.teleport(spawn.add(x, 0, z));
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		PlayerMoveEvent.getHandlerList().unregister(PlayerMove.getInstance());
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new LevelGiver(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_AIR) {
			Player player = event.getPlayer();
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
			TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
			setUpTNT(player, tnt);
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
		} else if(ticks == 20 && OSTB.getMiniGame().getGameState() == GameStates.STARTING) {
			String prefix = "&e";
			int counter = OSTB.getMiniGame().getCounter();
			if(counter == 18) {
				for(Player player : ProPlugin.getPlayers()) {
					new TitleDisplayer(player, "&cPlease watch chat", "&cFor game info").setFadeOut(20 * 4).display();
				}
			} else if(counter == 16) {
				MessageHandler.alertLine("&e");
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
				String path = Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/pvpbattles/";
				ConfigurationUtil anvilUtil = new ConfigurationUtil(path + "anvil.yml");
				ConfigurationUtil enchantUtil = new ConfigurationUtil(path + "enchant.yml");
				Random random = new Random();
				for(String team : new String [] {"red", "blue"}) {
					int x = anvilUtil.getConfig().getInt(team + ".x");
					int y = anvilUtil.getConfig().getInt(team + ".y");
					int z = anvilUtil.getConfig().getInt(team + ".z");
					Block block = world.getBlockAt(x, y, z);
					block.setType(Material.ANVIL);
					EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, block.getLocation());
					ParticleTypes.FIREWORK_SPARK.display(block.getLocation().add(0, 2, 0));
					x = enchantUtil.getConfig().getInt(team + ".x");
					y = enchantUtil.getConfig().getInt(team + ".y");
					z = enchantUtil.getConfig().getInt(team + ".z");
					block = world.getBlockAt(x, y, z);
					block.setType(Material.ENCHANTMENT_TABLE);
					EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, block.getLocation());
					ParticleTypes.FIREWORK_SPARK.display(block.getLocation().add(0, 2, 0));
				}
				EffectUtil.playSound(Sound.LEVEL_UP);
			} else if(counter == 4) {
				if(OSTB.getPlugin() == Plugins.CTF) {
					MessageHandler.alert(prefix + "Capture the Enemy Flag!");
				} else if(OSTB.getPlugin() == Plugins.DOM) {
					MessageHandler.alert(prefix + "Gain control of the Command Posts!");
				}
				MessageHandler.alert("");
				MessageHandler.alertLine("&e");
				EffectUtil.playSound(Sound.LEVEL_UP);
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
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof TNTPrimed) {
			event.setDamage(event.getDamage() / 2);
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
}
