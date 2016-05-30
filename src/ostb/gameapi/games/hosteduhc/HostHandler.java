package ostb.gameapi.games.hosteduhc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.gameapi.MiniGame.GameStates;
import ostb.gameapi.SpectatorHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;
import ostb.staff.mute.MuteHandler;

@SuppressWarnings("deprecation")
public class HostHandler implements Listener {
    private static List<UUID> hosts = null;
    private static List<String> prefixes = null;
    private static UUID mainHost = null;
    private static ItemStack center = null;
    private static String name = null;

    public HostHandler() {
        hosts = new ArrayList<UUID>();
        prefixes = new ArrayList<String>();
        center = new ItemCreator(Material.COMPASS).setName("&aTeleport to &e0, 0").getItemStack();
        name = "World Selection";
        new CommandBase("host", 0, 4) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                if (arguments.length == 0 && sender instanceof Player) {
                    Player player = (Player) sender;
                    if (isHost(player.getUniqueId())) {
                        mainHost = player.getUniqueId();
                        MessageHandler.sendMessage(player, "You've set yourself as the main host for this game");
                        return true;
                    }
                }
                if (arguments.length == 0 || (arguments.length == 1 && arguments[0].equalsIgnoreCase("help"))) {
                    sendHelpMenu(sender);
                } else if (arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
                    if (hosts == null || hosts.isEmpty()) {
                        MessageHandler.sendMessage(sender, "&cThere are currently no hosts");
                    } else {
                        String message = "";
                        for (String prefix : prefixes) {
                            message += prefix + ", ";
                        }
                        message = message.substring(0, message.length() - 2);
                        MessageHandler.sendMessage(sender, "Current Hosts: (&e" + hosts.size() + "&a) " + message);
                    }
                } else if (arguments.length >= 1 && arguments[0].equalsIgnoreCase("team")) {
                    if (arguments.length == 3 && arguments[1].equalsIgnoreCase("list")) {
                        String name = arguments[2];
                        Team team = TeamHandler.getTeam(name);
                        if (team == null) {
                            MessageHandler.sendMessage(sender, "&c" + name + " is not in a team");
                        } else {
                            String message = "";
                            for (OfflinePlayer offlinePlayer : team.getPlayers()) {
                                message += offlinePlayer.getName() + ", ";
                            }
                            MessageHandler.sendMessage(sender, message.substring(0, message.length() - 2));
                        }
                    } else if (arguments.length == 4 && arguments[1].equalsIgnoreCase("add")) {
                        Player leader = ProPlugin.getPlayer(arguments[2]);
                        if (leader == null) {
                            MessageHandler.sendMessage(sender, "&c" + arguments[2] + " is not online");
                        } else {
                            Player member = ProPlugin.getPlayer(arguments[3]);
                            if (member == null) {
                                MessageHandler.sendMessage(sender, "&c" + arguments[3] + " is not online");
                            } else {
                                Team team = TeamHandler.getTeam(leader);
                                if (team == null) {
                                    if (OSTB.getScoreboard().getTeam(leader.getName()) == null) {
                                        team = OSTB.getScoreboard().registerNewTeam(leader.getName());
                                    } else {
                                        team = OSTB.getScoreboard().getTeam(leader.getName());
                                    }
                                    team.setAllowFriendlyFire(false);
                                    team.addPlayer(leader);
                                    team.addPlayer(member);
                                    TeamHandler.setTeam(leader, team);
                                    TeamHandler.setTeam(member, team);
                                } else {
                                    team.addPlayer(member);
                                    TeamHandler.teamChat(member, "has joined the team");
                                    TeamHandler.setTeam(member, team);
                                }
                                MessageHandler.sendMessage(sender, "Added " + member.getName() + " to " + leader.getName() + "'s team");
                            }
                        }
                    } else if (arguments.length == 3 && arguments[1].equalsIgnoreCase("remove")) {
                        TeamHandler.removeFromTeam(arguments[2]);
                        MessageHandler.sendMessage(sender, "Removed " + arguments[2] + " from their team");
                    } else {
                        MessageHandler.sendMessage(sender, "/host team list");
                        MessageHandler.sendMessage(sender, "/host team list <player name>");
                        MessageHandler.sendMessage(sender, "/host team add <player one> <player two>");
                        MessageHandler.sendMessage(sender, "/host team remove <player>");
                    }
                }
                return true;
            }
        };
        new CommandBase("helpop", 1, -1, true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if (SpectatorHandler.contains(player)) {
                	MessageHandler.sendMessage(player, "&cYou cannot run this command as a spectator");
                } else if (MuteHandler.checkMute(player)) {
                	MessageHandler.sendMessage(player, "&cYou cannot run this command while muted");
                } else {
                    String msg = "";
                    for (String argument : arguments) {
                        msg += argument + " ";
                    }
                    if (!QuestionAnswerer.askQuestion(player, msg)) {
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            if (Ranks.OWNER.hasRank(online) || HostHandler.isHost(online.getUniqueId())) {
                                MessageHandler.sendMessage(online, "");
                                MessageHandler.sendMessage(online, "&cHelpop: &f" + AccountHandler.getPrefix(player) + "&f: " + msg);
                                MessageHandler.sendMessage(online, "");
                            }
                        }
                        MessageHandler.sendMessage(player, "&cHelpop: &f" + AccountHandler.getPrefix(player) + "&f: " + msg);
                    }
                }
                return true;
            }
        }.enableDelay(5);
        new CommandBase("tele", 1, 2) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!isHost(player.getUniqueId()) && !Ranks.OWNER.hasRank(player)) {
                        MessageHandler.sendUnknownCommand(player);
                        return true;
                    }
                }
                String name = arguments[0];
                if (arguments.length == 1) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        Player target = ProPlugin.getPlayer(name);
                        if (target == null) {
                            MessageHandler.sendMessage(sender, "&c" + name + " is not online");
                        } else {
                            player.teleport(target);
                        }
                    } else {
                        MessageHandler.sendUnknownCommand(sender);
                    }
                } else {
                    Player playerOne = ProPlugin.getPlayer(name);
                    if (playerOne == null) {
                        MessageHandler.sendMessage(sender, "&c" + name + " is not online");
                    } else {
                        String nameTwo = arguments[1];
                        Player playerTwo = ProPlugin.getPlayer(nameTwo);
                        if (playerTwo == null) {
                            MessageHandler.sendMessage(sender, "&c" + nameTwo + " is not online");
                        } else {
                            playerOne.teleport(playerTwo);
                            MessageHandler.sendMessage(sender, "You have teleported " + AccountHandler.getPrefix(playerOne) + " &ato " + AccountHandler.getPrefix(playerTwo));
                        }
                    }
                }
                return true;
            }
        };
        EventUtil.register(this);
    }

    public static Player getMainHost() {
        return Bukkit.getPlayer(mainHost);
    }

    public static boolean isHost(UUID uuid) {
        return hosts != null && hosts.contains(uuid);
    }

    private void sendHelpMenu(CommandSender sender) {
        MessageHandler.sendLine(sender);
        MessageHandler.sendMessage(sender, "Host Commands:");
        MessageHandler.sendMessage(sender, "");
        MessageHandler.sendMessage(sender, "/host &eDisplays commands");
        MessageHandler.sendMessage(sender, "/host help &eDisplays commands");
        MessageHandler.sendMessage(sender, "/host list &eLists all current hosts");
        MessageHandler.sendMessage(sender, "/s <text> &eTalks in staff/host chat");
        MessageHandler.sendLine(sender);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Ranks.isStaff(event.getPlayer()) || isHost(event.getPlayer().getUniqueId())) {
            String name = AccountHandler.getPrefix(event.getPlayer());
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (Ranks.isStaff(player) || isHost(player.getUniqueId())) {
                    MessageHandler.sendMessage(player, "&bStaff: " + name + " has joined this server");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.isCancelled() && event.getPlayer().getUniqueId() == mainHost) {
            event.setFormat(ChatColor.DARK_RED + "[Host] " + event.getFormat());
        }
    }

    @EventHandler
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
        if (!event.isCancelled() && event.getState() == SpectatorState.ADDED) {
            Player player = event.getPlayer();
            if (isHost(player.getUniqueId()) && OSTB.getMiniGame().getGameState() != GameStates.STARTED) {
                final String name = player.getName();
                new AsyncDelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        Player player = ProPlugin.getPlayer(name);
                        if (player != null) {
                            player.getInventory().addItem(center);
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if (ItemUtil.isItem(player.getItemInHand(), center)) {
            if (OptionsHandler.isNetherEnabled()) {
                Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
                inventory.setItem(11, new ItemCreator(Material.GRASS).setName("&aTeleport to &eWorld").getItemStack());
                inventory.setItem(15, new ItemCreator(Material.NETHERRACK).setName("&aTeleport to &cNether").getItemStack());
                player.openInventory(inventory);
            } else {
                player.teleport(WorldHandler.getWorld().getSpawnLocation());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if (event.getTitle().equals(name)) {
            Player player = event.getPlayer();
            Material type = event.getItem().getType();
            if (type == Material.GRASS) {
                player.teleport(WorldHandler.getWorld().getSpawnLocation());
            } else if (type == Material.NETHERRACK) {
                player.teleport(WorldHandler.getNether().getSpawnLocation());
            }
            player.closeInventory();
            event.setCancelled(true);
        }
    }
}
