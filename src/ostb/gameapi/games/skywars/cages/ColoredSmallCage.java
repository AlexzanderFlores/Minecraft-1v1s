package ostb.gameapi.games.skywars.cages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ColoredSmallCage extends Cage {
	private Material material = null;
	private int data = 0;
	
	public ColoredSmallCage(Player player, Material material, int data) {
		super(player);
		this.material = material;
		this.data = data;
		place();
	}

	@Override
	public void place() {
		Player player = getPlayer();
		if(player != null) {
			setMaterial(material, (byte) data);
			Location location = player.getLocation();
			placeBlock(location.clone().add(0, -1, 0));
			for(int [] a : new int [] [] {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}) {
				int x = a[0];
				int z = a[1];
				for(int y = 0; y < 3; ++y) {
					placeBlock(location.clone().add(x, y, z));
				}
			}
			player.teleport(getBlocks().get(0).getLocation().clone().add(0.5, 1, 0.5));
		}
	}
}
