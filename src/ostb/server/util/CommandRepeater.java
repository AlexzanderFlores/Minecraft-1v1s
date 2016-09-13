package ostb.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;

public class CommandRepeater implements Listener {
	private class RepeaterData {
		private String name = null;
		private String command = null;
		
		public RepeaterData(String name, String command) {
			this.name = name;
			this.command = command;
		}
		
		private void execute() {
			Player player = ProPlugin.getPlayer(name);
			if(player == null) {
				for(long a : data.keySet()) {
					List<RepeaterData> list = data.get(a);
					Iterator<RepeaterData> iterator = list.iterator();
					while(iterator.hasNext()) {
						if(iterator.next().name.equals(this.name)) {
							iterator.remove();
						}
					}
					if(list.isEmpty()) {
						data.remove(a);
						if(data.isEmpty()) {
							registered = false;
							HandlerList.unregisterAll(instance);
						}
					} else {
						data.put(a, list);
					}
				}
			} else {
				player.performCommand(command);
			}
		}
	}
	
	private static CommandRepeater instance = null;
	private static boolean registered = false;
	private Map<Long, List<RepeaterData>> data = null;
	
	public CommandRepeater() {
		instance = this;
		data = new HashMap<Long, List<RepeaterData>>();
		new CommandBase("repeatCommand", 2, -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				long ticks = -1;
				try {
					ticks = Long.valueOf(arguments[0]);
				} catch(NumberFormatException e) {
					return false;
				}
				String command = "";
				for(int a = 1; a < arguments.length; ++a) {
					command += arguments[a] + " ";
				}
				List<RepeaterData> list = data.get(ticks);
				if(list == null) {
					list = new ArrayList<RepeaterData>();
				}
				list.add(new RepeaterData(sender.getName(), command));
				data.put(ticks, list);
				if(!registered) {
					registered = true;
					EventUtil.register(instance);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			int size = 0;
			for(long a : data.keySet()) {
				size += data.get(a).size();
			}
			Bukkit.getLogger().info("Command repeater: " + size + " logged");
		}
		if(data.containsKey(ticks)) {
			List<RepeaterData> list = data.get(ticks);
			for(RepeaterData repeaterData : list) {
				repeaterData.execute();
			}
		}
	}
}
