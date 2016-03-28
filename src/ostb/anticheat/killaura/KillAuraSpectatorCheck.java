package ostb.anticheat.killaura;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ostb.ProPlugin;
import ostb.anticheat.AntiCheat;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.timed.FiveSecondTaskEvent;
import ostb.customevents.timed.TenTickTaskEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

public class KillAuraSpectatorCheck extends AntiCheat implements Listener {
	private static Map<String, String> watching = null;
	private Map<String, String> oldListNames = null;
	private Map<String, Integer> lookingUpCounter = null;
	private float lookingUp = -45;
	
	public KillAuraSpectatorCheck() {
		super("KillAura-S");
		watching = new HashMap<String, String>();
		oldListNames = new HashMap<String, String>();
		lookingUpCounter = new HashMap<String, Integer>();
		new CommandBase("killAura", 1, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(isEnabled()) {
					final Player player = ProPlugin.getPlayer(arguments[0]);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online!");
					} else if(SpectatorHandler.contains(player)) {
						MessageHandler.sendMessage(sender, "&c" + player.getName() + " is spectating!");
					} else {
						final Player viewer = (Player) sender;
						if(SpectatorHandler.contains(viewer)) {
							if(viewer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
								if(viewer.getItemInHand() == null || viewer.getItemInHand().getType() == Material.AIR) {
									if(arguments.length == 1) {
										if(watching.containsKey(viewer.getName())) {
											MessageHandler.sendMessage(viewer, "&cYou are no longer watching " + watching.get(viewer.getName()));
											watching.remove(viewer.getName());
										} else {
											watching.put(viewer.getName(), player.getName());
											MessageHandler.sendMessage(sender, "You are now watching " + player.getName());
										}
									} else if(arguments.length == 2) {
										if(arguments[1].equalsIgnoreCase("tp")) {
											final String oldName;
											if(oldListNames.containsKey(viewer.getName())) {
												oldName = oldListNames.get(viewer.getName());
											} else {
												oldName = viewer.getPlayerListName();
												oldListNames.put(viewer.getName(), oldName);
											}
											//viewer.setPlayerListName(Ranks.PLAYER.getColor() + RandomStringUtils.randomAlphanumeric(new Random().nextInt(5) + 3));
											viewer.teleport(player.getLocation().add(0, 2, 0));
											viewer.setAllowFlight(true);
											viewer.setFlying(true);
											Location location = viewer.getLocation();
											location.setPitch(90.0f);
											viewer.teleport(location);
											if(player.getLocation().getPitch() > lookingUp) {
												player.showPlayer(viewer);
												new DelayedTask(new Runnable() {
													@Override
													public void run() {
														float pitch = player.getLocation().getPitch();
														if(pitch <= lookingUp && Ranks.isStaff(viewer)) {
															int counter = 0;
															if(lookingUpCounter.containsKey(player.getName())) {
																counter = lookingUpCounter.get(player.getName());
															}
															if(++counter >= 5) {
																ban(player, viewer);
															} else {
																lookingUpCounter.put(player.getName(), counter);
															}
														}
														player.hidePlayer(viewer);
														viewer.setPlayerListName(oldName);
													}
												}, 5);
											}
										} else {
											return false;
										}
									}
								} else {
									MessageHandler.sendMessage(viewer, "&cYou must not be holding anything to use this command");
								}
							} else {
								MessageHandler.sendMessage(viewer, "&cYou do not have invisibility... Giving now");
								viewer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
							}
						} else {
							MessageHandler.sendMessage(viewer, "&cYou must be a spectator or in staff mode to run this command");
						}
					}
				} else {
					MessageHandler.sendMessage(sender, "&cThe Anti Cheat is currently &4DISABLED");
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static boolean isWatching(Player player) {
		return watching != null && watching.containsKey(player.getName());
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(isEnabled() && watching != null) {
			Iterator<String> iterator = watching.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				Player player = ProPlugin.getPlayer(name);
				if(player == null) {
					iterator.remove();
				} else {
					String targetName = watching.get(name);
					Player target = ProPlugin.getPlayer(targetName);
					if(target == null || SpectatorHandler.contains(target)) {
						iterator.remove();
						MessageHandler.sendMessage(player, "&c" + targetName + "is no longer playing");
					} else {
						player.chat("/killAura " + targetName + " tp");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		if(isEnabled()) {
			lookingUpCounter.clear();
		}
	}
	
	//@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(SpectatorHandler.contains(player)) {
				Bukkit.getLogger().info("Spectator " + player.getName() + " was damaged");
				Player damager = (Player) event.getDamager();
				if(damager.canSee(player)) {
					Bukkit.getLogger().info("Damager " + damager.getName() + " can see the spectator");
					MessageHandler.sendMessage(player, AccountHandler.getPrefix(damager) + " &e&lhas damaged you");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			watching.remove(event.getPlayer().getName());
			oldListNames.remove(event.getPlayer().getName());
		}
	}
}
