package ostb.gameapi;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameEndingEvent;
import ostb.customevents.game.GameLossEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.game.GameVotingEvent;
import ostb.customevents.game.GameWaitingEvent;
import ostb.customevents.game.GameWinEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.server.nms.npcs.NPCRegistrationHandler.NPCs;
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
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(ProPlugin.getPlayers().size() > 0) {
				MiniGame miniGame = getMiniGame();
				GameStates gameState = miniGame.getGameState();
				if(gameState == GameStates.WAITING) {
					int waitingFor = miniGame.getRequiredPlayers() - ProPlugin.getPlayers().size();
					if(waitingFor <= 0) {
						miniGame.setGameState(GameStates.VOTING);
					} else {
						if(OSTB.getMiniGame().getUpdateTitleSidebar()) {
							OSTB.getSidebar().setName("&e" + ProPlugin.getPlayers().size() + "&8/&e" + getMiniGame().getRequiredPlayers() + " Needed");
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
					if(miniGame.getCounter() == 10) {
						if(StatsHandler.isEnabled()) {
							for(Player player : ProPlugin.getPlayers()) {
								try {
									StatsHandler.loadStats(player);
								} catch(Exception e) {
									
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
						/*if(miniGame.getCounter() <= 5) {
							for(Player player : Bukkit.getOnlinePlayers()) {
								String color = "&e";
								if(miniGame.getCounter() == 2) {
									color = "&c";
								} else if(miniGame.getCounter() == 1) {
									color = "&4";
								}
								new TitleDisplayer(player, "&2Starting", color + miniGame.getCounter()).setFadeIn(0).setStay(15).setFadeOut(60).display();
							}
						}*/
					}
				} else if(gameState == GameStates.STARTED) {
					
				} else if(gameState == GameStates.ENDING) {
					if(miniGame.getCounter() <= 0) {
						ProPlugin.restartServer();
					} else {
						MessageHandler.alert("Server restarting in " + miniGame.getCounterAsString());
						if(OSTB.getMiniGame().getUpdateTitleSidebar()) {
							OSTB.getSidebar().update(miniGame.getCounterAsString());
						}
					}
				}
			}
			OSTB.getSidebar().update();
			getMiniGame().decrementCounter();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameWaiting(GameWaitingEvent event) {
		MiniGame miniGame = getMiniGame();
		miniGame.resetFlags();
		World lobby = miniGame.getLobby();
		if(lobby != null) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!player.getWorld().getName().equals(lobby.getName())) {
					player.teleport(lobby.getSpawnLocation());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameVoting(GameVotingEvent event) {
		getMiniGame().setCounter(getMiniGame().getVotingCounter());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameStarting(GameStartingEvent event) {
		getMiniGame().setCounter(getMiniGame().getStartingCounter());
		for(NPCs npc : NPCs.values()) {
			npc.unregister();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameStart(GameStartEvent event) {
		getMiniGame().setCounter(0);
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().clear();
			player.getInventory().setHeldItemSlot(0);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGameEnding(GameEndingEvent event) {
		getMiniGame().setCounter(getMiniGame().getEndingCounter());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		MiniGame miniGame = getMiniGame();
		if(miniGame.getJoiningPreGame()) {
			GameStates gameState = miniGame.getGameState();
			Player player = event.getPlayer();
			List<Player> players = ProPlugin.getPlayers();
			if(gameState == GameStates.STARTING && miniGame.getCanJoinWhileStarting() && players.size() > 0) {
				player.teleport(players.get(0).getWorld().getSpawnLocation());
			} else {
				player.teleport(miniGame.getLobby().getSpawnLocation());
			}
			if(gameState == GameStates.WAITING && players.size() >= miniGame.getRequiredPlayers()) {
				miniGame.setGameState(GameStates.VOTING);
			}
			players.clear();
			players = null;
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLeave(final PlayerLeaveEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MiniGame miniGame = getMiniGame();
				GameStates gameState = miniGame.getGameState();
				List<Player> players = ProPlugin.getPlayers();
				int playing = players.size();
				Player leaving = event.getPlayer();
				if(gameState == GameStates.VOTING && playing < miniGame.getRequiredPlayers()) {
					for(Player player : players) {
						new TitleDisplayer(player, "&eWaiting for Players").display();
					}
					miniGame.setGameState(GameStates.WAITING);
				} else if(gameState == GameStates.STARTING && playing == 1) {
					for(Player player : players) {
						new TitleDisplayer(player, "&eWaiting for Players").display();
					}
					miniGame.setGameState(GameStates.WAITING);
				}
				if(gameState == GameStates.STARTING || gameState == GameStates.STARTED) {
					if(playing == 1 && miniGame.getRestartWithOnePlayerLeft()) {
						Player winner = players.get(0);
						if(winner.getName().equals(leaving.getName())) {
							winner = players.get(1);
						}
						Bukkit.getPluginManager().callEvent(new GameWinEvent(winner));
					} else if(playing == 0) {
						miniGame.setGameState(GameStates.ENDING);
					}
				}
				players.clear();
				players = null;
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		MiniGame miniGame = getMiniGame();
		if(miniGame.getPlayersHaveOneLife()) {
			Bukkit.getPluginManager().callEvent(new GameLossEvent(event.getPlayer()));
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					List<Player> players = ProPlugin.getPlayers();
					if(players.size() == 1 && OSTB.getMiniGame().getRestartWithOnePlayerLeft()) {
						Bukkit.getPluginManager().callEvent(new GameWinEvent(players.get(0)));
					} else if(players.size() == 0) {
						OSTB.getMiniGame().setGameState(GameStates.ENDING);
					}
					players.clear();
					players = null;
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
		if(event.getSpawnReason() != SpawnReason.CUSTOM && event.getEntity().getWorld().equals(getMiniGame().getLobby())) {
			event.setCancelled(true);
		}
	}
}
