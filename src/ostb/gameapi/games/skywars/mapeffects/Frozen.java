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
				new Location(world, -69, 123, 162), new Location(world, -42, 123, 135), new Location(world, -18, 123, 103), new Location(world, -42, 123, 71),
				new Location(world, -69, 123, 44), new Location(world, -101, 123, 20), new Location(world, -133, 123, 44), new Location(world, -160, 123, 71),
				new Location(world, -184, 123, 103), new Location(world, -160, 123, 135), new Location(world, -133, 123, 162), new Location(world, -101, 123, 186)
			}) {
			new Campfire(location.add(0.5, 0, 0.5));
		}
	}
}
