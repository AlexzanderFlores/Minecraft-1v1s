package ostb.server.servers.slave;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ostb.OSTB;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.server.CommandBase;
import ostb.server.networking.Instruction;
import ostb.server.networking.Instruction.Inst;

public class PlayerLogger {
	private static Map<UUID, String> locations = null;
	
	public PlayerLogger() {
		locations = new HashMap<UUID, String>();
		new CommandBase("test") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "Logged players:");
				for(UUID uuid : locations.keySet()) {
					Bukkit.getLogger().info(AccountHandler.getName(uuid) + ": " + locations.get(uuid));
				}
				return true;
			}
		};
	}
	
	public static void log(UUID uuid, String name) {
		locations.put(uuid, name);
	}
	
	public static void remove(UUID uuid) {
		locations.remove(uuid);
	}
	
	public static String getSever(UUID uuid) {
		return locations.get(uuid);
	}
	
	public static void findPlayer(String name) {
		findPlayer(AccountHandler.getUUID(name));
	}
	
	public static void findPlayer(UUID uuid) {
		OSTB.getClient().sendMessageToServer(new Instruction(new String [] {Inst.SERVER_GET_PLAYER_LOCATION.toString(), OSTB.getServerName(), uuid.toString()}));
	}
}
