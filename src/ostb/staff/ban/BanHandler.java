package ostb.staff.ban;

import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import ostb.ProPlugin;
import ostb.customevents.player.PlayerBanEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.TimeUtil;
import ostb.staff.Punishment;

public class BanHandler extends Punishment implements Listener {
	public enum Violations {CHEATING, CHARGING_BACK}
	
	public BanHandler() {
		super("Banned");
		// Command syntax: /ban <name> <reason> <proof>
		new CommandBase("ban", 2, -1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						// See if they are not attaching proof to the command, if they aren't and they aren't an owner don't run the command
						if(arguments.length == 2 && AccountHandler.getRank(sender) != Ranks.OWNER) {
							return;
						}
						// Use a try/catch to view if the given reason is valid
						try {
							Violations reason = Violations.valueOf(arguments[1].toUpperCase());
							// Detect if the command should be activated
							PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
							if(result.isValid()) {
								UUID uuid = result.getUUID();
								// See if the player is already banned
								String [] keys = new String [] {"uuid", "active"};
								String [] values = new String [] {uuid.toString(), "1"};
								if(DB.STAFF_BAN.isKeySet(keys, values)) {
									MessageHandler.sendMessage(sender, "&cThat player is already banned");
									return;
								}
								// Get the staff data
								String staff = "CONSOLE";
								String staffUUID = staff;
								if(sender instanceof Player) {
									Player player = (Player) sender;
									staff = player.getName();
									staffUUID = player.getUniqueId().toString();
								}
								// Compile the message and proof strings
								final String message = getReason(AccountHandler.getRank(sender), arguments, reason.toString(), result);
								String time = TimeUtil.getTime();
								String date = time.substring(0, 7);
								int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
								DB.STAFF_BAN.insert("'" + uuid.toString() + "', 'null', '" + staffUUID + "', 'null', '" + reason + "', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
								int id = DB.STAFF_BAN.getInt(keys, values, "id");
								String proof = (arguments.length == 2 ? "none" : arguments[2]);
								DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
								// Perform any final execution instructions
								MessageHandler.alert(message);
								Bukkit.getPluginManager().callEvent(new PlayerBanEvent(uuid, sender));
								// Ban other accounts attached to the IP
								int counter = 0;
								for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", AccountHandler.getAddress(uuid))) {
									if(!uuidString.equals(uuid.toString())) {
										Player player = Bukkit.getPlayer(UUID.fromString(uuidString));
										if(player != null) {
											player.kickPlayer(ChatColor.RED + "You have been banned due to sharing the IP of " + arguments[0]);
										}
										DB.STAFF_BAN.insert("'" + uuidString + "', '" + uuid.toString() + "', '" + staffUUID + "', 'null', '" + reason + "', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
										keys = new String [] {"uuid", "active"};
										values = new String [] {uuidString, "1"};
										id = DB.STAFF_BAN.getInt(keys, values, "id");
										DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
										++counter;
									}
								}
								if(counter > 0) {
									MessageHandler.alert("&cBanned &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + arguments[0]);
								}
								// Execute the ban if the player is online
								final Player player = ProPlugin.getPlayer(arguments[0]);
								if(player != null) {
									player.kickPlayer(message.replace("&x", "").replace("&c", ""));
								}
							}
						} catch(IllegalArgumentException e) {
							// Display all the valid options
							MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown violation, use one of the following:");
							String reasons = "";
							for(Violations reason : Violations.values()) {
								reasons += "&a" + reason + "&e, ";
							}
							MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.STAFF);
	}
	
	//uuid VARCHAR(40), attached_uuid VARCHAR(40), staff_uuid VARCHAR(40), who_unbanned VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), unban_date VARCHAR(10), unban_time VARCHAR(25), day INT, active INT
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(checkForBanned(event.getPlayer())) {
			event.setKickMessage(ChatColor.RED + "You are banned");
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	public static boolean checkForBanned(Player player) {
		return DB.STAFF_BAN.isKeySet(new String [] {"uuid", "active"}, new String [] {player.getUniqueId().toString(), "1"});
	}
	
	/*private static void display(final UUID uuid, final Player viewer) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ResultSet resultSet = null;
				PreparedStatement statement = null;
				try {
					DB table = DB.STAFF_BAN;
					statement = table.getConnection().prepareStatement("SELECT id, reason, time, staff_uuid, attached_uuid, day FROM " + table.getName() + " WHERE uuid = '" + uuid.toString() + "' AND active = 1");
					resultSet = statement.executeQuery();
					if(!resultSet.wasNull()) {
						MessageHandler.sendLine(viewer);
						MessageHandler.sendMessage(viewer, "&a&lThis account has been &c&lBANNED!");
						while(resultSet.next()) {
							String id = resultSet.getString("id");
							String reason = resultSet.getString("reason").replace("_", " ");
							if(reason != null && !reason.equals("null")) {
								MessageHandler.sendMessage(viewer, "Why? &e" + reason);
							}
							int counter = 0;
							for(String proof : DB.STAFF_BAN_PROOF.getAllStrings("proof", "ban_id", id)) {
								MessageHandler.sendMessage(viewer, "Proof #" + (++counter) + " &e" + proof);
							}
							String time = resultSet.getString("time");
							if(time != null && !time.equals("null")) {
								MessageHandler.sendMessage(viewer, "When? &e" + time);
							}
							String attached_uuid = resultSet.getString("attached_uuid");
							if(attached_uuid.equalsIgnoreCase("null")) {
								ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/banTypes", "&aType of ban? &eDIRECT");
							} else {
								ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/banTypes", "&aType of ban? &eASSOCIATION");
								MessageHandler.sendMessage(viewer, "Associated with? &e" + AccountHandler.getName(UUID.fromString(attached_uuid)));
							}
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/appealInfo", "&aSome ban types are only temporary bans");
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&aTo appeal your ban");
							String staff = resultSet.getString("staff_uuid");
							if(staff.equals("CONSOLE") || reason.equals("XRAY")) {
								int day = resultSet.getInt("day") - Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
								if(day <= 0) {
									ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&aThis player &eMAY &aappeal at this time");
								} else {
									MessageHandler.sendMessage(viewer, "This player &cMAY NOT &aappeal at this time");
									MessageHandler.sendMessage(viewer, "They must wait &e" + day + " &amore day" + (day == 1 ? "" : "s"));
								}
							}
							if(!staff.equals("CONSOLE")) {
								staff = AccountHandler.getName(UUID.fromString(staff));
							}
							if(Ranks.SENIOR_STAFF.hasRank(viewer)) {
								MessageHandler.sendMessage(viewer, "&c&lThis is ONLY shown to Sr. Mods and above");
								MessageHandler.sendMessage(viewer, "Who banned? &e" + staff);
							}
						}
						if(viewer.getUniqueId() == uuid) {
							MessageHandler.sendMessage(viewer, "Is this an unfair punishment? Appeal here: &bhttps://promcgames.com/forum/view_forum/?fid=12");
						}
						MessageHandler.sendLine(viewer);
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(statement, resultSet);
				}
			}
		});
	}*/
}
