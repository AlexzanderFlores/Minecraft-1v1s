package ostb.server;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.StringUtil;

public class GlobalCommands {
	public GlobalCommands() {
		new CommandBase("booster", -1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(arguments.length == 3 && Ranks.OWNER.hasRank(sender)) {
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							try {
								Plugins plugin = Plugins.valueOf(arguments[0].toUpperCase());
								String name = arguments[1];
								UUID uuid = AccountHandler.getUUID(name);
								if(uuid == null) {
									MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
								} else {
									try {
										int toAdd = Integer.valueOf(arguments[2]);
										String [] keys = new String [] {"uuid", "game_name"};
										String [] values = new String [] {uuid.toString(), plugin.getData()};
										int amount = toAdd;
										if(DB.PLAYERS_COIN_BOOSTERS.isKeySet(keys, values)) {
											amount = DB.PLAYERS_COIN_BOOSTERS.getInt(keys, values, "amount") + toAdd;
											DB.PLAYERS_COIN_BOOSTERS.updateInt("amount", amount, keys, values);
										} else {
											DB.PLAYERS_COIN_BOOSTERS.insert("'" + uuid.toString() + "', '" + plugin.getData() + "', '" + amount + "'");
										}
										MessageHandler.sendMessage(sender, "Gave " + name + " " + toAdd + " boosters for " + plugin.getData() + " (" + amount + " total)");
									} catch(NumberFormatException e) {
										MessageHandler.sendMessage(sender, "&cInvalid integer value \"&e" + arguments[2] + "&c\"");
									}
								}
							} catch(IllegalArgumentException e) {
								MessageHandler.sendMessage(sender, "&cUnknown plugin \"&e" + arguments[0].toUpperCase() + "&c\"");
							}
						}
					});
				} else {
					MessageHandler.sendMessage(sender, "URL: &cstore.OutsideTheBlock.org/category/679889");
				}
				return true;
			}
		};
		
		new CommandBase("sendCommand", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String server = arguments[0];
				String command = "";
				for(int a = 1; a < arguments.length; ++a) {
					command += arguments[a] + " ";
				}
				ProPlugin.dispatchCommandToServer(server, command);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("colorCodes") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "Edit the color of your chat with \"&&xx\" where 'x' is a number from 0-9 or a letter from a-f. Example: \"&&xa&aHey!&x\"");
				return true;
			}
		};
		
		new CommandBase("join", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				ProPlugin.sendPlayerToServer(player, arguments[0]);
				return true;
			}
		};
		
		new CommandBase("tpPos", 3, 4, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				try {
					double x = Integer.valueOf(arguments[0]);
					double y = Integer.valueOf(arguments[1]);
					double z = Integer.valueOf(arguments[2]);
					Player player = (Player) sender;
					player.setAllowFlight(true);
					player.setFlying(true);
					if(arguments.length == 3) {
						player.teleport(new Location(player.getWorld(), x, y, z));
					} else {
						player.teleport(new Location(Bukkit.getWorld(arguments[3]), x, y, z));
					}
				} catch(NumberFormatException e) {
					return false;
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("buy") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "&cComing soon");
				return true;
			}
		};
		
		new CommandBase("getLoc", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				MessageHandler.sendMessage(sender, "World: " + location.getWorld().getName());
				MessageHandler.sendMessage(sender, "X: " + location.getX());
				MessageHandler.sendMessage(sender, "Y: " + location.getY());
				MessageHandler.sendMessage(sender, "Z: " + location.getZ());
				MessageHandler.sendMessage(sender, "Yaw: " + location.getYaw());
				MessageHandler.sendMessage(sender, "Pitch: " + location.getPitch());
				return true;
			}
		};
		
		new CommandBase("gmc", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setGameMode(GameMode.CREATIVE);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("gms", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setGameMode(GameMode.SURVIVAL);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("hub", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					ProPlugin.sendPlayerToServer(player, "hub");
				} else if(arguments.length == 1) {
					ProPlugin.sendPlayerToServer(player, "hub" + arguments[0]);
				}
				return true;
			}
		};
		
		new CommandBase("list") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(Bukkit.getOnlinePlayers().isEmpty()) {
					MessageHandler.sendMessage(sender, "&cThere are no players online");
				} else {
					String players = "";
					int online = 0;
					for(Player player : ProPlugin.getPlayers()) {
						players += AccountHandler.getRank(player).getColor() + player.getName() + ", ";
						++online;
					}
					MessageHandler.sendMessage(sender, online + " Players: " + players.substring(0, players.length() - 2));
					if(OSTB.getMiniGame() != null && SpectatorHandler.isEnabled() && SpectatorHandler.getNumberOf() > 0) {
						String spectators = "";
						online = 0;
						for(Player player : SpectatorHandler.getPlayers()) {
							if(!Ranks.isStaff(player)) {
								spectators += AccountHandler.getRank(player).getColor() + player.getName() + ", ";
								++online;
							}
						}
						MessageHandler.sendMessage(sender, "Spectators (&e" + online + "&a): " + spectators.substring(0, spectators.length() - 2));
					}
				}
				return true;
			}
		};
		
		new CommandBase("say", -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String message = "";
				for(String argument : arguments) {
					message += argument + " ";
				}
				message = ChatColor.GREEN + StringUtil.color(message.substring(0, message.length() - 1));
				Bukkit.getLogger().info(message);
				for(Player player : Bukkit.getOnlinePlayers()) {
					player.sendMessage(message);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
}
