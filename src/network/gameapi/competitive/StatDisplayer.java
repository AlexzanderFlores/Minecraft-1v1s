package network.gameapi.competitive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.customevents.TimeEvent;
import network.gameapi.competitive.StatsHandler.StatTimes;
import network.player.MessageHandler;
import network.server.util.EventUtil;
import network.server.util.StringUtil;

public class StatDisplayer implements Listener {
	private static List<Hologram> holograms = null;
	private static List<Location> locations = null;
	
	public StatDisplayer() {
		this(locations);
	}
	
	public StatDisplayer(List<Location> locations) {
		if(holograms == null) {
			EventUtil.register(this);
		} else {
			for(Hologram hologram : holograms) {
				hologram.setLocation(new Location(hologram.getLocation().getWorld(), 0, -100, 0));
			}
		}
		holograms = new ArrayList<Hologram>();
		StatDisplayer.locations = locations;
		for(int a = 0; a < StatTimes.values().length; ++a) {
			StatTimes time = StatTimes.values()[a];
			Location location = locations.get(a).clone();
			HologramAPI.createHologram(location, StringUtil.color("&e" + (time == StatTimes.LIFETIME ? "Top 10 &7(&bLifetime" : time == StatTimes.MONTHLY ? "Top 10 &7(&bMonthly" : "Top 10 &7(&bWeekly") + "&7)")).spawn();
			for(String top : StatsHandler.getTop10(time)) {
				location = location.add(0, -0.35, 0);
				Hologram hologram = HologramAPI.createHologram(location, StringUtil.color(top));
				hologram.spawn();
			}
		}
		MessageHandler.alert("");
		MessageHandler.alert("Updated top 10 leaderboard holograms");
		MessageHandler.alert("");
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60) {
			new StatDisplayer();
		}
	}
}