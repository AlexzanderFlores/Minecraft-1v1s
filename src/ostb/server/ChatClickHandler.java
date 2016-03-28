package ostb.server;

import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class ChatClickHandler {
	public static void sendMessageToRunCommand(Player player, String text, String hover, String command) {
		sendMessageToRunCommand(player, text, hover, command, "");
	}
	
	public static void sendMessageToRunCommand(Player player, String text, String hover, String command, String prefix) {
		JSONObject message = new JSONObject();
		message.put("text", ChatColor.translateAlternateColorCodes('&', prefix));
        JSONArray extra = new JSONArray();
        JSONObject chatExtra = new JSONObject();
        chatExtra.put("text", ChatColor.translateAlternateColorCodes('&', text));
        JSONObject hoverEvent = new JSONObject();
        hoverEvent.put("action", "show_text");
        hoverEvent.put("value", hover);
        chatExtra.put("hoverEvent", hoverEvent);
        JSONObject clickEvent = new JSONObject();
        clickEvent.put("action", "run_command");
        clickEvent.put("value", command);
        chatExtra.put("clickEvent", clickEvent);
        extra.add(chatExtra);
        message.put("extra", extra);
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message.toJSONString()), (byte) 1));
	}
	
	public static void sendMessageToRunCommand(Player player, String [] text, String [] hover, String [] command) {
		sendMessageToRunCommand(player, text, hover, command, "");
	}
	
	public static void sendMessageToRunCommand(Player player, String [] text, String [] hover, String [] command, String prefix) {
		for(int a = 0; a < text.length; ++a) {
			text[a] = ChatColor.translateAlternateColorCodes('&', text[a]);
		}
		JSONObject message = new JSONObject();
		message.put("text", ChatColor.translateAlternateColorCodes('&', prefix));
		JSONArray extra = new JSONArray();
        for(int a = 0; a < text.length; ++a) {
            JSONObject chatExtra = new JSONObject();
            chatExtra.put("text", text[a]);
            JSONObject hoverEvent = new JSONObject();
            hoverEvent.put("action", "show_text");
            hoverEvent.put("value", hover[a]);
            chatExtra.put("hoverEvent", hoverEvent);
            JSONObject clickEvent = new JSONObject();
            clickEvent.put("action", "run_command");
            clickEvent.put("value", command[a]);
            chatExtra.put("clickEvent", clickEvent);
            extra.add(chatExtra);
        }
        message.put("extra", extra);
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message.toJSONString()), (byte) 1));
	}
}