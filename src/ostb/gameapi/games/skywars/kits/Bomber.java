package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.MouseClickEvent.ClickType;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.skywars.SkyWarsShop;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Bomber extends KitBase implements Listener {
	private static final int amount = 2;
	private static final int price = 875;
	private static boolean enabled = false;
	
	public Bomber() {
		super(Plugins.SKY_WARS, new ItemCreator(Material.TNT).setName("Bomber").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " TNT",
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aLeft click will throw TNT",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aPlacing will place primed TNT",
			"",
			"&7Coins: &a" + price
		}).getItemStack(), price);
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemStack(Material.TNT, amount));
		}
		EventUtil.register(this);
		enabled = true;
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(enabled) {
			Player player = event.getPlayer();
			if(has(player) && event.getClickType() == ClickType.LEFT_CLICK) {
				ItemStack item = player.getItemInHand();
				if(item != null && item.getType() == Material.TNT) {
					TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
					tnt.setVelocity(player.getLocation().getDirection().multiply(1.5d));
					tnt.setFuseTicks(tnt.getFuseTicks() / 2);
					removeATNT(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(enabled) {
			Player player = event.getPlayer();
			if(has(player) && event.getBlock().getType() == Material.TNT) {
				TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(tnt.getFuseTicks() / 2);
				removeATNT(player);
				event.setCancelled(true);
			}
		}
	}
	
	private void removeATNT(Player player) {
		ItemStack item = player.getItemInHand();
		int amount = item.getAmount() - 1;
		if(amount <= 0) {
			player.setItemInHand(new ItemStack(Material.AIR));
		} else {
			player.setItemInHand(new ItemStack(Material.TNT, amount));
		}
	}
}
