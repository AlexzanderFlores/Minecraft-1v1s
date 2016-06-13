package ostb.gameapi.competitive;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import ostb.OSTB;
import ostb.gameapi.competitive.StatsHandler.StatTimes;
import ostb.server.util.StringUtil;

public class StatDisplayer {
	private World world = null;
	
	public StatDisplayer() {
		world = OSTB.getMiniGame().getLobby();
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
	}
	
	private void setUpArmorStand(ArmorStand armorStand) {
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomNameVisible(true);
	}
}
