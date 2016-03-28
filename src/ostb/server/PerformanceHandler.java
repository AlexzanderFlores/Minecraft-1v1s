package ostb.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.timed.PlayerFiveMinuteConnectedEvent;
import ostb.customevents.player.timed.PlayerFiveSecondConnectedOnceEvent;
import ostb.customevents.player.timed.PlayerOneHourConnectedEvent;
import ostb.customevents.player.timed.PlayerOneMinuteConnectedEvent;
import ostb.customevents.player.timed.PlayerTenSecondConnectedEvent;
import ostb.customevents.timed.FifteenTickTaskEvent;
import ostb.customevents.timed.FiveMinuteTaskEvent;
import ostb.customevents.timed.FiveSecondTaskEvent;
import ostb.customevents.timed.FiveTickTaskEvent;
import ostb.customevents.timed.OneAndAHalfSecondTask;
import ostb.customevents.timed.OneMinuteTaskEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.customevents.timed.OneTickTaskEvent;
import ostb.customevents.timed.SevenSecondTaskEvent;
import ostb.customevents.timed.TenSecondTaskEvent;
import ostb.customevents.timed.TenTickTaskEvent;
import ostb.customevents.timed.ThirtySecondTaskEvent;
import ostb.customevents.timed.ThreeSecondTaskEvent;
import ostb.customevents.timed.ThreeTickTaskEvent;
import ostb.customevents.timed.TwentyFiveSecondTaskEvent;
import ostb.customevents.timed.TwentyMinuteTaskEvent;
import ostb.customevents.timed.TwoMinuteTaskEvent;
import ostb.customevents.timed.TwoSecondTaskEvent;
import ostb.customevents.timed.TwoTickTaskEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;

public class PerformanceHandler implements Listener {
	private int counter = 0;
	private static double ticksPerSecond = 0;
	private long seconds = 0;
	private long currentSecond = 0;
	private int ticks = 0;
	private static int uptimeCounter = 0;
	
	public PerformanceHandler() {
		//TODO: Rename this to /performance, make it for myself only
		new CommandBase("performance") {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					String perm = "bukkit.command.tps";
					PermissionAttachment permission = player.addAttachment(OSTB.getInstance());
					permission.setPermission(perm, true);
					player.chat("/tps");
					permission.unsetPermission(perm);
					permission.remove();
					permission = null;
					MessageHandler.sendMessage(sender, "&bPing: &c" + getPing(player));
				} else {
					Bukkit.dispatchCommand(sender, "tps");
				}
				int averagePing = 0;
				if(Bukkit.getOnlinePlayers().size() > 0) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						averagePing += getPing(player);
					}
					averagePing /= Bukkit.getOnlinePlayers().size();
				}
				//MessageHandler.sendMessage(sender, "&bTicks per second: &c" + ticksPerSecond);
				MessageHandler.sendMessage(sender, "&bAverage ping: &c" + averagePing);
				MessageHandler.sendMessage(sender, "&bConnected clients: &c" + Bukkit.getOnlinePlayers().size());
				MessageHandler.sendMessage(sender, "&bUsed memory: &c" + getMemory(!Ranks.OWNER.hasRank(sender)) + "%");
				MessageHandler.sendMessage(sender, "&bUptime: &c" + getUptimeString());
				MessageHandler.sendMessage(sender, "&eFor more server performance info run /bungeeInfo");
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("ping", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(arguments.length == 0 || !Ranks.isStaff(sender)) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						MessageHandler.sendMessage(player, "Your ping is " + getPing(player));
					} else {
						MessageHandler.sendPlayersOnly(sender);
					}
				} else if(arguments.length == 1) {
					Player player = ProPlugin.getPlayer(arguments[0]);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
					} else {
						MessageHandler.sendMessage(sender, player.getName() + "'s ping is " + getPing(player));
					}
				}
				return true;
			}
		};
		Bukkit.getScheduler().runTaskTimer(OSTB.getInstance(), new Runnable() {
			@Override
			public void run() {
				++counter;
				if(counter % (20 * 60 * 20) == 0) {
					Bukkit.getPluginManager().callEvent(new TwentyMinuteTaskEvent());
				}
				if(counter % (20 * 60 * 5) == 0) {
					Bukkit.getPluginManager().callEvent(new FiveMinuteTaskEvent());
				}
				if(counter % (20 * 60 * 2) == 0) {
					Bukkit.getPluginManager().callEvent(new TwoMinuteTaskEvent());
				}
				if(counter % (20 * 60) == 0) {
					Bukkit.getPluginManager().callEvent(new OneMinuteTaskEvent());
				}
				if(counter % (20 * 30) == 0) {
					Bukkit.getPluginManager().callEvent(new ThirtySecondTaskEvent());
				}
				if(counter % (20 * 25) == 0) {
					Bukkit.getPluginManager().callEvent(new TwentyFiveSecondTaskEvent());
				}
				if(counter % (20 * 10) == 0) {
					Bukkit.getPluginManager().callEvent(new TenSecondTaskEvent());
				}
				if(counter % (20 * 7) == 0) {
					Bukkit.getPluginManager().callEvent(new SevenSecondTaskEvent());
				}
				if(counter % (20 * 5) == 0) {
					Bukkit.getPluginManager().callEvent(new FiveSecondTaskEvent());
				}
				if(counter % (20 * 3) == 0) {
					Bukkit.getPluginManager().callEvent(new ThreeSecondTaskEvent());
				}
				if(counter % (20 * 2) == 0) {
					Bukkit.getPluginManager().callEvent(new TwoSecondTaskEvent());
				}
				if(counter % 30 == 0) {
					Bukkit.getPluginManager().callEvent(new OneAndAHalfSecondTask());
				}
				if(counter % 20 == 0) {
					Bukkit.getPluginManager().callEvent(new OneSecondTaskEvent());
				}
				if(counter % 15 == 0) {
					Bukkit.getPluginManager().callEvent(new FifteenTickTaskEvent());
				}
				if(counter % 10 == 0) {
					Bukkit.getPluginManager().callEvent(new TenTickTaskEvent());
				}
				if(counter % 5 == 0) {
					Bukkit.getPluginManager().callEvent(new FiveTickTaskEvent());
				}
				if(counter % 3 == 0) {
					Bukkit.getPluginManager().callEvent(new ThreeTickTaskEvent());
				}
				if(counter % 2 == 0) {
					Bukkit.getPluginManager().callEvent(new TwoTickTaskEvent());
				}
				Bukkit.getPluginManager().callEvent(new OneTickTaskEvent());
			}
		}, 1, 1);
		EventUtil.register(this);
	}
	
	public static int getPing(Player player) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		return craftPlayer.getHandle().ping / 2;
	}
	
	public static double getTicksPerSecond() {
		return ticksPerSecond;
	}
	
	public static double getMemory() {
		return getMemory(true);
	}
	
	public static double getMemory(boolean round) {
		double total = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		double allocated = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		double value = (total * 100) / allocated;
		return round ? new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue() : value;
	}
	
	public static String getUptimeString() {
		String uptime = null;
		if(uptimeCounter < 60) {
			uptime = uptimeCounter + " second(s)";
		} else if(uptimeCounter < (60 * 60)) {
			int minutes = getAbsoluteValue((uptimeCounter / 60));
			int seconds = getAbsoluteValue((uptimeCounter % 60));
			uptime = minutes + " minute(s) and " + seconds + " second(s)";
		} else {
			int hours = getAbsoluteValue((uptimeCounter / 60 / 60));
			int minutes = getAbsoluteValue((hours * 60) - (uptimeCounter / 60));
			int seconds = getAbsoluteValue((uptimeCounter % 60));
			uptime = hours + " hour(s) and " + minutes + " minute(s) and " + seconds + " second(s)";
		}
		return uptime;
	}
	
	public static int getUptime() {
		return uptimeCounter;
	}
	
	private static int getAbsoluteValue(int value) {
		if(value < 0) {
			value *= -1;
		}
		return value;
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		seconds = (System.currentTimeMillis() / 1000);
		if(currentSecond == seconds) {
			++ticks;
		} else {
			currentSecond = seconds;
			ticksPerSecond = (ticksPerSecond == 0 ? ticks : ((ticksPerSecond + ticks) / 2));
			if(ticksPerSecond < 19.0d) {
				++ticksPerSecond;
			}
			if(ticksPerSecond > 20.0d) {
				ticksPerSecond = 20.0d;
			}
			ticksPerSecond = new BigDecimal(ticksPerSecond).setScale(2, RoundingMode.HALF_UP).doubleValue();
			ticks = 0;
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getTicksLived() % (20 * 60 * 60) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerOneHourConnectedEvent(player));
			}
			if(player.getTicksLived() % (20 * 60 * 5) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerFiveMinuteConnectedEvent(player));
			}
			if(player.getTicksLived() % (20 * 60) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerOneMinuteConnectedEvent(player));
			}
			if(player.getTicksLived() % (20 * 10) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerTenSecondConnectedEvent(player));
			}
			if(player.getTicksLived() == (20 * 5)) {
				Bukkit.getPluginManager().callEvent(new PlayerFiveSecondConnectedOnceEvent(player));
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		++uptimeCounter;
	}
}
