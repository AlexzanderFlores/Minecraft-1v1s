package ostb.gameapi.games.skywars.cages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SmallCage extends Cage {
	public SmallCage(Player player) {
		super(player);
		place();
	}
	
	public SmallCage(Player player, Material material) {
		this(player, material, (byte) 0);
	}
	
	public SmallCage(Player player, Material material, byte data) {
		super(player);
		setMaterial(material, data);
		place();
	}

	@Override
	public void place() {
		Player player = getPlayer();
		if(player != null) {
			Location location = player.getLocation();
			placeBlock(location.clone().add(0, -1, 0));
			for(int [] a : new int [] [] {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}) {
				int x = a[0];
				int z = a[1];
				for(int y = 0; y < 3; ++y) {
					placeBlock(location.clone().add(x, y, z));
				}
			}
			teleport(player);
		}
	}
}
