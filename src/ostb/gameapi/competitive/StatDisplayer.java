package ostb.gameapi.competitive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.customevents.game.GameVotingEvent;
import ostb.gameapi.competitive.StatsHandler.StatTimes;
import ostb.server.util.EventUtil;
import ostb.server.util.StringUtil;

public class StatDisplayer implements Listener {
	private World world = null;
	private List<ArmorStand> armorStands = null;
	
	public StatDisplayer() {
		world = OSTB.getMiniGame().getLobby();
		display();
		EventUtil.register(this);
	}
	
	private void display() {
		if(armorStands != null) {
			for(ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
		}
		armorStands = new ArrayList<ArmorStand>();
		double y = 7.5;
		Location [] locations = new Location [] {new Location(world, -5, y, -16), new Location(world, 0, y, -16), new Location(world, 5, y, -16)};
		for(int a = 0; a < StatTimes.values().length; ++a) {
			StatTimes time = StatTimes.values()[a];
			Location location = locations[a].clone();
			text("&e" + (time == StatTimes.LIFETIME ? "Top 10 &7(&bLifetime" : time == StatTimes.MONTHLY ? "Top 10 &7(&bMonthly" : "Top 10 &7(&bWeekly") + "&7)", location);
			for(String top : StatsHandler.getTop10(time)) {
				location = location.add(0, -0.35, 0);
				text(top, location);
			}
		}
	}
	
	private void text(String text, Location location) {
		ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
		setUpArmorStand(armorStand);
		armorStand.setCustomName(StringUtil.color(text));
		armorStands.add(armorStand);
	}
	
	private void setUpArmorStand(ArmorStand armorStand) {
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
	}
	
	@EventHandler
	public void onGameVoting(GameVotingEvent event) {
		display();
	}
}
