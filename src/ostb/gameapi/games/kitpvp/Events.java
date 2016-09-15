package ostb.gameapi.games.kitpvp;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import ostb.Network;
import ostb.Network.Plugins;
import ostb.customevents.ServerRestartAlertEvent;
import ostb.customevents.game.GameKillEvent;
import ostb.customevents.player.AsyncPostPlayerJoinEvent;
import ostb.gameapi.games.kitpvp.events.TeamSelectEvent;
import ostb.player.CoinsHandler;
import ostb.player.LevelGiver;
import ostb.player.MessageHandler;
import ostb.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setScoreboard(Network.getScoreboard());
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData());
		if(coinsHandler != null) {
			coinsHandler.getCoins(player);
			if(coinsHandler.isNewPlayer(player)) {
				coinsHandler.addCoins(player, 25, "&7(To help you get started)");
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			event.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onTeamSelect(TeamSelectEvent event) {
		Player player = event.getPlayer();
		ItemStack helmet = player.getInventory().getHelmet();
		if(helmet == null || helmet.getType() == Material.AIR) {
			player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
		}
		ItemStack chestplate = player.getInventory().getChestplate();
		if(chestplate == null || chestplate.getType() == Material.AIR) {
			player.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
		}
		ItemStack leggings = player.getInventory().getLeggings();
		if(leggings == null || leggings.getType() == Material.AIR) {
			player.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
		}
		ItemStack boots = player.getInventory().getBoots();
		if(boots == null || boots.getType() == Material.AIR) {
			player.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS));
		}
		if(!player.getInventory().contains(Material.STONE_SWORD) && !player.getInventory().contains(Material.IRON_SWORD) && !player.getInventory().contains(Material.DIAMOND_SWORD)) {
			player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new LevelGiver(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getKiller() != null) {
			Player killer = player.getKiller();
			MessageHandler.sendMessage(player, event.getDeathMessage());
			MessageHandler.sendMessage(killer, event.getDeathMessage());
		}
		event.setDeathMessage(null);
	}
	
	@EventHandler
	public void onServerRestartAlert(ServerRestartAlertEvent event) {
		MessageHandler.alert("&a&lTIP: &eSave your inventory in the &bShop's &c\"&bSave Your Items&c\" &eoption");
	}
}
