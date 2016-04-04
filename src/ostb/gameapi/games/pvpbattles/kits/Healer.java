package ostb.gameapi.games.pvpbattles.kits;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import de.slikey.effectlib.util.ParticleEffect;
import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.pvpbattles.PVPBattlesShop;
import ostb.server.util.CircleUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class Healer extends KitBase {
	private CircleUtil circle = null;
	
	public Healer() {
		super(Plugins.PVP_BATTLES, new ItemCreator(getPotion()).setName("Healer").getItemStack(), -1);
		setHelmet(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_HELMET), Color.fromRGB(25, 220, 40)));
		setChestplate(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.fromRGB(25, 220, 40)));
		setLeggings(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.fromRGB(25, 220, 40)));
		setBoots(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.fromRGB(25, 220, 40)));
	}
	
	private static ItemStack getPotion() {
		return new Potion(PotionType.REGEN, 1, true).toItemStack(1);
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
			if(circle == null) {
				circle = new CircleUtil(armorStand, .85, 6) {
					@Override
					public void run(Vector vector, Location location) {
						ParticleEffect.VILLAGER_HAPPY.display(location.add(0, 2.25, 0), 5);
						//TODO: Display to the specific player
					}
				};
			}
		}
	}
	
	@Override
	public void disableArt(ArmorStand armorStand) {
		super.disableArt(armorStand);
		if(circle != null) {
			circle.delete();
			circle = null;
		}
	}
}
