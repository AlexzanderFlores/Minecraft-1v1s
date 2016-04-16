package ostb.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.server.CommandBase;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class TeamHandler implements Listener {
	private List<Team> teams = null;
	private List<String> colors = null;
	private String name = null;
	
	public TeamHandler() {
		teams = new ArrayList<Team>();
		colors = new ArrayList<String>();
		colors.add("&1");
		colors.add("&2");
		colors.add("&3");
		colors.add("&4");
		colors.add("&5");
		colors.add("&6");
		colors.add("&9");
		colors.add("&a");
		colors.add("&b");
		colors.add("&c");
		colors.add("&d");
		colors.add("&e");
		name = "Team Invite - ";
		new CommandBase("team", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(player, "Team commands:");
					MessageHandler.sendMessage(player, "/team help &eDisplays help");
					MessageHandler.sendMessage(player, "/team list &eDisplays your team's players");
					MessageHandler.sendMessage(player, "/team leave &eLeave your team");
					MessageHandler.sendMessage(player, "/team <name> &eInvite a player to your team");
				} else if(arguments[0].equalsIgnoreCase("list")) {
					Team team = getTeam(player);
					if(team == null) {
						MessageHandler.sendMessage(player, "&cYou are not in a team &x/team help");
					} else {
						MessageHandler.sendMessage(player, "Players on your team:");
						for(OfflinePlayer offlinePlayer : team.getPlayers()) {
							MessageHandler.sendMessage(player, offlinePlayer.getName());
						}
					}
				} else if(arguments[0].equalsIgnoreCase("leave")) {
					Team team = getTeam(player);
					if(team == null) {
						MessageHandler.sendMessage(player, "&cYou are not in a team &x/team help");
					} else {
						remove(player);
					}
				} else {
					String name = arguments[0];
					Player target = ProPlugin.getPlayer(name);
					if(target == null) {
						MessageHandler.sendMessage(player, "&c" + name + " is not online");
					} else {
						Inventory inventory = Bukkit.createInventory(target, 9 * 3, name + player.getName());
						inventory.setItem(11, new ItemCreator(Material.WOOL, 5).setName("&aAccept").getItemStack());
						inventory.setItem(14, new ItemCreator(Material.WOOL, 14).setName("&cDeny").getItemStack());
						target.openInventory(inventory);
					}
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	private void alert(Team team, String alert) {
		for(OfflinePlayer offlinePlayer : team.getPlayers()) {
			if(offlinePlayer.isOnline()) {
				Player onlinePlayer = (Player) offlinePlayer;
				MessageHandler.sendMessage(onlinePlayer, alert);
			}
		}
	}
	
	private Team getTeam(Player player) {
		for(Team team : teams) {
			if(team.hasPlayer(player)) {
				return team;
			}
		}
		return null;
	}
	
	private void remove(Player player) {
		Team team = getTeam(player);
		if(team != null) {
			alert(team, "&c" + player.getName() + " has left the team");
			team.removePlayer(player);
			if(team.getSize() <= 1) {
				alert(team, "&cTeam deleted");
				colors.add(team.getPrefix());
				team.unregister();
				teams.remove(team);
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith(name)) {
			Player player = event.getPlayer();
			if(event.getSlot() == 11) {
				String [] split = event.getTitle().split(" ");
				String senderName = split[split.length - 1];
				Player sender = ProPlugin.getPlayer(senderName);
				if(sender == null) {
					MessageHandler.sendMessage(player, "&c" + senderName + " is no longer online");
				} else {
					Team team = OSTB.getScoreboard().registerNewTeam(senderName);
					team.addPlayer(sender);
					team.addPlayer(player);
					team.setAllowFriendlyFire(false);
					team.setPrefix(StringUtil.color(colors.get(0)));
					colors.remove(0);
					alert(team, senderName + " has joined the team");
					alert(team, player.getName() + " has joined the team");
				}
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
