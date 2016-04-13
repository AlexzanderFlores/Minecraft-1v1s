package ostb.gameapi.games.skywars.cages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BigCage extends Cage {
	public BigCage(Player player) {
		super(player);
		place();
	}
	
	public BigCage(Player player, Material material) {
		this(player, material, (byte) 0);
	}
	
	public BigCage(Player player, Material material, byte data) {
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
			teleport(player);
		}
	}
}
