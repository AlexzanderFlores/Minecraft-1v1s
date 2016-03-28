package ostb.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ostb.ProPlugin;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.util.EventUtil;

//TODO: Use this in all places that it is useful: Staff mode, spectating
public class Vanisher implements Listener {
	private static List<String> vanished = null;
	
	public Vanisher() {
		vanished = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static boolean isVanished(Player player) {
		return vanished != null && vanished.contains(player.getName());
	}
	
	public static void toggleVanished(Player player) {
		if(isVanished(player)) {
			remove(player);
		} else {
			add(player);
		}
	}
	
	public static void add(Player player) {
		remove(player);
		vanished.add(player.getName());
		for(Player online : Bukkit.getOnlinePlayers()) {
			online.hidePlayer(player);
		}
	}
	
	public static void remove(Player player) {
		vanished.remove(player.getName());
		for(Player online : Bukkit.getOnlinePlayers()) {
			online.showPlayer(player);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for(String name : vanished) {
			Player player = ProPlugin.getPlayer(name);
			event.getPlayer().hidePlayer(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		vanished.remove(event.getPlayer().getName());
	}
}
