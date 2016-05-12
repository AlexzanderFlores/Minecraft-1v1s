package ostb.server.servers.worker;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import ostb.ProPlugin;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;

public class Worker extends ProPlugin {
	public Worker() {
		super("Worker");
		new CommandBase("purchase", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
				} else {
					String packageName = "";
					for(int a = 1; a < arguments.length; ++a) {
						packageName += arguments[a] + " ";
					}
					DB.NETWORK_RECENT_SUPPORTERS.insert("'" + uuid.toString() + "', '" + packageName + "'");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
}
