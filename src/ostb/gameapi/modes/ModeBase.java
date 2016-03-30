package ostb.gameapi.modes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameEndingEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.SpawnPointHandler;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.util.EventUtil;

public abstract class ModeBase implements Listener {
	private String name = null;
	private String abbreviation = null;
	private int maxTeamSize = -1;
	protected int comeBackPercentage = 80;
	protected Teams comebackEffect = null;
	public static enum Teams {RED, BLUE}
	private List<String> teamRed = null;
	private List<String> teamBlue = null;
	private List<Location> redSpawns = null;
	private List<Location> blueSpawns = null;
	
	public ModeBase(String name, String abbreviation) {
		this.name = name;
		this.abbreviation = abbreviation;
		maxTeamSize = OSTB.getMaxPlayers() / 2;
		teamRed = new ArrayList<String>();
		teamBlue = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getAbbreviation() {
		return this.abbreviation;
	}
	
	public int getMaxTeamSize() {
		return this.maxTeamSize;
	}
	
	public void setMaxTeamSize(int maxTeamSize) {
		this.maxTeamSize = maxTeamSize;
	}
	
	public Teams getComebackEffect() {
		return this.comebackEffect;
	}
	
	public String getPrefix(Teams team) {
		return team == Teams.RED ? ChatColor.RED + "[Red]" : team == Teams.BLUE ? ChatColor.BLUE + "[Blue]" : "";
	}
	
	public void addToTeam(Player player) {
		addToTeam(player, getLeastPopulated());
	}
	
	public void addToTeam(Player player, Teams team) {
		if(isTeamFull(team)) {
			MessageHandler.sendMessage(player, "&cThe " + getPrefix(team) + " &cteam is full");
		} else if(isOnTeam(player, team)) {
			MessageHandler.sendMessage(player, "&cYou are already on the " + getPrefix(team) + " &cteam");
		} else {
			removeFromTeam(player);
			if(team == Teams.RED) {
				teamRed.add(player.getName());
			} else if(team == Teams.BLUE) {
				teamBlue.add(player.getName());
			}
			giveArmor(player);
			colorTab(player);
			MessageHandler.sendMessage(player, "You joined the " + getPrefix(getTeam(player)) + " &ateam");
		}
	}
	
	public void removeFromTeam(Player player) {
		if(isOnTeam(player, Teams.RED)) {
			teamRed.remove(player.getName());
			MessageHandler.sendMessage(player, "You left the " + getPrefix(Teams.RED) + " &ateam");
		}
		if(isOnTeam(player, Teams.BLUE)) {
			teamBlue.remove(player.getName());
			MessageHandler.sendMessage(player, "You left the " + getPrefix(Teams.BLUE) + " &ateam");
		}
	}
	
	public boolean isTeamFull(Teams team) {
		return team == Teams.RED ? teamRed != null && teamRed.size() >= maxTeamSize : team == Teams.BLUE ? teamBlue != null && teamBlue.size() >= maxTeamSize : false;
	}
	
	public int getPopulation(Teams team) {
		return team == Teams.RED ? teamRed == null ? 0 : teamRed.size() : team == Teams.BLUE ? teamBlue == null ? 0 : teamBlue.size() : 0;
	}
	
	public Teams getLeastPopulated() {
		int red = getPopulation(Teams.RED);
		int blue = getPopulation(Teams.BLUE);
		return red < blue ? Teams.RED : blue < red ? Teams.BLUE : Teams.values()[new Random().nextInt(Teams.values().length)];
	}
	
	public Teams getTeam(Player player) {
		return isOnTeam(player, Teams.RED) ? Teams.RED : isOnTeam(player, Teams.BLUE) ? Teams.BLUE : null;
	}
	
	public boolean isOnSameTeam(Player playerOne, Player playerTwo) {
		return getTeam(playerOne) == getTeam(playerTwo);
	}
	
	public boolean isOnTeam(Player player) {
		return getTeam(player) != null;
	}
	
	public boolean isOnTeam(Player player, Teams team) {
		String name = player.getName();
		return team == Teams.RED ? teamRed != null && teamRed.contains(name) : team == Teams.BLUE ? teamBlue != null && teamBlue.contains(name) : false;
	}
	
	public List<Location> getSpawns(World world, Teams team) {
		if(team == Teams.RED) {
			if(redSpawns == null) {
				redSpawns = new SpawnPointHandler(world, "red_spawns").getSpawns();
			}
			return redSpawns;
		} else if(team == Teams.BLUE) {
			if(blueSpawns == null) {
				blueSpawns = new SpawnPointHandler(world, "blue_spawns").getSpawns();
			}
			return blueSpawns;
		} else {
			return null;
		}
	}
	
	public void spawn() {
		for(Teams team : Teams.values()) {
			spawn(team);
		}
	}
	
	public void spawn(Teams team) {
		List<Player> players = getPlayers(team);
		if(players != null && !players.isEmpty()) {
			World world = players.get(0).getWorld();
			List<Location> spawns = getSpawns(world, team);
			if(spawns != null && !spawns.isEmpty()) {
				for(Player player : players) {
					Location spawn = spawns.get(new Random().nextInt(spawns.size()));
					player.teleport(spawn);
					spawns.remove(spawn);
				}
				spawns = null;
			}
			world = null;
			players.clear();
		}
		players = null;
	}
	
	public void spawn(Player player) {
		spawn(player, player.getWorld());
	}
	
	public void spawn(Player player, World world) {
		Teams team = getTeam(player);
		List<Location> spawns = getSpawns(world, team);
		player.teleport(spawns.get(new Random().nextInt(spawns.size())));
		spawns = null;
	}
	
	public void swapSpawns() {
		List<Location> redSpawns = new ArrayList<Location>();
		List<Location> blueSpawns = new ArrayList<Location>();
		for(Location location : this.redSpawns) {
			redSpawns.add(location);
		}
		for(Location location : this.blueSpawns) {
			blueSpawns.add(location);
		}
		this.redSpawns = blueSpawns;
		this.blueSpawns = redSpawns;
	}
	
	public List<String> getNames(Teams team) {
		return team == Teams.RED ? teamRed : team == Teams.BLUE ? teamBlue : null;
	}
	
	public List<Player> getPlayers(Teams team) {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			Teams currentTeam = getTeam(player);
			if(team == currentTeam) {
				players.add(player);
			}
		}
		return players;
	}
	
	public void broadcast(Teams team, String message) {
		for(Player player : getPlayers(team)) {
			MessageHandler.sendMessage(player, message);
		}
	}
	
	public void giveArmor() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			giveArmor(player);
		}
	}
	
	public void giveArmor(Player player) {
		Teams team = getTeam(player);
		int r = team == Teams.RED ? 255 : 0;
		int b = team == Teams.BLUE ? 255 : 0;
		ItemStack [] leatherArmor = new ItemStack [] {
			new ItemStack(Material.LEATHER_BOOTS),
			new ItemStack(Material.LEATHER_LEGGINGS),
			new ItemStack(Material.LEATHER_CHESTPLATE),
			new ItemStack(Material.LEATHER_HELMET)
		};
		for(ItemStack armor : leatherArmor) {
			LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
			meta.setColor(Color.fromRGB(r, 0, b));
			armor.setItemMeta(meta);
		}
		player.getInventory().setArmorContents(leatherArmor);
		player.updateInventory();
	}
	
	public void colorTab() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			colorTab(player);
		}
	}
	
	public void colorTab(Player player) {
		Teams team = getTeam(player);
		String name = player.getName();
		name = team == Teams.RED ? ChatColor.RED + name : team == Teams.BLUE ? ChatColor.BLUE + name : null;
		if(name != null) {
			if(name.length() > 16) {
				name = name.substring(0, 16);
			}
			player.setPlayerListName(name);
		}
	}
	
	public void executeComebackEffects(Teams team) {
		team = team == Teams.RED ? Teams.BLUE : team == Teams.BLUE ? Teams.RED : team;
		comebackEffect = team;
		for(Player player : ProPlugin.getPlayers()) {
			player.sendMessage("");
			player.sendMessage("");
			MessageHandler.sendMessage(player, "&e&lComeback Effects Activated!");
			MessageHandler.sendMessage(player, "&e&lThe " + getPrefix(team) + " &e&lteam now has 2 extra max hearts");
			player.sendMessage(ChatColor.YELLOW + "(This is activated at the enemy team reaching " + comeBackPercentage + "% victory)");
			player.sendMessage("");
			player.sendMessage("");
			if(getTeam(player) == team) {
				player.setMaxHealth(player.getMaxHealth() + 4.0d);
			}
		}
	}
	
	public abstract Teams getWinning();
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		removeFromTeam(event.getPlayer());
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		MessageHandler.alert("&e&lTEAM CHAT HAS BEEN &a&lENABLED");
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		MessageHandler.alert("&e&lTEAM CHAT HAS BEEN &c&lDISABLED");
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(isOnTeam(event.getPlayer(), Teams.RED)) {
			event.setFormat(event.getFormat().replace(event.getPlayer().getName(), ChatColor.RED + event.getPlayer().getName() + ChatColor.RESET));
		} else if(isOnTeam(event.getPlayer(), Teams.BLUE)) {
			event.setFormat(event.getFormat().replace(event.getPlayer().getName(), ChatColor.BLUE + event.getPlayer().getName() + ChatColor.RESET));
		}
		if(OSTB.getMiniGame().getGameState() == GameStates.STARTED) {
			for(Player player : ProPlugin.getPlayers()) {
				if(!isOnSameTeam(event.getPlayer(), player)) {
					event.getRecipients().remove(player);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Teams team = getTeam(player);
		if(team == Teams.RED) {
			event.setDeathMessage(event.getDeathMessage().replace(player.getName(), ChatColor.RED + player.getName() + ChatColor.RESET));
		} else if(team == Teams.BLUE) {
			event.setDeathMessage(event.getDeathMessage().replace(player.getName(), ChatColor.BLUE + player.getName() + ChatColor.RESET));
		}
		Player killer = player.getKiller();
		if(killer != null) {
			Teams killerTeam = getTeam(killer);
			if(killerTeam == Teams.RED) {
				event.setDeathMessage(event.getDeathMessage().replace(killer.getName(), ChatColor.RED + killer.getName() + ChatColor.RESET));
			} else if(killerTeam == Teams.BLUE) {
				event.setDeathMessage(event.getDeathMessage().replace(killer.getName(), ChatColor.BLUE + killer.getName() + ChatColor.RESET));
			}
			if(getPopulation(team) > getPopulation(killerTeam) + 1 && getPopulation(getLeastPopulated()) < 10) {
				addToTeam(player, killerTeam);
				String color = (team == Teams.RED ? ChatColor.RED.toString() : team == Teams.BLUE ? ChatColor.BLUE.toString() : "");
				MessageHandler.alert(AccountHandler.getRank(player).getPrefix() + color + player.getName() + " &ewas moved to the " + getPrefix(killerTeam) + " &eteam");
				colorTab(player);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Player damager = null;
			if(event.getDamager() instanceof Player) {
				damager = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					damager = (Player) projectile.getShooter();
				}
			}
			if(damager != null && isOnSameTeam(player, damager)) {
				event.setCancelled(true);
			}
		}
	}
}
