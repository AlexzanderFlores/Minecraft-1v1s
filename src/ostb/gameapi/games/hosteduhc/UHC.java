package ostb.gameapi.games.hosteduhc;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.GoldenHeadUtil;
import ostb.gameapi.MiniGame;
import ostb.gameapi.SkullPikeUtil;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.hosteduhc.anticheat.AntiIPVP;
import ostb.gameapi.games.hosteduhc.anticheat.CommandSpy;
import ostb.gameapi.games.hosteduhc.anticheat.DiamondTracker;
import ostb.gameapi.games.hosteduhc.anticheat.StripMineDetection;
import ostb.gameapi.games.hosteduhc.anticheat.WaterBucketLogging;
import ostb.gameapi.scenarios.Scenario;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.ChatClickHandler;
import ostb.server.CommandBase;
import ostb.server.Tweeter;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.FileHandler;
import ostb.server.util.StringUtil;

public class UHC extends MiniGame {
    public static String ultraGoldenApple = null;
    public static String account = null;

    public UHC() {
        super("UHC");
        account = "ProMcHostedUHC";
        OSTB.setSidebar(new SidebarScoreboardUtil(" &aUHC "));
        setRequiredPlayers(60);
        setVotingCounter(-1);
        setStartingCounter(60 * 3 + 30);
        setEndingCounter(20);
        setAutoJoin(false);
        setCanJoinWhileStarting(true);
        setResetPlayerUponJoining(true);
        setRestartWithOnePlayerLeft(false);
        setDoDaylightCycle(true);
        new Events();
        new HostHandler();
        new WhitelistHandler();
        new ScenarioManager();
        new TeamHandler();
        new OptionsHandler();
        new DiamondTracker();
        new WorldHandler();
        new AntiIPVP();
        new HealthHandler();
        new StripMineDetection();
        new BelowNameHealthScoreboardUtil();
        new QuestionAnswerer();
        new WaterBucketLogging();
        new CommandSpy();
        new HostedEvent();
        if (HostedEvent.isEvent()) {
            OSTB.getSidebar().setText(new String[]{
                    " ",
                    "&5EliteUHC"
            }, -1);
        } else {
            OSTB.getSidebar().setText(new String[]{
                    " ",
                    "&6Info:",
                    "&b/info",
                    "  ",
                    "&6Rules:",
                    "&b/rules"
            }, -1);
        }
        //UHC:
        new Tweeter("Ze778QTP0SuPDXoGPaIqqGekB", "KfOVLJBl2r14HliWG2t52fSXMPx2lRhDJEQ749pSqS2VbDxDVT", "719686159102816257-ANGTIPu15z9MCyxoynHnHrm3IFdQwZ3", "YiN7VgjPYjfuulKdJwcqRnBNDT5TrJ7sSd2zrK5K65axA");
        new CommandBase("heal", 0, 1) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!HostHandler.isHost(player.getUniqueId())) {
                        MessageHandler.sendUnknownCommand(sender);
                        return true;
                    }
                }
                String target = null;
                if (arguments.length == 0) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        target = player.getName();
                    } else {
                        MessageHandler.sendPlayersOnly(sender);
                        return true;
                    }
                } else if (arguments.length == 1) {
                    target = arguments[0];
                }
                if (target.equalsIgnoreCase("all")) {
                    for (Player player : ProPlugin.getPlayers()) {
                        heal(player);
                    }
                    MessageHandler.sendMessage(sender, "You've healed all players");
                } else {
                    Player player = ProPlugin.getPlayer(target);
                    if (player == null) {
                        MessageHandler.sendMessage(player, "&c" + target + " is not online");
                    } else {
                        heal(player);
                        MessageHandler.sendMessage(sender, "You've healed " + AccountHandler.getPrefix(player));
                    }
                }
                HealthHandler.updateHealth();
                return true;
            }
        };
        new CommandBase("feed", 0, 1) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!HostHandler.isHost(player.getUniqueId())) {
                        MessageHandler.sendUnknownCommand(sender);
                        return true;
                    }
                }
                String target = null;
                if (arguments.length == 0) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        target = player.getName();
                    } else {
                        MessageHandler.sendPlayersOnly(sender);
                        return true;
                    }
                } else if (arguments.length == 1) {
                    target = arguments[0];
                }
                if (target.equalsIgnoreCase("all")) {
                    for (Player player : ProPlugin.getPlayers()) {
                        feed(player);
                    }
                    MessageHandler.sendMessage(sender, "You've fed all players");
                } else {
                    Player player = ProPlugin.getPlayer(target);
                    if (player == null) {
                        MessageHandler.sendMessage(player, "&c" + target + " is not online");
                    } else {
                        feed(player);
                        MessageHandler.sendMessage(sender, "You've fed " + AccountHandler.getPrefix(player));
                    }
                }
                return true;
            }
        };
        new CommandBase("invSee", 1, true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if (HostHandler.isHost(player.getUniqueId()) || Ranks.isStaff(player)) {
                    if (SpectatorHandler.contains(player)) {
                        Player target = ProPlugin.getPlayer(arguments[0]);
                        if (target == null) {
                            MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
                        } else {
                            player.openInventory(target.getInventory());
                        }
                    } else {
                        MessageHandler.sendMessage(player, "&cYou must be a spectator to run this command");
                    }
                } else {
                    MessageHandler.sendUnknownCommand(sender);
                }
                return true;
            }
        };
        new CommandBase("info") {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                String scenarios = "";
                for (Scenario scenario : ScenarioManager.getActiveScenarios()) {
                    scenarios += scenario.getName() + ", ";
                }
                scenarios = scenarios.substring(0, scenarios.length() - 2);
                MessageHandler.sendLine(sender);
                MessageHandler.sendMessage(sender, "Scenario: &b" + scenarios);
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK FOR SCENARIO INFO", "Click for info", "/sinfo");
                }
                MessageHandler.sendMessage(sender, "Team Size: &b" + (TeamHandler.getMaxTeamSize() == 1 ? "Solo" : TeamHandler.getMaxTeamSize()));
                MessageHandler.sendMessage(sender, "Is Rush: &b" + OptionsHandler.isRush());
                MessageHandler.sendMessage(sender, "Nether Enabled: &b" + OptionsHandler.isNetherEnabled());
                MessageHandler.sendMessage(sender, "Apple Rates: &b" + OptionsHandler.getAppleRates() + "&a%");
                MessageHandler.sendMessage(sender, "Horses Enabled: &b" + OptionsHandler.allowHorses());
                MessageHandler.sendMessage(sender, "Horse Healing Enabled: &b" + OptionsHandler.allowHorseHealing());
                MessageHandler.sendMessage(sender, "Notch Apples Enabled: &b" + OptionsHandler.allowNotchApples());
                MessageHandler.sendMessage(sender, "Team damage: &bFalse");
                MessageHandler.sendMessage(sender, "Ender Pearl damage: &b" + OptionsHandler.allowPearlDamage());
                MessageHandler.sendMessage(sender, "Absorption: &b" + OptionsHandler.getAbsorption());
                MessageHandler.sendLine(sender);
                return true;
            }
        };
        new CommandBase("sinfo") {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                for (Scenario scenario : ScenarioManager.getActiveScenarios()) {
                    MessageHandler.sendMessage(sender, "");
                    String info = scenario.getInfo();
                    if (info == null) {
                        MessageHandler.sendMessage(sender, "&cNo info to display at this time for " + scenario.getName());
                    } else {
                        MessageHandler.sendMessage(sender, "&b&l" + scenario.getName() + " &e" + info);
                    }
                    MessageHandler.sendMessage(sender, "");
                }
                return true;
            }
        };
        new CommandBase("uhcKick", 2, -1, true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if (HostHandler.isHost(player.getUniqueId()) || Ranks.OWNER.hasRank(player)) {
                    Player target = ProPlugin.getPlayer(arguments[0]);
                    if (target == null) {
                        MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
                    } else {
                        String reason = "";
                        for (int a = 1; a < arguments.length; ++a) {
                            reason += arguments[a] + " ";
                        }
                        MessageHandler.alert(AccountHandler.getPrefix(target) + " &cwas kicked: " + reason);
                        target.kickPlayer(ChatColor.RED + reason);
                    }
                } else {
                    MessageHandler.sendUnknownCommand(player);
                }
                return true;
            }
        };
        new CommandBase("uhcKill", 2, -1) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if (HostHandler.isHost(player.getUniqueId()) || Ranks.OWNER.hasRank(player)) {
                    Player target = ProPlugin.getPlayer(arguments[0]);
                    if (target == null) {
                        MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
                    } else {
                        String reason = "";
                        for (int a = 1; a < arguments.length; ++a) {
                            reason += arguments[a] + " ";
                        }
                        MessageHandler.alert(AccountHandler.getPrefix(target) + " &cwas killed: " + reason);
                        target.setHealth(0.0d);
                    }
                } else {
                    MessageHandler.sendUnknownCommand(player);
                }
                return true;
            }
        };
        new DelayedTask(new Runnable() {
            @Override
            public void run() {
                new CommandBase("rules") {
                    @Override
                    public boolean execute(CommandSender sender, String[] arguments) {
                        MessageHandler.sendLine(sender);
                        MessageHandler.sendMessage(sender, "Strip Mining: &cNOT ALLOWED");
                        MessageHandler.sendMessage(sender, "Stair Casing: ONLY START ABOVE Y 32");
                        MessageHandler.sendMessage(sender, "Roller Coastering: ONLY START ABOVE AND COME BACK TO Y 32");
                        MessageHandler.sendMessage(sender, "Mining to Coordinates: &cNOT ALLOWED");
                        MessageHandler.sendMessage(sender, "iPVP: &cNOT ALLOWED");
                        MessageHandler.sendMessage(sender, "Cross Teaming: " + (OptionsHandler.getCrossTeaming() ? "&eALLOWED" : "&cNOT ALLOWED"));
                        MessageHandler.sendMessage(sender, "Spoiling While Spectating: &cNOT ALLOWED");
                        MessageHandler.sendMessage(sender, "Sky Basing: &eONLY BEFORE MEET UP");
                        MessageHandler.sendMessage(sender, "Ground Hogging: &eONLY BEFORE MEET UP");
                        MessageHandler.sendMessage(sender, "Poke holing: &eALLOWED");
                        MessageHandler.sendMessage(sender, "Mining to Sounds: &eALLOWED");
                        MessageHandler.sendMessage(sender, "Stalking: &eALLOWED");
                        MessageHandler.sendMessage(sender, "&bHave a Question? &c/helpop <question>");
                        MessageHandler.sendLine(sender);
                        return true;
                    }
                };
                new CommandBase("s", 1, -1, true) {
                    @Override
                    public boolean execute(CommandSender sender, String[] arguments) {
                        Player player = (Player) sender;
                        if (Ranks.OWNER.hasRank(player) || HostHandler.isHost(player.getUniqueId())) {
                            String name = AccountHandler.getPrefix(sender);
                            String message = "";
                            for (String argument : arguments) {
                                message += argument + " ";
                            }
                            for (Player online : Bukkit.getOnlinePlayers()) {
                                if (Ranks.isStaff(online) || HostHandler.isHost(online.getUniqueId())) {
                                    MessageHandler.sendMessage(online, "&bStaff: " + name + ": " + StringUtil.color(message.substring(0, message.length() - 1)));
                                }
                            }
                        } else {
                            MessageHandler.sendUnknownCommand(player);
                        }
                        return true;
                    }
                };
            }
        });
        new GoldenHeadUtil();
        new SkullPikeUtil();
    }

    @Override
    public void disable() {
        super.disable();
        String container = Bukkit.getWorldContainer().getPath();
        Bukkit.unloadWorld(getLobby(), false);
        File newWorld = new File(container + "/../resources/maps/uhc");
        if (newWorld.exists() && newWorld.isDirectory()) {
            FileHandler.delete(new File(container + "/lobby"));
            FileHandler.copyFolder(newWorld, new File(container + "/lobby"));
        }
    }

    private void heal(Player player) {
        player.setHealth(player.getMaxHealth());
        feed(player);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void feed(Player player) {
        player.setFoodLevel(20);
    }
}
