package ostb.gameapi.modes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import npc.util.EventUtil;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.TeamHandler;
import ostb.player.MessageHandler;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class CTF implements Listener {
	private int captureLimit = 5;
	private Team redTeam = null;
	private Team blueTeam = null;
	private int redCaptures = 0;
	private int blueCaptures = 0;
	private List<BlockState> redFlagLocationRemoved = null;
	private List<BlockState> blueFlagLocationRemoved = null;
	private List<ArmorStand> armorStands = null;
	private Location startingRed = null;
	private Location startingBlue = null;
	private Location redFlag = null;
	private Location blueFlag = null;
	private boolean redFlagPickedUp = false;
	private boolean blueFlagPickedUp = false;
	private int standStillCounter = 0; // Used to count how many seconds both flags have been held
	private ItemStack compass = null;
	
	public CTF(int captureLimit) {
		this.captureLimit = captureLimit;
		redTeam = OSTB.getMiniGame().getTeamHandler().addTeam("red");
		redTeam.setPrefix(ChatColor.RED + "[Red] ");
		redTeam.setAllowFriendlyFire(false);
		blueTeam = OSTB.getMiniGame().getTeamHandler().addTeam("blue");
		blueTeam.setPrefix(ChatColor.AQUA + "[Blue] ");
		blueTeam.setAllowFriendlyFire(false);
		redFlagLocationRemoved = new ArrayList<BlockState>();
		blueFlagLocationRemoved = new ArrayList<BlockState>();
		armorStands = new ArrayList<ArmorStand>();
		EventUtil.register(this);
	}

	public Team getWinning() {
		int red = getCaptures(redTeam);
		int blue = getCaptures(blueTeam);
		return red > blue ? redTeam : blue > red ? blueTeam : null;
	}
	
	public void spawnFlag(Team team) {
		Location location = null;
		DyeColor color = null;
		if(team == redTeam) {
			location = redFlag;
			color = DyeColor.RED;
		} else if(team == blueTeam) {
			location = blueFlag;
			color = DyeColor.BLUE;
		}
		if(location != null && color != null) {
			while(location.getBlock().getRelative(0, -1, 0).getType() == Material.AIR && location.getBlockY() > 0) {
				location = location.add(0, -1, 0);
			}
			for(int a = 0; a < 3; ++a) {
				Block block = location.getBlock().getRelative(0, a, 0);
				if(team == redTeam) {
					redFlagLocationRemoved.add(block.getState());
				} else if(team == blueTeam) {
					blueFlagLocationRemoved.add(block.getState());
				}
				block.setType(Material.FENCE);
			}
			for(int a = 1; a < 3; ++a) {
				Block block = location.getBlock().getRelative(a, 2, 0);
				if(team == redTeam) {
					redFlagLocationRemoved.add(block.getState());
				} else if(team == blueTeam) {
					blueFlagLocationRemoved.add(block.getState());
				}
				block.setType(Material.WOOL);
				block.setData(color.getData());
			}
		}
		/*if(team == redTeam) {
			if(redParticles != null) {
				redParticles.delete();
			}
			redParticles = new CircleUtil(redFlag.clone(), .85, 6) {
				@Override
				public void run(Vector vector, Location location) {
					ParticleEffect.DRIP_LAVA.display(location.add(0, 2.20, 0), 20);
				}
			};
		} else if(team == blueTeam) {
			if(blueParticles != null) {
				blueParticles.delete();
			}
			blueParticles = new CircleUtil(blueFlag.clone(), .85, 6) {
				@Override
				public void run(Vector vector, Location location) {
					ParticleEffect.DRIP_WATER.display(location.add(0, 2.20, 0), 20);
				}
			};
		}*/
	}
	
	private void removeFlag(Team team) {
		removeFlag(team, true);
	}
	
	private void removeFlag(Team team, boolean rollBack) {
		Location location = null;
		if(team == redTeam) {
			location = redFlag;
		} else if(team == blueTeam) {
			location = blueFlag;
		}
		if(location != null) {
			for(int a = 0; a < 3; ++a) {
				Block block = location.getBlock().getRelative(0, a, 0);
				block.setType(Material.AIR);
				block.setData((byte) 0);
			}
			for(int a = 1; a < 3; ++a) {
				Block block = location.getBlock().getRelative(a, 2, 0);
				block.setType(Material.AIR);
				block.setData((byte) 0);
			}
		}
		if(rollBack && team == redTeam && !redFlagLocationRemoved.isEmpty()) {
			for(BlockState block : redFlagLocationRemoved) {
				block.getLocation().getBlock().setType(block.getType());
				block.getLocation().getBlock().setData(block.getData().getData());
			}
			redFlagLocationRemoved.clear();
		} else if(rollBack && team == blueTeam && !blueFlagLocationRemoved.isEmpty()) {
			for(BlockState block : blueFlagLocationRemoved) {
				block.getLocation().getBlock().setType(block.getType());
				block.getLocation().getBlock().setData(block.getData().getData());
			}
			blueFlagLocationRemoved.clear();
		}
	}
	
	private void giveFlag(Player player, byte data) {
		ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
		String color = data == DyeColor.RED.getData() ? "&c" : data == DyeColor.BLUE.getData() ? "&b" : "&6";
		armorStand.setCustomName(StringUtil.color(color + "&l&nFlag"));
		armorStand.setHelmet(new ItemStack(Material.WOOL, 1, data));
		player.setPassenger(armorStand);
		armorStands.add(armorStand);
	}
	
	public int getCaptureLimit() {
		return this.captureLimit;
	}
	
	public int getCaptures(Team team) {
		return team == redTeam ? redCaptures : team == blueTeam ? blueCaptures : 0;
	}
	
	public void addCapture(Team team) {
		if(team == redTeam) {
			++redCaptures;
			if(redCaptures >= captureLimit){
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			}
		} else if(team == blueTeam) {
			++blueCaptures;
			if(blueCaptures >= captureLimit){
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			}
		}
		updateSidebar();
	}
	
	private void updateSidebar() {
		OSTB.getSidebar().removeScore(14);
		OSTB.getSidebar().removeScore(11);
		OSTB.getSidebar().setText("     ", 16);
		OSTB.getSidebar().setText("&eRed Captures", 15);
		OSTB.getSidebar().setText("&c" + redCaptures + "&7 / &c" + captureLimit, 14);
		OSTB.getSidebar().setText("      ", 13);
		OSTB.getSidebar().setText("&eBlue Captures", 12);
		OSTB.getSidebar().setText("&b" + blueCaptures + "&7 / &b" + captureLimit, 11);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			MiniGame miniGame = OSTB.getMiniGame();
			GameStates gameState = miniGame.getGameState();
			if(gameState == GameStates.STARTING && miniGame.getCounter() == 4) {
				World world = miniGame.getMap();
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/pvpbattles/flag.yml");
				if(config.getFile().exists()) {
					for(Team team : OSTB.getMiniGame().getTeamHandler().getTeams()) {
						double x = config.getConfig().getDouble(team.getName().toLowerCase() + ".x");
						double y = config.getConfig().getDouble(team.getName().toLowerCase() + ".y");
						double z = config.getConfig().getDouble(team.getName().toLowerCase() + ".z");
						Location location = new Location(world, x, y, z);
						if(team == redTeam) {
							startingRed = location;
							redFlag = startingRed;
							spawnFlag(team);
						} else if(team == blueTeam) {
							startingBlue = location;
							blueFlag = startingBlue;
							spawnFlag(team);
						}
					}
				} else {
					MessageHandler.alert("&4ERROR: &cNo flags found for this map... closing game");
					miniGame.setGameState(GameStates.ENDING);
				}
			} else if(gameState == GameStates.STARTED && gameState == GameStates.ENDING) {
				TeamHandler teamHandler = miniGame.getTeamHandler();
				for(Player player : ProPlugin.getPlayers()) {
					if(player.getLevel() == 9) {
						Team team = teamHandler.getTeam(player);
						String text = " Flag Tracker " + ChatColor.GRAY + "(Must be holding)";
						String title = team == redTeam ? ChatColor.BLUE + "Blue" + text : team == blueTeam ? ChatColor.RED + "Red" + text : "";
						//player.getInventory().addItem(ItemHandler.getItem(compass, title));
						player.getInventory().addItem(new ItemCreator(compass).setName(title).getItemStack());
					}
					if(player.getItemInHand().getType() == Material.COMPASS) {
						String title = ChatColor.stripColor(player.getItemInHand().getItemMeta().getDisplayName());
						if(title != null) {
							if(title.startsWith("Red")) {
								player.setCompassTarget(redFlag);
							} else if(title.startsWith("Blue")) {
								player.setCompassTarget(blueFlag);
							}
						}
					}
				}
				if(redFlagPickedUp && blueFlagPickedUp) {
					if(standStillCounter == 10) {
						MessageHandler.alert("&e&lBoth teams are holding flags... Stand still started");
					}
					int max = 3;
					if(++standStillCounter >= (60 * max)) {
						redFlagPickedUp = false;
						blueFlagPickedUp = false;
						redFlag = startingRed;
						blueFlag = startingBlue;
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								for(Team team : OSTB.getMiniGame().getTeamHandler().getTeams()) {
									spawnFlag(team);
								}
							}
						});
						MessageHandler.alert("&c&l" + standStillCounter / 60 + "&e&l/&c&l" + max + " &e&lminutes of Stand Still have passed");
						MessageHandler.alert("&c&lFlags returned to their original location");
					} else if(standStillCounter % 60 == 0) {
						MessageHandler.alert("&c&l" + standStillCounter / 60 + "&e&l/&c&l" + max + " &e&lminutes of Stand Still have passed");
						MessageHandler.alert("&e&lOnce all " + max + " minutes have passed flags respawn");
					}
				} else {
					standStillCounter = 0;
				}
			}
			/*if(redParticles != null && redFlag != null) {
				redParticles.setLocation(redFlag.clone());
			}
			if(blueParticles != null && blueFlag != null) {
				blueParticles.setLocation(blueFlag.clone());
			}*/
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		MiniGame miniGame = OSTB.getMiniGame();
		if(miniGame.getGameState() == GameStates.STARTED && !(SpectatorHandler.isEnabled() && SpectatorHandler.contains(event.getPlayer()))) {
			Entity passenger = player.getPassenger();
			if(event.getTo().getBlock().getType() == Material.FENCE) {
				Location to = event.getTo();
				int x = to.getBlockX();
				int y = to.getBlockY();
				int z = to.getBlockZ();
				int xRed = redFlag.getBlockX();
				int yRed = redFlag.getBlockY();
				int zRed = redFlag.getBlockZ();
				int xBlue = blueFlag.getBlockX();
				int yBlue = blueFlag.getBlockY();
				int zBlue = blueFlag.getBlockZ();
				//Bukkit.getLogger().info(x + "," + y + "," + z + " vs " + xRed + "," + yRed + "," + zRed + " vs " + xBlue + "," + yBlue + "," + zBlue);
				TeamHandler teamHandler = miniGame.getTeamHandler();
				Team team = teamHandler.getTeam(player);
				if(x == xRed && y == yRed && z == zRed) {
					if(team == redTeam) {
						int xRedStart = startingRed.getBlockX();
						int yRedStart = startingRed.getBlockY();
						int zRedStart = startingRed.getBlockZ();
						if(x == xRedStart && y == yRedStart && z == zRedStart) {
							if(passenger != null && passenger instanceof ArmorStand) {
								// Score for the red team
								MessageHandler.alert(team.getPrefix() + player.getName() + " &ehas CAPTURED the enemy flag");
								addCapture(team);
								armorStands.remove(passenger);
								passenger.remove();
								// Return blue flag
								new DelayedTask(new Runnable() {
									@Override
									public void run() {
										blueFlag = startingBlue;
										spawnFlag(blueTeam);
									}
								});
							}
						} else {
							// Return red flag
							MessageHandler.alert(team.getPrefix() + player.getName() + " &ehas RETURNED their flag");
							removeFlag(redTeam, false);
							redFlag = startingRed;
							spawnFlag(redTeam);
						}
					} else if(team == blueTeam) {
						// Remove the blue flag
						player.setHealth(player.getMaxHealth());
						MessageHandler.alert(team.getPrefix() + player.getName() + " &ehas PICKED UP the enemy flag");
						redFlagPickedUp = true;
						removeFlag(redTeam);
						giveFlag(player, DyeColor.RED.getData());
					}
				} else if(x == xBlue && y == yBlue && z == zBlue) {
					if(team == blueTeam) {
						int xBlueStart = startingBlue.getBlockX();
						int yBlueStart = startingBlue.getBlockY();
						int zBlueStart = startingBlue.getBlockZ();
						if(x == xBlueStart && y == yBlueStart && z == zBlueStart) {
							if(passenger != null && passenger instanceof ArmorStand) {
								// Score for the blue team
								MessageHandler.alert(team.getPrefix() + player.getName() + " &ehas CAPTURED the enemy flag");
								addCapture(team);
								armorStands.remove(passenger);
								passenger.remove();
								// Return red flag
								new DelayedTask(new Runnable() {
									@Override
									public void run() {
										redFlag = startingRed;
										spawnFlag(redTeam);
									}
								});
							}
						} else {
							// Return blue flag
							MessageHandler.alert(team.getPrefix() + player.getName() + " &ehas RETURNED their flag");
							removeFlag(blueTeam, false);
							blueFlag = startingBlue;
							spawnFlag(blueTeam);
						}
					} else if(team == redTeam) {
						// Remove the red flag
						player.setHealth(player.getMaxHealth());
						MessageHandler.alert(team.getPrefix() + player.getName() + " &ehas PICKED UP the enemy flag");
						blueFlagPickedUp = true;
						removeFlag(blueTeam);
						giveFlag(player, DyeColor.BLUE.getData());
					}
				}
			}
			if(passenger != null && passenger instanceof ArmorStand) {
				ArmorStand armorStand = (ArmorStand) passenger;
				byte data = armorStand.getHelmet().getData().getData();
				if(data == DyeColor.RED.getData()) {
					redFlag = event.getTo();
				} else if(data == DyeColor.BLUE.getData()) {
					blueFlag = event.getTo();
				}
			}
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		updateSidebar();
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null && (redFlagLocationRemoved.contains(event.getClickedBlock().getState()) || blueFlagLocationRemoved.contains(event.getClickedBlock().getState()))) {
			Player player = event.getPlayer();
			MessageHandler.sendMessage(player, "");
			MessageHandler.sendMessage(player, "To pick up the flag walk into the fence");
			MessageHandler.sendMessage(player, "");
			EffectUtil.playSound(player, Sound.CHICKEN_EGG_POP);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(event.getPlayer().getItemInHand().getType() == Material.COMPASS && !(SpectatorHandler.isEnabled() && SpectatorHandler.contains(event.getPlayer()))) {
			String title = ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName());
			String text = " Flag Tracker " + ChatColor.GRAY + "(Must be holding)";
			if(title.startsWith("Blue")) {
				title = ChatColor.RED + "Red" + text;
			} else if(title.startsWith("Red")) {
				title = ChatColor.BLUE + "Blue" + text;
			}
			//event.getPlayer().setItemInHand(ItemHandler.getItem(Material.COMPASS, title));
			event.getPlayer().setItemInHand(new ItemCreator(compass).setName(title).getItemStack());
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		dropFlag(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		dropFlag(event.getPlayer());
	}
	
	public void dropFlag(Player player) {
		Entity passenger = player.getPassenger();
		if(passenger != null && passenger instanceof ArmorStand) {
			ArmorStand armorStand = (ArmorStand) passenger;
			byte data = armorStand.getHelmet().getData().getData();
			armorStands.remove(armorStand);
			armorStand.remove();
			final Team team = data == DyeColor.RED.getData() ? redTeam : data == DyeColor.BLUE.getData() ? blueTeam : null;
			Team enemyTeam = team == redTeam ? blueTeam : team == blueTeam ? redTeam : null;
			if(team != null && enemyTeam != null) {
				MessageHandler.alert(enemyTeam.getPrefix() + player.getName() + " &ehas DROPPED the enemy flag");
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						if(team == redTeam) {
							redFlagPickedUp = false;
						} else if(team == blueTeam){
							blueFlagPickedUp = false;
						}
						spawnFlag(team);
					}
				});
			}
		}
	}
}