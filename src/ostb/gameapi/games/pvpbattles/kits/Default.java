package ostb.gameapi.games.pvpbattles.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import npc.ostb.util.DelayedTask;
import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.pvpbattles.PVPBattlesShop;
import ostb.server.util.ItemCreator;

public class Default extends KitBase {
	private BukkitTask task = null;
	private float x = 300.0f;
	private boolean runTask = false;
	
	public Default() {
		super(Plugins.PVP_BATTLES, new ItemCreator(Material.IRON_SWORD).setName("Default").getItemStack(), -1);
		setHelmet(new ItemStack(Material.LEATHER_HELMET));
		setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		setBoots(new ItemStack(Material.LEATHER_BOOTS));
	}
	
	@Override
	public boolean owns(Player player) {
		return true;
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
			runTask = true;
			startTask(armorStand);
		}
	}
	
	@Override
	public void disableArt(ArmorStand armorStand) {
		super.disableArt(armorStand);
		cancel();
		runTask = false;
	}
	
	private void startTask(final ArmorStand armorStand) {
		cancel();
		task = Bukkit.getScheduler().runTaskTimer(OSTB.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(runTask) {
					armorStand.setRightArmPose(new EulerAngle(x, 0f, 0f));
					x -= 25.0f;
					if(x < 100f) {
						x = 300.0f;
						cancel();
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								startTask(armorStand);
							}
						}, 20);
					}
				} else {
					cancel();
				}
			}
		}, 1, 2);
	}
	
	private void cancel() {
		if(task != null) {
			task.cancel();
			task = null;
		}
	}
}
