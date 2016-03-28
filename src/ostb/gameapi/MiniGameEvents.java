package ostb.gameapi;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameEndingEvent;
import ostb.customevents.game.GameLossEvent;
import ostb.customevents.game.GameWinEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PostPlayerJoinEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class MiniGameEvents implements Listener {
	public MiniGameEvents() {
		EventUtil.register(this);
	}
	
	private MiniGame getMiniGame() {
		return OSTB.getMiniGame();
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProPlugin.getPlayers().size() > 0) {
			MiniGame miniGame = getMiniGame();
			GameStates gameState = miniGame.getGameState();
			if(gameState == GameStates.WAITING) {
				int waitingFor = miniGame.getRequiredPlayers() - ProPlugin.getPlayers().size();
				if(waitingFor <= 0) {
					miniGame.setGameState(GameStates.VOTING);
				} else {
					if(OSTB.getMiniGame().getUpdateTitleSidebar()) {
						OSTB.getSidebar().setName("&e" + ProPlugin.getPlayers().size() + "&8/&e" + waitingFor + " Needed");
					}
				}
			} else if(gameState == GameStates.VOTING) {
				if(miniGame.getCounter() <= 0) {
					miniGame.setGameState(GameStates.STARTING);
				} else {
					if(miniGame.getCounter() <= 5) {
						for(Player player : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(player, "&2Voting Ends", miniGame.getCounterAsString()).setFadeIn(0).setStay(15).setFadeOut(60).display();
						}
					}
					if(miniGame.getCounter() <= 3) {
						EffectUtil.playSound(Sound.CLICK);
					}
					if(OSTB.getMiniGame().getUpdateTitleSidebar()) {
						OSTB.getSidebar().update(miniGame.getCounterAsString());
					}
				}
			} else if(gameState == GameStates.STARTING) {
				if(miniGame.getStoreStats() && miniGame.getCounter() == 10) {
					if(StatsHandler.isEnabled()) {
						for(Player player : ProPlugin.getPlayers()) {
							try {
								StatsHandler.loadStats(player);
							} catch(NullPointerException e) {
								
							}
						}
					}
				}
				if(miniGame.getCounter() <= 0) {
					miniGame.setGameState(GameStates.STARTED);
				} else {
					if(miniGame.getCounter() <= 5) {
						for(Player player : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(player, "&2Starting", miniGame.getCounterAsString()).setFadeIn(0).setStay(15).setFadeOut(60).display();
						}
					}
					if(miniGame.getCounter() <= 3) {
						EffectUtil.playSound(Sound.CLICK);
					}
					if(OSTB.getMiniGame().getUpdateTitleSidebar()) {
						OSTB.getSidebar().update(miniGame.getCounterAsString());
					}
					if(miniGame.getCounter() <= 5) {
						for(Player player : Bukkit.getOnlinePlayers()) {
							String color = "&e";
							if(miniGame.getCounter() == 2) {
								color = "&c";
							} else if(miniGame.getCounter() == 1) {
								color = "&4";
							}
							new TitleDisplayer(player, "&2Starting", color + miniGame.getCounter()).setFadeIn(0).setStay(15).setFadeOut(60).display();
						}
					}
				}
			}
		}
		OSTB.getSidebar().update();
		getMiniGame().decrementCounter();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPreGameEnding(GameEndingEvent event) {
		getMiniGame().setCounter(5);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameEnding(GameEndingEvent event) {
		if(event.isCancelled()) {
			getMiniGame().setGameState(GameStates.STARTED, false);
		} else {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					ProPlugin.restartServer();
				}
			}, 20 * getMiniGame().getCounter());
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(OSTB.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			if(getMiniGame().getGameState() == GameStates.STARTING && getMiniGame().getCanJoinWhileStarting() && ProPlugin.getPlayers().size() > 0) {
				player.teleport(ProPlugin.getPlayers().get(0));
			} else {
				player.teleport(getMiniGame().getLobby().getSpawnLocation());
			}
			if(OSTB.getMiniGame().getGameState() == GameStates.WAITING && ProPlugin.getPlayers().size() >= OSTB.getMiniGame().getRequiredPlayers()) {
				OSTB.getMiniGame().setGameState(GameStates.VOTING);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		GameStates state = getMiniGame().getGameState();
		if(Ranks.PREMIUM_PLUS.hasRank(player) && (state == GameStates.WAITING || state == GameStates.VOTING)) {
			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLeave(final PlayerLeaveEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				GameStates gameState = OSTB.getMiniGame().getGameState();
				List<Player> players = ProPlugin.getPlayers();
				int playing = players.size();
				Player leaving = event.getPlayer();
				if(gameState == GameStates.VOTING && playing < OSTB.getMiniGame().getRequiredPlayers()) {
					MessageHandler.alert("&cNot enough players");
					OSTB.getMiniGame().setGameState(GameStates.WAITING);
				} else if(gameState == GameStates.STARTING && playing == 1) {
					MessageHandler.alert("&cNot enough players");
					OSTB.getMiniGame().setGameState(GameStates.ENDING);
				}
				if(gameState == GameStates.STARTING || gameState == GameStates.STARTED) {
					if(OSTB.getMiniGame() != null) {
						if(playing == 1 && OSTB.getMiniGame().getRestartWithOnePlayerLeft()) {
							Player winner = players.get(0);
							if(winner.getName().equals(leaving.getName())) {
								winner = players.get(1);
							}
							Bukkit.getPluginManager().callEvent(new GameWinEvent(winner));
						} else if(playing == 0) {
							OSTB.getMiniGame().setGameState(GameStates.ENDING);
						}
					}
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(OSTB.getMiniGame().getPlayersHaveOneLife()) {
			if(OSTB.getMiniGame() != null) {
				Bukkit.getPluginManager().callEvent(new GameLossEvent(event.getPlayer()));
			}
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					List<Player> players = ProPlugin.getPlayers();
					if(players.size() == 1 && OSTB.getMiniGame().getRestartWithOnePlayerLeft()) {
						Bukkit.getPluginManager().callEvent(new GameWinEvent(players.get(0)));
					} else if(players.size() == 0) {
						OSTB.getMiniGame().setGameState(GameStates.ENDING);
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(event.getEndServer() && getMiniGame().getGameState() != GameStates.ENDING) {
			getMiniGame().setGameState(GameStates.ENDING);
		}
		getMiniGame().setAllowEntityDamage(false);
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() != SpawnReason.CUSTOM && event.getEntity().getWorld().getName().equals("lobby")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		GameStates gameState = getMiniGame().getGameState();
		if(gameState == GameStates.WAITING || gameState == GameStates.VOTING || gameState == GameStates.STARTING) {
			if(!event.getTo().getWorld().getName().equals(event.getFrom().getWorld().getName()) && event.getPlayer().getAllowFlight() && !SpectatorHandler.contains(event.getPlayer())) {
				event.getPlayer().setFlying(false);
				event.getPlayer().setAllowFlight(false);
			}
		}
	}
}
