package ostb.gameapi.competitive;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.game.GameEndingEvent;
import ostb.customevents.game.GameKillEvent;
import ostb.customevents.game.GameLossEvent;
import ostb.customevents.game.GameWinEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.StatsChangeEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.DoubleUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.TimeUtil;

public class StatsHandler implements Listener {
	public static class GameStats {
		private int wins = 0;
		private int losses = 0;
		private int kills = 0;
		private int deaths = 0;
		private int monthlyWins = 0;
		private int monthlyLosses = 0;
		private int monthlyKills = 0;
		private int monthlyDeaths = 0;
		private int weeklyWins = 0;
		private int weeklyLosses = 0;
		private int weeklyKills = 0;
		private int weeklyDeaths = 0;
		
		public GameStats(Player player) {
			String uuid = player.getUniqueId().toString();
			String [] keys = new String [] {"uuid", "date"};
			if(table.isUUIDSet(player.getUniqueId())) {
				wins = table.getInt("uuid", uuid, "wins");
				losses = table.getInt("uuid", uuid, "losses");
				kills = table.getInt("uuid", uuid, "kills");
				deaths = table.getInt("uuid", uuid, "deaths");
			} else {
				table.insert("'" + player.getUniqueId().toString() + "', '0', '0', '0', '0'");
			}
			if(monthly != null) {
				String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
				if(monthly.isKeySet(keys, values)) {
					monthlyWins = monthly.getInt(keys, values, "wins");
					monthlyLosses = monthly.getInt(keys, values, "losses");
					monthlyKills = monthly.getInt(keys, values, "kills");
					monthlyDeaths = monthly.getInt(keys, values, "deaths");
				} else {
					String date = TimeUtil.getTime().substring(0, 7);
					monthly.insert("'" + uuid + "', '" + date + "', '0', '0', '0', '0'");
				}
			}
			if(weekly != null) {
				String week = String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
				String [] values = new String [] {uuid, week};
				if(weekly.isKeySet(keys, values)) {
					weeklyWins = weekly.getInt(keys, values, "wins");
					weeklyLosses = weekly.getInt(keys, values, "losses");
					weeklyKills = weekly.getInt(keys, values, "kills");
					weeklyDeaths = weekly.getInt(keys, values, "deaths");
				} else {
					weekly.insert("'" + uuid + "', '" + week + "', '0', '0', '0', '0'");
				}
			}
			gameStats.put(player.getName(), this);
		}
		
		public int getWins() {
			return this.wins;
		}
		
		public void addWins() {
			++wins;
		}
		
		public int getMonthlyWins() {
			return this.monthlyWins;
		}
		
		public void addMonthlyWins() {
			++monthlyWins;
		}
		
		public int getWeeklyWins() {
			return this.weeklyWins;
		}
		
		public void addWeeklyWins() {
			++weeklyWins;
		}
		
		public int getLosses() {
			return this.losses;
		}
		
		public void addLosses() {
			++losses;
		}
		
		public int getMonthlyLosses() {
			return this.monthlyLosses;
		}
		
		public void addMonthlyLosses() {
			++monthlyLosses;
		}
		
		public int getWeeklyLosses() {
			return this.weeklyLosses;
		}
		
		public void addWeeklyLosses() {
			++weeklyLosses;
		}
		
		public int getKills() {
			return this.kills;
		}
		
		public void addKills() {
			++kills;
		}
		
		public int getMonthlyKills() {
			return this.monthlyKills;
		}
		
		public void addMonthlyKills() {
			++monthlyKills;
		}
		
		public int getWeeklyKills() {
			return this.weeklyKills;
		}
		
		public void addWeeklyKills() {
			++weeklyKills;
		}
		
		public int getDeaths() {
			return this.deaths;
		}
		
		public void addDeaths() {
			++deaths;
		}
		
		public int getMonthlyDeaths() {
			return this.monthlyDeaths;
		}
		
		public void addMonthlyDeaths() {
			++monthlyDeaths;
		}
		
		public int getWeeklyDeaths() {
			return this.weeklyDeaths;
		}
		
		public void addWeeklyDeaths() {
			++weeklyDeaths;
		}
		
		public void removeDeath() {
			--deaths;
			--monthlyDeaths;
			--weeklyDeaths;
		}
	}
	
	private static Map<String, GameStats> gameStats = null;
	private static Map<String, String> combatTagged = null;
	public static enum StatTypes {RANK, WINS, LOSSES, KILLS, DEATHS};
	private static DB table = null;
	private static DB weekly = null;
	private static DB monthly = null;
	private static DB elo = null;
	private static String wins = null;
	private static String losses = null;
	private static String kills = null;
	private static String deaths = null;
	private static boolean enabled = false;
	private static boolean saveOnQuit = true;
	private static boolean viewOnly = false;
	
	public StatsHandler(DB table) {
		this(table, null, null);
	}
	
	public StatsHandler(DB table, DB monthly, DB weekly) {
		StatsHandler.table = table;
		StatsHandler.monthly = monthly;
		StatsHandler.weekly = weekly;
		if(table == null) {
			new CommandBase("stats", -1) {
				@Override
				public boolean execute(CommandSender sender, String[] arguments) {
					MessageHandler.sendMessage(sender, "&cYou can only use this command on a game server");
					return true;
				}
			};
		} else {
			enabled = true;
			wins = "Wins";
			losses = "Losses";
			kills = "Kills";
			deaths = "Deaths";
			new CommandBase("stats", 0, 1) {
				@Override
				public boolean execute(CommandSender sender, String[] arguments) {
					String name = "";
					if(arguments.length == 0) {
						if(sender instanceof Player) {
							Player player = (Player) sender;
							name = player.getName();
						} else {
							MessageHandler.sendUnknownCommand(sender);
							return true;
						}
					} else if(Ranks.PREMIUM_PLUS.hasRank(sender)) {
						name = arguments[0];
					} else {
						MessageHandler.sendMessage(sender, Ranks.PREMIUM_PLUS.getNoPermission());
						return true;
					}
					Player player = ProPlugin.getPlayer(name);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
					} else {
						loadStats(player);
						MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player, false) + "'s Statistics:");
						MessageHandler.sendMessage(sender, "Key: &cLifetime Stats &7/ &bMonthly Stats &7/ &bWeekly Stats");
						if(!gameStats.containsKey(player.getName())) {
							loadStats(player);
						}
						GameStats stats = gameStats.get(player.getName());
						MessageHandler.sendMessage(sender, "&e" + wins + ": &c" + stats.getWins() + " &7/ &b" + stats.getMonthlyWins() + " &7/ &a" + stats.getWeeklyWins());
						MessageHandler.sendMessage(sender, "&e" + losses + ": &c" + stats.getLosses() + " &7/ &b" + stats.getMonthlyLosses() + " &7/ &a" + stats.getWeeklyLosses());
						MessageHandler.sendMessage(sender, "&e" + kills + ": &c" + stats.getKills() + " &7/ &b" + stats.getMonthlyKills() + " &7/ &a" + stats.getWeeklyKills());
						MessageHandler.sendMessage(sender, "&e" + deaths + ": &c" + stats.getDeaths() + " &7/ &b" + stats.getMonthlyDeaths() + " &7/ &a" + stats.getWeeklyDeaths());
						double kills = (double) gameStats.get(player.getName()).getKills();
						double deaths = (double) gameStats.get(player.getName()).getDeaths();
						double monthlyKills = (double) gameStats.get(player.getName()).getMonthlyKills();
						double monthlyDeaths = (double) gameStats.get(player.getName()).getMonthlyDeaths();
						double weeklyKills = (double) gameStats.get(player.getName()).getWeeklyKills();
						double weeklyDeaths = (double) gameStats.get(player.getName()).getWeeklyDeaths();
						double kdr = (kills == 0 || deaths == 0 ? 0 : DoubleUtil.round(kills / deaths, 2));
						double monthlyKdr = (monthlyKills == 0 || monthlyDeaths == 0 ? 0 : DoubleUtil.round(monthlyKills / monthlyDeaths, 2));
						double weeklyKdr = (weeklyKills == 0 || weeklyDeaths == 0 ? 0 : DoubleUtil.round(weeklyKills / weeklyDeaths, 2));
						MessageHandler.sendMessage(sender, "&eKDR: &c" + kdr + " &7/ &b" + monthlyKdr + " &7/ &a" + weeklyKdr);
					}
					return true;
				}
			}.setRequiredRank(Ranks.PREMIUM);
			new StatRanking();
			EventUtil.register(this);
		}
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEloDB(DB elo, int starting) {
		if(StatsHandler.elo == null) {
			new EloHandler(starting);
		}
		StatsHandler.elo = elo;
	}
	
	public static DB getEloDB() {
		return elo;
	}
	
	public static DB getStatsDB() {
		return table;
	}
	
	public static boolean getSaveOnQuit() {
		return saveOnQuit;
	}
	
	public static boolean getViewOnly() {
		return viewOnly;
	}
	
	public static void setSaveOnQuit(boolean saveOnQuit) {
		StatsHandler.saveOnQuit = saveOnQuit;
	}
	
	public static void loadStats(Player player) {
		if(gameStats == null) {
			gameStats = new HashMap<String, GameStats>();
		}
		if(!gameStats.containsKey(player.getName())) {
			new GameStats(ProPlugin.getPlayer(player.getName()));
		}
	}
	
	public static void setViewOnly(boolean viewOnly) {
		StatsHandler.viewOnly = viewOnly;
	}
	
	public static void setWinsString(String wins) {
		StatsHandler.wins = wins;
	}
	
	public static void setLossesString(String losses) {
		StatsHandler.losses = losses;
	}
	
	public static void setKillsString(String kills) {
		StatsHandler.kills = kills;
	}
	
	public static void setDeathsString(String deaths) {
		StatsHandler.deaths = deaths;
	}
	
	public static int getWins(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getWins();
	}
	
	public static int getMonthlyWins(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getMonthlyWins();
	}
	
	public static int getLosses(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getLosses();
	}
	
	public static int getMonthlyLosses(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getMonthlyLosses();
	}
	
	public static int getKills(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getKills();
	}
	
	public static int getMonthlyKills(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getMonthlyKills();
	}
	
	public static int getDeaths(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getDeaths();
	}
	
	public static int getMonthlyDeaths(Player player) {
		loadStats(player);
		return gameStats.get(player.getName()).getMonthlyDeaths();
	}
	
	private static boolean canEditStats(Player player) {
		if(viewOnly) {
			return false;
		}
		StatsChangeEvent event = new StatsChangeEvent(player);
		Bukkit.getPluginManager().callEvent(event);
		return !event.isCancelled();
	}
	
	public static void addWin(Player player) {
		if(!canEditStats(player)) {
			return;
		}
		loadStats(player);
		gameStats.get(player.getName()).addWins();
		gameStats.get(player.getName()).addMonthlyWins();
		gameStats.get(player.getName()).addWeeklyWins();
	}
	
	public static void addLoss(Player player) {
		if(!canEditStats(player)) {
			return;
		}
		loadStats(player);
		gameStats.get(player.getName()).addLosses();
		gameStats.get(player.getName()).addMonthlyLosses();
		gameStats.get(player.getName()).addWeeklyLosses();
	}
	
	public static void addKill(Player player) {
		if(!canEditStats(player)) {
			return;
		}
		loadStats(player);
		gameStats.get(player.getName()).addKills();
		gameStats.get(player.getName()).addMonthlyKills();
		gameStats.get(player.getName()).addWeeklyKills();
	}
	
	public static void addDeath(Player player) {
		if(!canEditStats(player)) {
			return;
		}
		loadStats(player);
		gameStats.get(player.getName()).addDeaths();
		gameStats.get(player.getName()).addMonthlyDeaths();
		gameStats.get(player.getName()).addWeeklyDeaths();
	}
	
	public static void removeDeath(Player player) {
		if(!canEditStats(player)) {
			return;
		}
		loadStats(player);
		gameStats.get(player.getName()).removeDeath();
	}
	
	public static void save(Player player) {
		GameStats stats = gameStats.get(player.getName());
		String uuid = player.getUniqueId().toString();
		table.updateInt("wins", stats.getWins(), "uuid", uuid);
		table.updateInt("losses", stats.getLosses(), "uuid", uuid);
		table.updateInt("kills", stats.getKills(), "uuid", uuid);
		table.updateInt("deaths", stats.getDeaths(), "uuid", uuid);
		if(monthly != null) {
			String [] keys = new String [] {"uuid", "date"};
			String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
			monthly.updateInt("wins", stats.getMonthlyWins(), keys, values);
			monthly.updateInt("losses", stats.getMonthlyLosses(), keys, values);
			monthly.updateInt("kills", stats.getMonthlyKills(), keys, values);
			monthly.updateInt("deaths", stats.getMonthlyDeaths(), keys, values);
		}
		gameStats.remove(player.getName());
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(event.getPlayer() != null) {
			addWin(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onGameLoss(GameLossEvent event) {
		if(event.getPlayer() != null) {
			addLoss(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		addKill(event.getPlayer());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		addDeath(event.getPlayer());
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : Bukkit.getOnlinePlayers()) {
					save(player);
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player && (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile)) {
			Player attacker = null;
			if(event.getDamager() instanceof Player) {
				attacker = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					attacker = (Player) projectile.getShooter();
				}
			}
			if(attacker != null && !SpectatorHandler.contains(attacker)) {
				final Player player = (Player) event.getEntity();
				if(!SpectatorHandler.contains(player)) {
					if(combatTagged == null) {
						combatTagged = new HashMap<String, String>();
					}
					combatTagged.put(player.getName(), attacker.getName());
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							combatTagged.remove(player.getName());
						}
					}, 20 * 5);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectatorStart(PlayerSpectatorEvent event) {
		if(event.getState() == SpectatorState.STARTING && combatTagged != null) {
			combatTagged.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(!SpectatorHandler.contains(player) && combatTagged != null && combatTagged.containsKey(player.getName())) {
			if(OSTB.getMiniGame() != null && OSTB.getMiniGame().getGameState() != GameStates.STARTED) {
				return;
			}
			Player attacker = ProPlugin.getPlayer(combatTagged.get(player.getName()));
			if(attacker != null) {
				addKill(attacker);
				MessageHandler.sendMessage(attacker, "Given 1 kill due to " + player.getName() + " combat logging");
			}
			addLoss(player);
			addDeath(player);
			combatTagged.remove(player.getName());
		}
		if(gameStats != null && gameStats.containsKey(player.getName())) {
			if(saveOnQuit) {
				save(player);
			}
			gameStats.remove(player.getName());
		}
	}
}
