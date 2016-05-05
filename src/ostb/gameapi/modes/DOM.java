package ostb.gameapi.modes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

import npc.util.EventUtil;
import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;

@SuppressWarnings("deprecation")
public class DOM implements Listener {
	public class CommandPost {
		private int x = 0;
		private int y = 0;
		private int z = 0;
		private int progress = 0; // -5 = blue, 5 = red
		private Block wool = null;
		
		public CommandPost(World world, int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.wool = world.getBlockAt(x + 1, y, z);
			wool.setType(Material.WOOL);
			wool.setData(DyeColor.WHITE.getData());
			wool.getRelative(1, 0, 0).setType(Material.WOOL);
			wool.getRelative(1, 0, 0).setData(DyeColor.WHITE.getData());
			for(int a = 0; a < 5; ++a) {
				world.getBlockAt(x, y++, z).setType(Material.FENCE);
			}
			commandPosts.add(this);
		}
		
		public boolean isAt(Player player) {
			double pX = player.getLocation().getX();
			double pZ = player.getLocation().getZ();
			return Math.sqrt((x - pX) * (x - pX) + (z - pZ) * (z - pZ)) <= 10;
		}
		
		public int getProgress() {
			return progress;
		}
		
		public void update() {
			List<Player> players = new ArrayList<Player>();
			boolean containsRed = false;
			boolean containsBlue = false;
			MiniGame miniGame = OSTB.getMiniGame();
			for(Player player : ProPlugin.getPlayers()) {
				if(SpectatorHandler.isEnabled() && !SpectatorHandler.contains(player)) {
					double x = player.getLocation().getX();
					double z = player.getLocation().getZ();
					if(Math.sqrt((x - this.x) * (x - this.x) + (z - this.z) * (z - this.z)) <= 10) {
						players.add(player);
						Team team = miniGame.getTeamHandler().getTeam(player);
						if(team == redTeam) {
							containsRed = true;
						} else if(team == blueTeam) {
							containsBlue = true;
						}
					}
				}
			}
			if(!players.isEmpty()) {
				DyeColor color = DyeColor.WHITE;
				if(containsRed && !containsBlue) {
					if(progress < 5 && ++progress >= 5) {
						progress = 5;
						EffectUtil.launchFirework(wool.getWorld().getBlockAt(x, y + 5, z).getLocation());
					}
					if(progress > 0) {
						color = DyeColor.RED;
					} else if(progress < 0) {
						color = DyeColor.BLUE;
					} else if(progress == 0) {
						color = DyeColor.WHITE;
					}
				} else if(containsBlue && !containsRed) {
					if(progress > -5 && --progress <= -5) {
						progress = -5;
						EffectUtil.launchFirework(wool.getWorld().getBlockAt(x, y + 5, z).getLocation());
					}
					if(progress < 0) {
						color = DyeColor.BLUE;
					} else if(progress > 0) {
						color = DyeColor.RED;
					} else if(progress == 0) {
						color = DyeColor.WHITE;
					}
				}
				if(progress > -5 && progress < 5) {
					wool.setType(Material.AIR);
					wool.getRelative(1, 0, 0).setType(Material.AIR);
					wool = wool.getWorld().getBlockAt(x + 1, y + (progress < 0 ? progress * -1 : progress), z);
					wool.setType(Material.WOOL);
					wool.setData(color.getData());
					wool.getRelative(1, 0, 0).setType(Material.WOOL);
					wool.getRelative(1, 0, 0).setData(color.getData());
					EffectUtil.displayParticles(Material.WOOL, wool.getLocation());
					EffectUtil.displayParticles(Material.WOOL, wool.getLocation().add(1, 0, 0));
				}
				if(color != DyeColor.WHITE && (progress == -5 || progress == 5)) {
					wool.setType(Material.WOOL);
					wool.setData(color.getData());
					wool.getRelative(1, 0, 0).setType(Material.WOOL);
					wool.getRelative(1, 0, 0).setData(color.getData());
				}
				players.clear();
				players = null;
			}
			if(progress == 5) {
				addScore(redTeam);
			} else if(progress == -5) {
				addScore(blueTeam);
			}
			/*String particle = "";
			if(wool.getData() == DyeColor.WHITE.getData()) {
				particle = "fireworksSpark";
			} else if(wool.getData() == DyeColor.RED.getData()) {
				particle = "dripLava";
			} else if(wool.getData() == DyeColor.BLUE.getData()) {
				particle = "dripWater";
			}
			ParticleTypes.valueOf(particle).displaySpiral(new Location(wool.getWorld(), x, y, z), 10, 5);*/
		}
	}
	
	private List<CommandPost> commandPosts = null;
	private int scoreLimit = 100;
	private Team redTeam = null;
	private Team blueTeam = null;
	private int redScore = 0;
	private int blueScore = 0;
	
	public DOM(int scoreLimit) {
		commandPosts = new ArrayList<CommandPost>();
		this.scoreLimit = scoreLimit;
		redTeam = OSTB.getMiniGame().getTeamHandler().addTeam("red");
		redTeam.setPrefix(ChatColor.RED + "[Red] ");
		redTeam.setAllowFriendlyFire(false);
		blueTeam = OSTB.getMiniGame().getTeamHandler().addTeam("blue");
		blueTeam.setPrefix(ChatColor.AQUA + "[Blue] ");
		blueTeam.setAllowFriendlyFire(false);
		EventUtil.register(this);
	}
	
	public Team getWinning() {
		int red = getScore(redTeam);
		int blue = getScore(blueTeam);
		return red > blue ? redTeam : blue > red ? blueTeam : null;
	}
	
	public int getScore(Team team) {
		return team == redTeam ? redScore : team == blueTeam ? blueScore : 0;
	}
	
	public void addScore(Team team) {
		if(OSTB.getMiniGame().getGameState() != GameStates.ENDING) {
			if(team == redTeam && ++redScore >= scoreLimit) {
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			} else if(team == blueTeam && ++blueScore >= scoreLimit) {
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			}
		}
	}
	
	private int oldRedScore = 0;
	private int oldBlueScore = 0;
	private int oldRedCaptured = 0;
	private int oldBlueCaptured = 0;
	
	private void updateSidebar() {
		int redCaptured = 0;
		int blueCaptured = 0;
		for(CommandPost commandPost : commandPosts) {
			if(commandPost.getProgress() == 5) {
				++redCaptured;
			} else if(commandPost.getProgress() == -5) {
				++blueCaptured;
			}
		}
		if(oldRedScore != redScore) {
			oldRedScore = redScore;
			OSTB.getSidebar().removeScore(15);
		}
		if(oldBlueScore != blueScore) {
			oldBlueScore = blueScore;
			OSTB.getSidebar().removeScore(15);
		}
		if(oldRedCaptured != redCaptured) {
			oldRedCaptured = redCaptured;
			OSTB.getSidebar().removeScore(11);
		}
		if(oldBlueCaptured != blueCaptured) {
			oldBlueCaptured = blueCaptured;
			OSTB.getSidebar().removeScore(11);
		}
		OSTB.getSidebar().setText("&eScores", 16);
		OSTB.getSidebar().setText("&c" + redScore + "&7 - &b" + blueScore, 15);
		OSTB.getSidebar().setText("&7Score Limit: " + scoreLimit, 14);
		OSTB.getSidebar().setText("      ", 13);
		OSTB.getSidebar().setText("&ePosts Captured", 12);
		OSTB.getSidebar().setText("&c" + redCaptured + "&7 - &b" + blueCaptured + " ", 11);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		updateSidebar();
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			MiniGame miniGame = OSTB.getMiniGame();
			GameStates gameState = miniGame.getGameState();
			if(gameState == GameStates.STARTING && miniGame.getCounter() == 4) {
				World world = miniGame.getMap();
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/pvpbattles/command_posts.yml");
				if(config.getFile().exists()) {
					for(String key : config.getConfig().getKeys(false)) {
						int x = config.getConfig().getInt(key + ".x");
						int y = config.getConfig().getInt(key + ".y");
						int z = config.getConfig().getInt(key + ".z");
						new CommandPost(world, x, y, z);
					}
				} else {
					MessageHandler.alert("&4ERROR: &cNo command posts found for this map... closing game");
					miniGame.setGameState(GameStates.ENDING);
				}
			} else if(gameState == GameStates.STARTED) {
				for(CommandPost commandPost : commandPosts) {
					commandPost.update();
				}
				updateSidebar();
			}
		}
	}
}
