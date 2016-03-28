package ostb.gameapi.games.hardcoreelimination.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.gameapi.GracePeriod;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.hardcoreelimination.HardcoreEliminationShop;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class BlastMiner extends KitBase {
	private static final int amount = 3;
	private static final int price = 1200;
	
	public BlastMiner() {
		super(Plugins.HE_KITS, new ItemCreator(Material.TNT).setName("Blast Miner").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " TNT",
			"",
			"&7Note:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aTNT cannot damage players",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aTNT only explodes during grace period",
			"",
			"&7Coins: &a" + price
		}).getItemStack(), price);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(HardcoreEliminationShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().setHelmet(new ItemStack(Material.TNT, amount));
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		if(!GracePeriod.isRunning()) {
			if(event.blockList() != null) {
				event.blockList().clear();
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof TNTPrimed) {
			event.setCancelled(true);
		}
	}
}
