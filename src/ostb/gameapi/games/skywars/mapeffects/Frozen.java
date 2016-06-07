package ostb.gameapi.games.skywars.mapeffects;

import org.bukkit.Location;
import org.bukkit.World;

import ostb.gameapi.mapeffects.MapEffectsBase;
import ostb.server.Campfire;

public class Frozen extends MapEffectsBase {
	public Frozen() {
		super("Frozen");
	}

	@Override
	public void execute(World world) {
		for(Location location : new Location [] {
				new Location(world, -81, 123, 156),
				new Location(world, -101, 123, 176),
				new Location(world, -121, 123, 156),
				new Location(world, -154, 123, 123),
				new Location(world, -174, 123, 103),
				new Location(world, -154, 123, 83),
				new Location(world, -121, 123, 50),
				new Location(world, -101, 123, 30),
				new Location(world, -81, 123, 50),
				new Location(world, -48, 123, 83),
				new Location(world, -28, 123, 103),
				new Location(world, -48, 123, 123)
			}) {
			new Campfire(location.add(0.5, 0, 0.5));
		}
	}
}
