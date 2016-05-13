package ostb.staff.mute;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.staff.Punishment;

public class UnMuteHandler extends Punishment {
	public UnMuteHandler() {
		super("Unmuted");
		// Command syntax: /unmute <player name> <reason>
		new CommandBase("unmute", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// Get the UUID of the target player
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
				} else {
					// See if the target player is muted
					if(DB.STAFF_MUTES.isUUIDSet(uuid)) {
						// Detect if the comment should be activated
						PunishmentExecuteReuslts result = executePunishment(sender, arguments, true);
						if(result.isValid()) {
							// Get the staff uuid for the unmute
							String staff = "CONSOLE";
							String staffUUID = staff;
							if(sender instanceof Player) {
								Player player = (Player) sender;
								staff = player.getName();
								staffUUID = player.getUniqueId().toString();
							}
							// Get the reason for the unmute
							String reason = "";
							for(int a = 1; a < arguments.length; ++a) {
								reason += arguments[a] + " ";
							}
							// Unmute
							MuteHandler.unMute(staffUUID, result.getUUID(), true);
							// Perform any final execution instructions
							//Compile the message string
							String message = getReason(AccountHandler.getRank(sender), arguments, reason, result, true);
							MessageHandler.alert(message);
						}
					} else {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not muted");
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.STAFF);
	}
}
