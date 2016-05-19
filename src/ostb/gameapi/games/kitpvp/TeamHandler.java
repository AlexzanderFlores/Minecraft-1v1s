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
import org.bukkit.inventory.ItemStack;
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
	private Map<Team, Integer> scores = null;
	private Team redTeam = null;
	private Team blueTeam = null;
	private Team yellowTeam = null;
	private Team greenTeam = null;
	private String invName = null;
	
	public enum KitTeam {
		RED("Red", ChatColor.RED, EnumColor.RED),
		BLUE("Blue", ChatColor.AQUA, EnumColor.LIGHT_BLUE),
		YELLOW("Yellow", ChatColor.YELLOW, EnumColor.YELLOW),
		GREEN("Green", ChatColor.GREEN, EnumColor.LIME);
		
		private Team team = null;
		private EnumColor woolColor = null;
		
		private KitTeam(String prefix, ChatColor color, EnumColor woolColor) {
			team = OSTB.getScoreboard().registerNewTeam(prefix);
			team.setPrefix(color + "[" + prefix + "] ");
			team.setAllowFriendlyFire(false);
			this.woolColor = woolColor;
		}
		
		private String getName() {
			String prefix = team.getPrefix();
			return prefix.substring(0, prefix.length() - 1);
		}
		
		private String [] getLores() {
			return new String [] {"", "&7Players: &e" + team.getSize(), ""};
		}
		
		public ItemStack getIcon() {
			return new ItemCreator(Material.WOOL, woolColor.getColorIndex()).setName(getName()).setLores(getLores()).getItemStack();
		}
	}
	
	public TeamHandler() {
		scores = new HashMap<Team, Integer>();
		for(KitTeam team : KitTeam.values()) {
			scores.put(team.team, 0);
		}
		invName = "Team Selection";
		EventUtil.register(this);
	}
	
	public List<Team> getTeams() {
		return new ArrayList<Team>(scores.keySet());
	}
	
	public Team getTeam(Player player) {
		for(Team team : getTeams()) {
			if(team.hasPlayer(player)) {
				return team;
			}
		}
		return null;
	}
	
	private void openTeamSelection(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, invName);
		for(int a = 0, slot = 10; a < KitTeam.values().length; ++a, slot += 2) {
			inventory.setItem(slot, KitTeam.values()[a].getIcon());
		}
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
			for(Team team : getTeams()) {
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
			for(Team team : getTeams()) {
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
