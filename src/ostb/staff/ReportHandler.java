package ostb.staff;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ostb.ProPlugin;
import ostb.customevents.timed.TenSecondTaskEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.ChatClickHandler;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.TimeUtil;

public class ReportHandler implements Listener {
	public ReportHandler() {
		new CommandBase("report", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				Player player = ProPlugin.getPlayer(name);
				if(player == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " is not online");
				} else if(Ranks.isStaff(player)) {
					MessageHandler.sendMessage(sender, "&cYou cannot report staff");
				} else if(player.getName().equals(sender.getName())) {
					MessageHandler.sendMessage(sender, "&cYou cannot report yourself");
				} else {
					String reportingUUID = "CONSOLE";
					if(sender instanceof Player) {
						Player reporting = (Player) sender;
						reportingUUID = reporting.getUniqueId().toString();
						if(DB.STAFF_REPORTS.isKeySet("reporting", reportingUUID)) {
							MessageHandler.sendMessage(reporting, "&cYou already have an open report");
							return true;
						}
					}
					UUID uuid = player.getUniqueId();
					String text = "";
					for(int a = 1; a < arguments.length; ++a) {
						text += arguments[a] + " ";
					}
					String time = TimeUtil.getTime();
					DB.STAFF_REPORTS.insert("'" + reportingUUID + "', '" + uuid.toString() + "', '" + text.substring(0, text.length() - 1) + "', '" + time + "'");
					MessageHandler.sendMessage(sender, "Your report has been sent");
				}
				return true;
			}
		}.enableDelay(2);
		new CommandBase("reports") {
			@Override
			public boolean execute(final CommandSender sender, String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						for(String id : DB.STAFF_REPORTS.getAllStrings("id")) {
							String reportingUUID = DB.STAFF_REPORTS.getString("id", id, "reporting");
							String reporting = reportingUUID.equals("CONSOLE") ? reportingUUID : AccountHandler.getName(UUID.fromString(reportingUUID));
							UUID uuid = UUID.fromString(DB.STAFF_REPORTS.getString("id", id, "uuid"));
							String reported = AccountHandler.getName(uuid);
							String text = DB.STAFF_REPORTS.getString("id", id, "text");
							String location = ChatColor.stripColor(DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "location").split("is on ")[1]);
							MessageHandler.sendLine(sender);
							if(sender instanceof Player) {
								Player player = (Player) sender;
								ChatClickHandler.sendMessageToRunCommand(player, "&eClick to join", "Click to join", "/join " + location, "&b" + reporting + " &creported &b" + reported + " &con &b" + location + " ");
							} else {
								MessageHandler.sendMessage(sender, "&b" + reporting + " &creported &b" + reported + " &con &b" + location);
							}
							MessageHandler.sendMessage(sender, "Reason: &c" + text);
							if(sender instanceof Player) {
								Player player = (Player) sender;
								ChatClickHandler.sendMessageToRunCommand(player, "&eClick to close", "Click to close", "/closeReport " + id, "&bDone checking this report? ");
							} else {
								MessageHandler.sendMessage(sender, "To close this report run &e/closeReport " + id);
							}
							MessageHandler.sendLine(sender);
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.TRIAL).enableDelay(2);
		new CommandBase("closeReport", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				try {
					Integer.valueOf(arguments[0]);
					if(DB.STAFF_REPORTS.isKeySet("id", arguments[0])) {
						DB.STAFF_REPORTS.delete("id", arguments[0]);
						String uuid = "CONSOLE";
						if(sender instanceof Player) {
							Player player = (Player) sender;
							uuid = player.getUniqueId().toString();
						}
						String date = TimeUtil.getTime().substring(0, 7);
						String [] keys = new String [] {"uuid", "date_closed"};
						String [] values = new String [] {uuid, date};
						if(DB.STAFF_CLOSED_REPORTS.isKeySet(keys, values)) {
							int amount = DB.STAFF_CLOSED_REPORTS.getInt(keys, values, "amount");
							DB.STAFF_CLOSED_REPORTS.updateInt("amount", amount + 1, keys, values);
						} else {
							DB.STAFF_CLOSED_REPORTS.insert("'" + uuid + "', '" + date + "', '1'");
						}
					} else {
						MessageHandler.sendMessage(sender, "&cThere is no report for that number");
					}
				} catch(NumberFormatException e) {
					return false;
				}
				return true;
			}
		}.setRequiredRank(Ranks.TRIAL).enableDelay(2);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(DB.STAFF_REPORTS.getSize() > 0) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(Ranks.isStaff(player)) {
							ChatClickHandler.sendMessageToRunCommand(player, "&eClick here", "Click to view", "/reports", "&bThere are open reports ");
						}
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(!event.isCancelled() && !Ranks.isStaff(event.getPlayer())) {
			String msg = ChatColor.stripColor(event.getMessage().toLowerCase().replace("!", "").replace(".", "").replace("?", ""));
			String regex = "([h]+[\\W]*[a|4|@|q]+[\\W]*(x|k|ck)+[\\W]*(s)*+(([0|e]+[\\W]*[r]+[\\W]*[s]*)*|([i|1]+[\\W]*[n]+[\\W]*[g]*)))+";
			if(msg.toLowerCase().matches(regex)) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!event.getPlayer().getName().equals(online.getName())) {
						event.getRecipients().remove(online);
					}
				}
				display(event.getPlayer());
			} else {
				for(String word : event.getMessage().split(" ")) {
					if(word.toLowerCase().matches(regex)) {
						display(event.getPlayer());
						event.setCancelled(true);
						break;
					}
				}
			}
		}
	}
	
	private void display(Player player) {
		MessageHandler.sendLine(player);
		MessageHandler.sendMessage(player, "&cPlease report cheaters through &e/report <name> <reason>");
		MessageHandler.sendMessage(player, "&eSaying it in chat may make them turn their cheats off. If they do this we may not be able to collect proof to ban them.");
		MessageHandler.sendLine(player);
	}
}
