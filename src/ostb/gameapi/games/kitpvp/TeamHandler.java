package ostb.gameapi.games.kitpvp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.minecraft.server.v1_8_R3.EnumColor;
import ostb.OSTB;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class TeamHandler implements Listener {
	private List<Team> teams = null;
	private Map<Team, Integer> scores = null;
	private Team redTeam = null;
	private Team blueTeam = null;
	private Team yellowTeam = null;
	private Team greenTeam = null;
	private String invName = null;
	
	public TeamHandler() {
		teams = new ArrayList<Team>();
		scores = new HashMap<Team, Integer>();
		Scoreboard scoreboard = OSTB.getScoreboard();
		redTeam = scoreboard.registerNewTeam("red");
		redTeam.setPrefix(ChatColor.RED + "[Red] ");
		teams.add(redTeam);
		blueTeam = scoreboard.registerNewTeam("blue");
		blueTeam.setPrefix(ChatColor.AQUA + "[Blue] ");
		teams.add(blueTeam);
		yellowTeam = scoreboard.registerNewTeam("yellow");
		yellowTeam.setPrefix(ChatColor.YELLOW + "[Yellow] ");
		teams.add(yellowTeam);
		greenTeam = scoreboard.registerNewTeam("green");
		greenTeam.setPrefix(ChatColor.GREEN + "[Green] ");
		teams.add(greenTeam);
		for(Team team : teams) {
			team.setAllowFriendlyFire(false);
		}
		invName = "Team Selection";
		EventUtil.register(this);
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	private void openTeamSelection(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, invName);
		inventory.setItem(10, new ItemCreator(Material.WOOL, EnumColor.RED.getColorIndex()).setName(redTeam.getPrefix()).setLores(new String [] {"", "&7Players: &e" + redTeam.getSize(), ""}).getItemStack());
		inventory.setItem(12, new ItemCreator(Material.WOOL, EnumColor.LIGHT_BLUE.getColorIndex()).setName(blueTeam.getPrefix()).setLores(new String [] {"", "&7Players: &e" + blueTeam.getSize(), ""}).getItemStack());
		inventory.setItem(14, new ItemCreator(Material.WOOL, EnumColor.YELLOW.getColorIndex()).setName(yellowTeam.getPrefix()).setLores(new String [] {"", "&7Players: &e" + yellowTeam.getSize(), ""}).getItemStack());
		inventory.setItem(16, new ItemCreator(Material.WOOL, EnumColor.LIME.getColorIndex()).setName(greenTeam.getPrefix()).setLores(new String [] {"", "&7Players: &e" + greenTeam.getSize(), ""}).getItemStack());
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(invName)) {
			Player player = event.getPlayer();
			int slot = event.getSlot();
			if(slot == 10) {
				redTeam.addPlayer(player);
			} else if(slot == 12) {
				blueTeam.addPlayer(player);
			} else if(slot == 14) {
				yellowTeam.addPlayer(player);
			} else if(slot == 16) {
				greenTeam.addPlayer(player);
			}
			SpectatorHandler.remove(player);
			event.setCancelled(true);
			player.closeInventory();
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			for(Team team : teams) {
				if(team.hasPlayer(player)) {
					return;
				}
			}
			openTeamSelection(player);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PREMIUM.hasRank(player)) {
			SpectatorHandler.add(player);
			openTeamSelection(player);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player killer = event.getKiller();
		if(killer != null) {
			for(Team team : teams) {
				if(team.hasPlayer(killer)) {
					int score = 0;
					if(scores.containsKey(team)) {
						score = scores.get(team);
					}
					scores.put(team, ++score);
				}
			}
			//TODO: Update the scoreboard
		}
	}
}
