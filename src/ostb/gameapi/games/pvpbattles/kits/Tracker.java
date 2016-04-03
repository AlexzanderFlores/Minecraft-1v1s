package ostb.gameapi.games.pvpbattles.kits;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.pvpbattles.PVPBattlesShop;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class Tracker extends KitBase {
	public Tracker() {
		super(Plugins.PVP_BATTLES, new ItemCreator(Material.COMPASS).setName("Tracker").getItemStack(), -1);
		setHelmet(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_HELMET), Color.fromRGB(150, 120, 25)));
		setChestplate(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.fromRGB(40, 70, 15)));
		setLeggings(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.fromRGB(150, 120, 25)));
		setBoots(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.fromRGB(40, 70, 15)));
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(PVPBattlesShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		
	}
	
	@Override
	public void executeArt(final ArmorStand armorStand, boolean all, final Player player) {
		super.executeArt(armorStand, all, player);
		if(all) {
			
		}
	}
	
	@Override
	public void disableArt(ArmorStand armorStand) {
		super.disableArt(armorStand);
	}
}
