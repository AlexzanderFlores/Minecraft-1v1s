package ostb.server;

import org.bukkit.command.CommandSender;

import ostb.player.account.AccountHandler.Ranks;

public class AntiCheatListener {
	public AntiCheatListener() {
		new CommandBase("antiCheat", 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String action = arguments[0];
				if(action.equalsIgnoreCase("ban")) {
					
				} else if(action.equalsIgnoreCase("kick")) {
					
				} else if(action.equalsIgnoreCase("NETWORK_DISTANCE_LOGS")) {
					
				} else if(action.equalsIgnoreCase("NETWORK_POWER_BOW_LOGS")) {
					
				} else if(action.equalsIgnoreCase("NETWORK_CPS_LOGS")) {
					
				} else if(action.equalsIgnoreCase("NETWORK_ATTACK_DISTANCE_LOGS")) {
					
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
}
