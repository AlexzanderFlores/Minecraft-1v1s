package ostb.gameapi.games.skywars.kits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import ostb.OSTB.Plugins;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.KitBase;
import ostb.gameapi.games.skywars.Events;
import ostb.gameapi.shops.SkyWarsShop;
import ostb.player.TitleDisplayer;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Looter extends KitBase {
	private static final int amount = 3;
	private static Map<String, Integer> uses = null;
	private static boolean enabled = false;
	
	public Looter() {
		super(Plugins.SKY_WARS_SOLO, new ItemCreator(Material.CHEST).setName("Looter").setLores(new String [] {
			"",
			"&7Abilities:",
			"   &7" + UnicodeUtil.getUnicode("25B6") + " &aLoot up to " + amount + " chest by breaking them",
			"",
			"&7Unlocked in &bSky Wars Crate",
			"&7Rarity: " + getRarity().getName()
		}).getItemStack(), getRarity(), -1);
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
		uses = new HashMap<String, Integer>();
		for(Player player : getPlayers()) {
			uses.put(player.getName(), amount);
		}
		EventUtil.register(this);
		enabled = true;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(enabled) {
			Player player = event.getPlayer();
			if(event.getBlock().getType() == Material.CHEST && uses.containsKey(player.getName())) {
				Events.restock(event.getBlock());
				int amount = uses.get(player.getName()) - 1;
				if(amount <= 0) {
					uses.remove(player.getName());
					amount = 0;
				} else {
					uses.put(player.getName(), amount);
				}
				new TitleDisplayer(player, "&2" + amount, "&aUses left").setFadeIn(0).setStay(20).setFadeOut(0).display();
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(enabled) {
			uses.remove(event.getPlayer().getName());
		}
	}
}
