package ostb.gameapi.games.skywars.cages;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.server.servers.hub.items.Features.Rarity;

@SuppressWarnings("deprecation")
public class BigCage extends Cage {
	public BigCage(ItemStack icon) {
		this(null, icon);
	}
	
	public BigCage(ItemStack icon, int slot) {
		this(null, icon, slot);
	}
	
	public BigCage(Player player, ItemStack icon) {
		this(player, icon, -1);
	}
	
	public BigCage(Player player, ItemStack icon, int slot) {
		super(player, icon, Rarity.UNCOMMON, slot);
		setMaterial(icon.getType(), icon.getData().getData());
		setKitType("big_cage");
	}

	@Override
	public String getPermission() {
		return null;
	}

	@Override
	public void execute() {
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
