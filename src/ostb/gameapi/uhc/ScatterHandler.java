package ostb.gameapi.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import anticheat.events.TimeEvent;
import ostb.ProPlugin;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.uhc.Events;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class ScatterHandler implements Listener {
	private Map<String, Location> spawns = null;
	private List<String> scattered = null;
	private boolean logSpawns = false;
	private boolean canRescatter = false;
	private boolean chatAlerts = true;
	
	public ScatterHandler(int size, boolean chatAlerts) {
		spawns = new HashMap<String, Location>();
		scattered = new ArrayList<String>();
		this.chatAlerts = chatAlerts;
		new CommandBase("rescatter", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				final Player player = (Player) sender;
				if(SpectatorHandler.contains(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot rescatter as a spectator");
				} else {
					if(canRescatter) {
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								UUID uuid = player.getUniqueId();
								if(DB.PLAYERS_SPEED_UHC_RESCATTER.isUUIDSet(uuid)) {
									int amount = DB.PLAYERS_SPEED_UHC_RESCATTER.getInt("uuid", uuid.toString(), "amount");
									if(amount > 0) {
										DB.PLAYERS_SPEED_UHC_RESCATTER.updateInt("amount", --amount, "uuid", uuid.toString());
										final String command = "spreadPlayers 0 0 100 " + size + " false " + player.getName();
										player.setNoDamageTicks(20 * 30);
										new DelayedTask(new Runnable() {
											@Override
											public void run() {
												Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
											}
										});
										new TitleDisplayer(player, "&e" + amount + " &brescatter" + (amount == 1 ? "" : "s") + " left", "&cGet more with &a/vote").display();
										return;
									} else {
										DB.PLAYERS_SPEED_UHC_RESCATTER.deleteUUID(uuid);
									}
								}
								new TitleDisplayer(player, "&cOut of rescatters", "&cGet more with &a/vote").display();
							}
						});
					} else {
						MessageHandler.sendMessage(player, "&cYou cannot rescatter at this time");
					}
				}
				return true;
			}
		}.enableDelay(2);
		EventUtil.register(this);
		String command = "spreadPlayers 0 0 100 " + size + " false ";
		for(Player player : ProPlugin.getPlayers()) {
			player.setNoDamageTicks(20 * 30);
			command += player.getName() + " ";
			new TitleDisplayer(player, "&bScattering...").display();
		}
		logSpawns = true;
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		canRescatter = true;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				canRescatter = false;
			}
		}, 20 * 30);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					new TitleDisplayer(player, "&bHealing All...").display();
					player.setHealth(player.getMaxHealth());
					player.setFoodLevel(20);
				}
			}
		}, 20 * 30);
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			if(spawns != null && !spawns.isEmpty()) {
				String name = null;
				for(String spawn : spawns.keySet()) {
					if(!scattered.contains(spawn)) {
						name = spawn;
						break;
					}
				}
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					scattered.add(name);
					Location location = spawns.get(name);
					player.teleport(location);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							new TitleDisplayer(player, "&bBad scatter?", "&bRun command &c/rescatter").display();
						}
					}, 20);
					if(chatAlerts) {
						MessageHandler.alert("&eScattering " + AccountHandler.getPrefix(player) + " &e[&a" + scattered.size() + "&7/&a" + spawns.size() + "&e]");
					}
					if(scattered.size() >= spawns.size()) {
						ChunkUnloadEvent.getHandlerList().unregister(this);
						PlayerLeaveEvent.getHandlerList().unregister(this);
						logSpawns = false;
						scattered.clear();
						scattered = null;
						spawns.clear();
						spawns = null;
						Events.start();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(logSpawns && !spawns.containsKey(player.getName()) && !SpectatorHandler.contains(player)) {
			spawns.put(player.getName(), event.getTo());
			event.getTo().getChunk().load(true);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		spawns.remove(event.getPlayer().getName());
		scattered.remove(event.getPlayer().getName());
	}
}
