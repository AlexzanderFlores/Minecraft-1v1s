package ostb.gameapi.games.pvpbattles.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.pvpbattles.PVPBattlesShop;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class Bomber extends KitBase {
	private BukkitTask task = null;
	private List<ArmorStand> tntStands = null;
	
	public Bomber() {
		super(Plugins.PVP_BATTLES, new ItemCreator(Material.TNT).setName("Bomber").getItemStack(), -1);
		setHelmet(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_HELMET), Color.fromRGB(200, 25, 25)));
		setChestplate(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.fromRGB(200, 25, 25)));
		setLeggings(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.fromRGB(200, 25, 25)));
		setBoots(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.fromRGB(200, 25, 25)));
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(PVPBattlesShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		
	}
	
	@Override
	public void executeArt(ArmorStand armorStand, boolean all, final Player player) {
		super.executeArt(armorStand, all, player);
		if(all) {
			if(tntStands == null) {
				tntStands = new ArrayList<ArmorStand>(3);
			}
			cancel();
			task = Bukkit.getScheduler().runTaskTimer(OSTB.getInstance(), new Runnable() {
				@Override
				public void run() {
					EffectUtil.playSound(player, Sound.EXPLODE, 0.5f);
				}
			}, 20, 20 * 2);
			Location loc = armorStand.getLocation();
			Random random = new Random();
			double distance = 1;
			double height = .85;
			Vector vectors [] = new Vector [] {new Vector(distance, height, distance), new Vector(-distance, height, distance), new Vector(distance, height, -distance)};
			for(int a = 0; a < vectors.length; ++a) {
				ArmorStand tntStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(vectors[a]), EntityType.ARMOR_STAND);
				tntStand.setGravity(false);
				tntStand.setVisible(false);
				tntStand.setHelmet(getIcon());
				tntStand.setHeadPose(new EulerAngle(random.nextDouble(), random.nextDouble(), random.nextDouble()));
				tntStands.add(tntStand);
			}
		}
	}
	
	@Override
	public void disableArt(ArmorStand armorStand) {
		super.disableArt(armorStand);
		if(tntStands != null) {
			for(ArmorStand tntStand : tntStands) {
				tntStand.remove();
			}
		}
		cancel();
	}
	
	private void cancel() {
		if(task != null) {
			task.cancel();
			task = null;
		}
	}
}
