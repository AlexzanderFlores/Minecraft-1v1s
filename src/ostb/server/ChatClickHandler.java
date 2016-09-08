package ostb.server;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.PacketPlayOutChat;

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
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message.toJSONString()), true));
    }
}