package ostb.gameapi.games.skywars.cages;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;

@SuppressWarnings("deprecation")
public class SmallCage extends Cage {
	public SmallCage(ItemStack icon) {
		this(null, icon);
	}
	
	public SmallCage(ItemStack icon, int slot) {
		this(null, icon, slot);
	}
	
	public SmallCage(Player player, ItemStack icon) {
		this(player, icon, -1);
	}
	
	public SmallCage(Player player, ItemStack icon, int slot) {
		super(player, icon, Rarity.COMMON, slot);
		setMaterial(icon.getType(), icon.getData().getData());
		setKitType("small_cage");
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
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
