package ostb.staff.mute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ostb.ProPlugin;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.TimeUtil;
import ostb.staff.Punishment;

public class MuteHandler extends Punishment {
	private Map<ChatViolations, String> muteLengths = null;
	private static Map<String, MuteData> muteData = null;
	private static List<String> checkedForMuted = null;
	
	public static class MuteData {
		private int id = 0;
		private String player = null;
		private String time = null;
		private String expires = null;
		private String staffName = null;
		private String reason = null;
		private String proof = null;
		
		public MuteData(Player player) {
			this.player = player.getName();
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			DB table = DB.STAFF_MUTES;
			try {
				statement = table.getConnection().prepareStatement("SELECT * FROM " + table.getName() + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
				resultSet = statement.executeQuery();
				while(resultSet.next()) {
					id = resultSet.getInt("id") + 1;
					this.time = resultSet.getString("time");
					this.expires = resultSet.getString("expires");
					if(!hasExpired(player)) {
						String uuid = resultSet.getString("staff_uuid");
						if(uuid.equals("CONSOLE")) {
							this.staffName = uuid;
						} else {
							this.staffName = AccountHandler.getName(UUID.fromString(uuid));
						}
						this.reason = resultSet.getString("reason");
						this.proof = resultSet.getString("proof");
					}
				}
				if(muteData == null) {
					muteData = new HashMap<String, MuteData>();
				}
				muteData.put(this.player, this);
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				DB.close(statement, resultSet);
			}
		}
		
		public MuteData(Player player, String time, String expires, String staff, String reason, String proof, int id) {
			this.id = id;
			this.player = player.getName();
			this.time = time;
			this.expires = expires;
			this.staffName = staff;
			this.reason = reason;
			this.proof = proof;
			if(muteData == null) {
				muteData = new HashMap<String, MuteData>();
			}
			muteData.put(this.player, this);
		}
		
		public void display(Player player) {
			if(this.player.equals(player.getName())) {
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, (this.player.equals(player.getName()) ? "You have" : this.player + " has") + " been muted: (ID #" + id + ")");
				MessageHandler.sendMessage(player, "Muted by: " + staffName);
				MessageHandler.sendMessage(player, "Muted for: " + reason.replace("_", " ") + " " + proof.replace("_", " "));
				MessageHandler.sendMessage(player, "Muted at: " + time);
				MessageHandler.sendMessage(player, "Expires on: " + expires);
				MessageHandler.sendMessage(player, "Appeal your mute: " + appeal);
				if(reason.equals(Punishment.ChatViolations.SERVER_ADVERTISEMENT.toString())) {
					MessageHandler.sendMessage(player, "&bThis type of mute can be undone through an unmute pass:");
					MessageHandler.sendMessage(player, "&6http://store.promcgames.com/category/359455");
				}
				MessageHandler.sendLine(player);
			}
		}
		
		public boolean hasExpired(Player player) {
			if(!expires.equals("NEVER")) {
				long timeCheck = Long.valueOf(TimeUtil.getTime().split(" ")[0].replace("/", "").replace(":", ""));
				long expiresCheck = Long.valueOf(expires.split(" ")[0].replace("/", "").replace(":", ""));
				if(expiresCheck <= timeCheck) {
					unMute(player, true);
					return true;
				}
			}
			return false;
		}
	}
	
	public MuteHandler() {
		super("MUTED");
		// Command syntax: /mute <player name> <reason> <proof>
		new CommandBase("mute", 3) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					public void run() {
						// Use a try/catch to view if the given reason is valid
						try {
							ChatViolations reason = ChatViolations.valueOf(arguments[1].toUpperCase());
							// Detect if the command should be activated
							PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
							if(result.isValid()) {
								// See if the player is already muted
								if(DB.STAFF_MUTES.isUUIDSet(result.getUUID())) {
									MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already " + getName());
									return;
								}
								// Get the staff data
								String staffUUID = "CONSOLE";
								Ranks rank = Ranks.OWNER;
								if(sender instanceof Player) {
									Player player = (Player) sender;
									staffUUID = player.getUniqueId().toString();
									rank = AccountHandler.getRank(player);
								}
								// Compile the message and proof strings
								String message = getReason( rank, arguments, reason.toString(), result);
								// Update the database
								UUID uuid = result.getUUID();
								String time = TimeUtil.getTime();
								// Set times for temporary mutes, note that being muted twice for any reason(s) will result in a lifetime mute
								// Key: DAYS/HOURS
								if(muteLengths == null) {
									muteLengths = new HashMap<ChatViolations, String>();
									muteLengths.put(ChatViolations.DISRESPECT, "0/1");
									muteLengths.put(ChatViolations.RACISM, "0/1");
									muteLengths.put(ChatViolations.DEATH_COMMENTS, "0/1");
									muteLengths.put(ChatViolations.INAPPROPRIATE_COMMENTS, "0/1");
									muteLengths.put(ChatViolations.SPAMMING, "0/1");
									muteLengths.put(ChatViolations.SOCIAL_MEDIA_ADVERTISEMENT, "0/1");
									muteLengths.put(ChatViolations.SERVER_ADVERTISEMENT, "15/0");
									muteLengths.put(ChatViolations.DDOS_THREATS, "1/0");
								}
								String expires = "NEVER";
								if(!muteLengths.get(reason).equals("NEVER")) {
									int days = Integer.valueOf(muteLengths.get(reason).split("/")[0]);
									int hours = Integer.valueOf(muteLengths.get(reason).split("/")[1]);
									int previousMutes = DB.STAFF_UNMUTES.getSize(new String [] {"uuid", "reason"}, new String [] {uuid.toString(), "Mute expired"});
									if(previousMutes > 0) {
										days *= previousMutes;
										hours *= previousMutes;
									}
									expires = TimeUtil.addDate(days, hours);
								}
								String address = null;
								Player player = ProPlugin.getPlayer(arguments[0]);
								if(player == null) {
									address = AccountHandler.getAddress(uuid);
								} else {
									address = player.getAddress().getAddress().getHostAddress();
									uuid = player.getUniqueId();
								}
								DB.STAFF_MUTES.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + address + "', '" + reason.toString() + "', '" + arguments[2] + "', '" + time.substring(0, 7) + "', '" + time + "', '" + expires + "'");
								// Perform any final execution instructions
								MessageHandler.alert(message);
								// Execute the mute if the player is online
								if(player != null) {
									new MuteData(player, time, expires, sender.getName(), reason.toString(), arguments[2], DB.STAFF_MUTES.getInt("uuid", result.getUUID().toString(), "id"));
									muteData.get(player.getName()).display(player);
								}
							}
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
							// Display all the valid options
							MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown chat violatoin, use one of the following:");
							String reasons = "";
							for(ChatViolations reason : ChatViolations.values()) {
								reasons += "&a" + reason + "&e, ";
							}
							MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	public static boolean checkMute(Player player) {
		if(Ranks.isStaff(player)) {
			return false;
		} else {
			if(checkedForMuted == null) {
				checkedForMuted = new ArrayList<String>();
			}
			if(!checkedForMuted.contains(player.getName())) {
				checkedForMuted.add(player.getName());
				String address = player.getAddress().getAddress().getHostAddress();
				if(DB.STAFF_MUTES.isUUIDSet(player.getUniqueId())) {
					new MuteData(player);
					DB.STAFF_MUTES.updateString("address", address, "uuid", player.getUniqueId().toString());
				}
			}
			return muteData != null && muteData.containsKey(player.getName());
		}
	}
	
	public static void remove(Player player) {
		if(checkedForMuted != null && checkedForMuted.contains(player.getName())) {
			checkedForMuted.remove(player.getName());
		}
		if(muteData != null && muteData.containsKey(player.getName())) {
			muteData.remove(player.getName());
		}
	}
	
	public static void unMute(Player player, boolean editDatabase) {
		MessageHandler.sendLine(player);
		player.sendMessage(ChatColor.YELLOW + "Your mute has expired! Be sure to follow all rules please!");
		player.sendMessage(ChatColor.AQUA + "/rules");
		MessageHandler.sendLine(player);
		remove(player);
		String time = TimeUtil.getTime();
		if(editDatabase) {
			DB.STAFF_UNMUTES.insert("'" + player.getUniqueId().toString() + "', 'CONSOLE', 'Mute expired', '" + time.substring(0, 7) + "', '" + time + "'");
			DB.STAFF_MUTES.deleteUUID(player.getUniqueId());
		}
	}
	
	public static void display(Player player) {
		if(checkMute(player)) {
			muteData.get(player.getName()).display(player);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(checkMute(player) && muteData != null && !muteData.get(player.getName()).hasExpired(player)) {
			muteData.get(player.getName()).display(player);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
