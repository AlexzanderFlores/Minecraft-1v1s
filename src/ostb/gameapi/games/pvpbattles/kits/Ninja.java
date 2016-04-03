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

public class Ninja extends KitBase {
	public Ninja() {
		super(Plugins.PVP_BATTLES, new ItemCreator(Material.NETHER_STAR).setName("Ninja").getItemStack(), -1);
		setHelmet(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_HELMET), Color.fromRGB(25, 25, 25)));
		setChestplate(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.fromRGB(25, 25, 25)));
		setLeggings(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.fromRGB(25, 25, 25)));
		setBoots(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.fromRGB(25, 25, 25)));
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(PVPBattlesShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		
	}
	
	@Override
	public void executeArt(ArmorStand armorStand, boolean all, Player player) {
		super.executeArt(armorStand, all, player);
		if(all) {
			
		}
	}
	
	@Override
	public void disableArt(ArmorStand armorStand) {
		super.disableArt(armorStand);
	}
}
