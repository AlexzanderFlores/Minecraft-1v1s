package ostb.server;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.AsyncDelayedTask;

public class AlertHandler {
	public AlertHandler() {
		new CommandBase("alert", -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					MessageHandler.sendUnknownCommand(sender);
				} else {
					String message = "";
					for(String argument : arguments) {
						message += argument + " ";
					}
					alert(message.substring(0, message.length() - 1));
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	public static void alert(final String text) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(String server : DB.NETWORK_PROXIES.getAllStrings("server")) {
					DB.NETWORK_COMMAND_DISPATCHER.insert("'" + server + "', 'alert " + text + "'");
				}
			}
		});
	}
}
