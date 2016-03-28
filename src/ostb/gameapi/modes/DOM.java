package ostb.gameapi.modes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.customevents.timed.ThirtySecondTaskEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.Particles.ParticleTypes;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;

@SuppressWarnings("deprecation")
public class DOM extends ModeBase {
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
		
		public void update() {
			List<Player> players = new ArrayList<Player>();
			boolean containsRed = false;
			boolean containsBlue = false;
			for(Player player : ProPlugin.getPlayers()) {
				if(SpectatorHandler.isEnabled() && !SpectatorHandler.contains(player)) {
					double x = player.getLocation().getX();
					double z = player.getLocation().getZ();
					if(Math.sqrt((x - this.x) * (x - this.x) + (z - this.z) * (z - this.z)) <= 10) {
						players.add(player);
						Teams team = getTeam(player);
						if(team == Teams.RED) {
							containsRed = true;
						} else if(team == Teams.BLUE) {
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
				addScore(Teams.RED);
			} else if(progress == -5) {
				addScore(Teams.BLUE);
			}
			String particle = "";
			if(wool.getData() == DyeColor.WHITE.getData()) {
				particle = "fireworksSpark";
			} else if(wool.getData() == DyeColor.RED.getData()) {
				particle = "dripLava";
			} else if(wool.getData() == DyeColor.BLUE.getData()) {
				particle = "dripWater";
			}
			ParticleTypes.valueOf(particle).displaySpiral(new Location(wool.getWorld(), x, y, z), 10, 5);
		}
	}
	
	private List<CommandPost> commandPosts = null;
	private int scoreLimit = 100;
	private int redScore = 0;
	private int blueScore = 0;
	
	public DOM(int scoreLimit) {
		super("Domination", "DOM");
		commandPosts = new ArrayList<CommandPost>();
		this.scoreLimit = scoreLimit;
	}
	
	@Override
	public Teams getWinning() {
		int red = getScore(Teams.RED);
		int blue = getScore(Teams.BLUE);
		return red > blue ? Teams.RED : blue > red ? Teams.BLUE : null;
	}
	
	public int getScore(Teams team) {
		return team == Teams.RED ? redScore : team == Teams.BLUE ? blueScore : 0;
	}
	
	public void addScore(Teams team) {
		if(OSTB.getMiniGame().getGameState() != GameStates.ENDING) {
			if(team == Teams.RED && ++redScore >= scoreLimit) {
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			} else if(team == Teams.BLUE && ++blueScore >= scoreLimit) {
				OSTB.getMiniGame().setGameState(GameStates.ENDING);
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		MiniGame miniGame = OSTB.getMiniGame();
		GameStates gameState = miniGame.getGameState();
		if(gameState == GameStates.STARTING && miniGame.getCounter() == 10) {
			World world = Bukkit.getWorlds().get(1); //TODO: Set this to something
			ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/command_posts.yml");
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
		}
	}
	
	@EventHandler
	public void onThirtySecondTask(ThirtySecondTaskEvent event) {
		MessageHandler.alert("");
		MessageHandler.alert("");
		MessageHandler.alert("&c&lSTAND NEAR THE COMMAND POSTS TO CAPTURE THEM!");
		MessageHandler.alert("");
		MessageHandler.alert("");
	}
}
