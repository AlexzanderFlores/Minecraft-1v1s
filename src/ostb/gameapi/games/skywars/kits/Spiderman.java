package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.MouseClickEvent.ClickType;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Spiderman extends KitBase {
	private static final int amount = 10;
	private static boolean enabled = false;
	
	public Spiderman() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.WEB).setName("Spiderman").setLores(new String [] {
			"",
			"&7Start with:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &a" + amount + " \"Web\" Snowballs",
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aSnowballs place webs upon landing",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1, 32);
	}
	
	public static Rarity getRarity() {
		return Rarity.RARE;
	}

	@Override
	public String getPermission() {
		return ChatColor.stripColor(SkyWarsShop.getInstance().getPermission() + getName().toLowerCase());
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemCreator(Material.SNOW_BALL).setName("&fWeb").setAmount(amount).getItemStack());
		}
		if(!enabled) {
			EventUtil.register(this);
		}
		enabled = true;
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(enabled && event.getClickType() == ClickType.RIGHT_CLICK) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();
			if(item != null && item.getType() == Material.SNOW_BALL) {
				String name = item.getItemMeta().getDisplayName();
				if(name != null && name.endsWith("Web")) {
					Snowball snowball = (Snowball) player.launchProjectile(Snowball.class);
					snowball.setTicksLived(500);
					int amount = item.getAmount() - 1;
					if(amount <= 0) {
						player.setItemInHand(new ItemStack(Material.AIR));
					} else {
						player.setItemInHand(new ItemCreator(item).setAmount(amount).getItemStack());
					}
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(enabled && event.getEntity() instanceof Snowball && event.getEntity().getTicksLived() >= 500) {
			event.getEntity().getLocation().getBlock().setType(Material.WEB);
		}
	}
}
