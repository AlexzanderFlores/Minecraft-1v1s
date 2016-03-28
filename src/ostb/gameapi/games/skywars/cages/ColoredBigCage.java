package ostb.gameapi.games.skywars.cages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ColoredBigCage extends Cage {
	private Material material = null;
	private int data = 0;
	
	public ColoredBigCage(Player player, Material material, int data) {
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
			Block min = location.getBlock().getRelative(-3, -1, -3);
			Block max = location.getBlock().getRelative(3, 2, 3);
			World world = player.getWorld();
			int x1 = min.getX();
			int y1 = min.getY();
			int z1 = min.getZ();
			int x2 = max.getX();
			int y2 = max.getY();
			int z2 = max.getZ();
			for(int x = x1; x <= x2; ++x) {
				for(int y = y1; y <= y2; ++y) {
					for(int z = z1; z <= z2; ++z) {
						if(x == x1 || x == x2 || y == y1 || z == z1 || z == z2) {
							placeBlock(world.getBlockAt(x, y, z));
						}
					}
				}
			}
			player.teleport(getBlocks().get(0).getLocation().clone().add(0.5, 1, 0.5));
		}
	}
}
