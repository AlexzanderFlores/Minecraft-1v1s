package ostb.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import anticheat.util.AsyncDelayedTask;
import ostb.ProPlugin;
import ostb.customevents.game.GameStartEvent;
import ostb.player.TitleDisplayer;
import ostb.server.DB;
import ostb.server.util.EventUtil;

public class LootPassHandler implements Listener {
	private List<String> canUsePass = null;
	
	public LootPassHandler() {
		canUsePass = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					if(DB.PLAYERS_SKY_WARS_LOOT_PASSES.isUUIDSet(player.getUniqueId())) {
						canUsePass.add(player.getName());
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if(block.getType() == Material.CHEST) {
			Player player = event.getPlayer();
			if(canUsePass.contains(player.getName())) {
				canUsePass.remove(player.getNoDamageTicks());
				ChestHandler.restock(block);
				new TitleDisplayer(player, "&bRestocked Chest", "&cGet more with &a/vote").display();
				final UUID uuid = player.getUniqueId();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						int amount = DB.PLAYERS_SKY_WARS_LOOT_PASSES.getInt("uuid", uuid.toString(), "amount") - 1;
						if(amount <= 0) {
							DB.PLAYERS_SKY_WARS_LOOT_PASSES.deleteUUID(uuid);
						} else {
							DB.PLAYERS_SKY_WARS_LOOT_PASSES.updateInt("amount", amount, "uuid", uuid.toString());
						}
					}
				});
			} else {
				new TitleDisplayer(player, "&cYou are out of restocks", "&cGet more with &a/vote").display();
			}
			event.setCancelled(true);
		}
	}
}
