package ostb.player.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.AsyncPlayerLeaveEvent;
import ostb.customevents.player.AsyncPostPlayerJoinEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;

public class PlayerTracker implements Listener {
	private List<String> queue = null;
	
	public PlayerTracker() {
		queue = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 15) {
			if(!queue.isEmpty()) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String name = queue.get(0);
						Player player = ProPlugin.getPlayer(name);
						if(player != null) {
							UUID uuid = player.getUniqueId();
							//OSTB.getClient().sendMessageToServer(new Instruction(new String [] {Inst.SERVER_LOG_PLAYER.toString(), uuid.toString(), OSTB.getServerName()}));
							String location = AccountHandler.getPrefix(player, false, true) + ChatColor.YELLOW + " is on " + ChatColor.RED + OSTB.getServerName();
							DB.PLAYERS_LOCATIONS.insert("'" + uuid.toString() + "', '" + location + "'");
						}
						queue.remove(0);
					}
				});
			}
		}
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.isStaff(player)) {
			String location = AccountHandler.getPrefix(player, true, true) + ChatColor.YELLOW + " is on " + ChatColor.RED;
			if(SpectatorHandler.contains(player)) {
				location += "VANISHED";
			} else {
				location += OSTB.getServerName();
			}
			DB.STAFF_ONLINE.insert("'" + player.getUniqueId().toString() + "', '" + location + "'");
			queue.add(player.getName());
		} else if(AccountHandler.getRank(player) != Ranks.YOUTUBER){
			queue.add(player.getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getUUID();
		//OSTB.getClient().sendMessageToServer(new Instruction(new String [] {Inst.SERVER_PLAYER_DISCONNECT.toString(), uuid.toString()}));
		DB.STAFF_ONLINE.deleteUUID(uuid);
		DB.PLAYERS_LOCATIONS.deleteUUID(uuid);
	}
}
