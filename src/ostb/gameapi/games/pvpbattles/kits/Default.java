package ostb.gameapi.games.pvpbattles.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.pvpbattles.PVPBattlesShop;
import ostb.player.MessageHandler;
import ostb.server.util.ItemCreator;

public class Default extends KitBase {
	private BukkitTask task = null;
	private int x = 0;
	
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
	public void executeArt(final ArmorStand armorStand, boolean all, Player player) {
		super.executeArt(armorStand, all, player);
		if(all) {
			cancel();
			task = Bukkit.getScheduler().runTaskTimer(OSTB.getInstance(), new Runnable() {
				@Override
				public void run() {
					armorStand.setRightArmPose(new EulerAngle(x--, 0, 50));
					if(x < 100) {
						x = 200;
					}
					MessageHandler.alert("X: " + x);
				}
			}, 1, 20);
		}
	}
	
	@Override
	public void disableArt(ArmorStand armorStand) {
		super.disableArt(armorStand);
		cancel();
	}
	
	private void cancel() {
		if(task != null) {
			task.cancel();
			task = null;
		}
	}
}
