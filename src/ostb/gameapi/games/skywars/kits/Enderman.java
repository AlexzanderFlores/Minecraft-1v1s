package ostb.gameapi.games.skywars.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import ostb.OSTB.Plugins;
import ostb.gameapi.KitBase;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Enderman extends KitBase {
	private static final int price = 1000;
	private static boolean enabled = false;
	
	public Enderman() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.ENDER_PEARL).setName("Enderman").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aTake no Enderpearl Damage",
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
		EventUtil.register(this);
		enabled = true;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(enabled && event.getCause() == TeleportCause.ENDER_PEARL && has(event.getPlayer())) {
			event.getPlayer().teleport(event.getTo());
			event.setCancelled(true);
		}
	}
}
