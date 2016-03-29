package ostb.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EventUtil;

public class RestarterHandler extends CountDownUtil implements Listener {
	private boolean running = false;
	
	public RestarterHandler() {
		new CommandBase("restartServer", 1, 2) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				try {
					setCounter(Integer.valueOf(arguments[0]) * 60);
					if(Bukkit.getOnlinePlayers().isEmpty()) {
						ProPlugin.restartServer();
					} else {
						running = true;
					}
				} catch(NumberFormatException e) {
					if(arguments[0].equalsIgnoreCase("stop")) {
						if(running) {
							running = false;
							MessageHandler.alert("&c" + OSTB.getServerName() + "'s restart has been cancelled");
						} else {
							MessageHandler.sendMessage(sender, "&cThere is no running update");
						}
					} else {
						return false;
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("restartIfEmpty", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0) {
					if(Bukkit.getOnlinePlayers().isEmpty()) {
						ProPlugin.restartServer();
					} else {
						MessageHandler.sendMessage(sender, "&cIgnoring restart: Players online");
						if(sender instanceof Player) {
							return false;
						}
					}
				} else if(arguments.length == 1 && arguments[0].equalsIgnoreCase("dispatch")) {
					CommandDispatcher.sendToAll("restartIfEmpty");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("globalHubUpdate", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0) {
					for(int a = 1; a <= ProPlugin.getNumberOfHubs(); ++a) {
						//ProPlugin.dispatchCommandToServer("hub" + a, "restartServer " + a);
						CommandDispatcher.sendToServer("hub" + a, "restartServer " + a);
					}
					return true;
				} else if(arguments.length == 1 && arguments[0].equalsIgnoreCase("stop")) {
					for(int a = 1; a <= ProPlugin.getNumberOfHubs(); ++a) {
						//ProPlugin.dispatchCommandToServer("hub" + a, "restartServer stop");
						CommandDispatcher.sendToServer("hub" + a, "restartServer stop");
					}
					return true;
				}
				return false;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(running) {
				if(getCounter() <= 0) {
					ProPlugin.restartServer();
				} else {
					if(canDisplay()) {
						MessageHandler.alert("&bServer restarting: " + getCounterAsString());
					}
					decrementCounter();
				}
			}
		} else if(ticks == 20 * 5) {
			if(PerformanceHandler.getMemory() >= 70 && !running && OSTB.getMiniGame() == null) {
				setCounter(60);
				running = true;
			}
		} else if(ticks == 20 * 60 * 5) {
			if(Bukkit.getOnlinePlayers().isEmpty() && OSTB.getMiniGame() != null) {
				ProPlugin.restartServer();
			}
		}
	}
}
