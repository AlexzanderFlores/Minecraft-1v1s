package network.gameapi.games.kitpvp;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import network.Network;
import network.Network.Plugins;
import network.customevents.ServerRestartAlertEvent;
import network.customevents.game.GameKillEvent;
import network.customevents.player.AsyncPostPlayerJoinEvent;
import network.player.CoinsHandler;
import network.player.LevelGiver;
import network.player.MessageHandler;
import network.server.util.EventUtil;

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
