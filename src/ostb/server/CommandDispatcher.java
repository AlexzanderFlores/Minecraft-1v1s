package ostb.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.server.DB.Databases;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class CommandDispatcher implements Listener {
	public CommandDispatcher() {
		new CommandBase("test", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String target = arguments[0];
				String command = "";
				for(int a = 1; a < arguments.length; ++a) {
					command += arguments[a] + " ";
				}
				if(target.equalsIgnoreCase("all")) {
					sendToAll(command);
				} else if(target.equalsIgnoreCase("game")) {
					String game = arguments[1];
					command = "";
					for(int a = 2; a < arguments.length; ++a) {
						command += arguments[a] + " ";
					}
					sendToGame(game, command);
				} else {
					sendToServer(target, command);
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static void sendToAll(final String command) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getLogger().info("Sending \"" + command + "\" to all servers");
				List<String> servers = new ArrayList<String>();
				ResultSet resultSet = null;
				try {
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT game_name, server_number FROM server_status").executeQuery();
					while(resultSet.next()) {
						servers.add(resultSet.getString("game_name") + resultSet.getInt("server_number"));
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					if(resultSet != null) {
						try {						
							resultSet.close();
						} catch(SQLException e) {
							e.printStackTrace();
						}
					}
				}
				for(String server : servers) {
					DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + server + "', '" + command + "'");
				}
				servers.clear();
				servers = null;
			}
		});
	}
	
	public static void sendToGame(final String game, final String command) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getLogger().info("Sending \"" + command + "\" to all \"" + game + "\" servers");
				for(String serverNumber : DB.NETWORK_SERVER_STATUS.getAllStrings("server_number", "game_name", game)) {
					DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + game + serverNumber + "', '" + command + "'");
				}
			}
		});
	}
	
	public static void sendToServer(final String server, final String command) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getLogger().info("Sending \"" + command + "\" to \"" + server + "\"");
				DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + server + "', '" + command + "'");
			}
		});
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String server = OSTB.getServerName();
				for(String command : DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.getAllStrings("command", "server", server)) {
					Bukkit.getLogger().info("Command Dispatcher: Running \"" + command + "\" for \"" + server + "\"");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
				}
				DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.delete("server", server);
			}
		});
	}
}
