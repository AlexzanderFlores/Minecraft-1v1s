package network.server.servers.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import network.player.account.AccountHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.CommandBase;
import network.server.DB;
import network.server.DB.Databases;
import network.server.tasks.AsyncDelayedTask;

public class RecentSupporters implements Listener {
	private Skull [] skulls = null;
	private Sign [] signs = null;
	
	public RecentSupporters() {
		World world = Bukkit.getWorlds().get(0);
		skulls = new Skull [] {(Skull) world.getBlockAt(1685, 6, -1261).getState(), (Skull) world.getBlockAt(1684, 6, -1261).getState(), (Skull) world.getBlockAt(1683, 6, -1261).getState()};
		signs = new Sign [] {(Sign) world.getBlockAt(1685, 5, -1262).getState(), (Sign) world.getBlockAt(1684, 5, -1262).getState(), (Sign) world.getBlockAt(1683, 5, -1262).getState()};
		new CommandBase("updateSupporters") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				run();
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		run();
	}
	
	private void run() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<String> packageNames = new ArrayList<String>();
				List<String> names = new ArrayList<String>();
				
				ResultSet resultSet = null;
				try {
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT uuid,package,package FROM recent_supporters ORDER BY id DESC LIMIT 3").executeQuery();
					while(resultSet.next()) {
						uuids.add(UUID.fromString(resultSet.getString("uuid")));
						packageNames.add(resultSet.getString("package"));
					}
				} catch(SQLException e) {
					Bukkit.getLogger().info(e.getMessage());
				} finally {
					DB.close(resultSet);
				}
				
				if(uuids.size() < 3) {
					return;
				}
				
				for(UUID uuid : uuids) {
					names.add(AccountHandler.getName(uuid));
				}
				
				for(int a = 0; a < 3; ++a) {
					skulls[a].setOwner(names.get(a));
					skulls[a].update();
					signs[a].setLine(1, names.get(a));
					signs[a].setLine(2, packageNames.get(a));
					signs[a].update();
				}
			}
		}, 20);
	}
}
