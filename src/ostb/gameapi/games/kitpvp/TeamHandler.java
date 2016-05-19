package ostb.gameapi.games.kitpvp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import anticheat.events.PlayerLeaveEvent;
import anticheat.events.TimeEvent;
import net.minecraft.server.v1_8_R3.EnumColor;
import ostb.OSTB;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PostPlayerJoinEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.kitpvp.events.TeamSelectEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class TeamHandler implements Listener {
	private String invName = null;
	
	public enum KitTeam {
		RED("Red", ChatColor.RED, EnumColor.RED),
		BLUE("Blue", ChatColor.AQUA, EnumColor.LIGHT_BLUE),
		YELLOW("Yellow", ChatColor.YELLOW, EnumColor.YELLOW),
		GREEN("Green", ChatColor.GREEN, EnumColor.LIME);
		
		private Team team = null;
		private ChatColor color = null;
		private EnumColor woolColor = null;
		private int score = 0;
		
		private KitTeam(String prefix, ChatColor color, EnumColor woolColor) {
			team = OSTB.getScoreboard().registerNewTeam(prefix);
			team.setPrefix(color + "[" + prefix + "] ");
			team.setAllowFriendlyFire(false);
			this.color = color;
			this.woolColor = woolColor;
		}
		
		public boolean isOnTeam(Player player) {
			return team.hasPlayer(player);
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
		
		public int getSize() {
			return team.getSize();
		}
		
		public String getSizeString() {
			return color + "" + getSize();
		}
		
		public void add(Player player) {
			for(KitTeam kitTeam : values()) {
				kitTeam.team.removePlayer(player);
			}
			team.addPlayer(player);
			String name = color + player.getName();
			if(name.length() > 16) {
				name = name.substring(0, 16);
			}
			player.setPlayerListName(name);
			MessageHandler.sendMessage(player, "You have joined the " + getName() + " &xteam");
		}
		
		public void remove(Player player) {
			team.removePlayer(player);
		}
		
		public void sendMessage(String message) {
			for(OfflinePlayer offlinePlayer : team.getPlayers()) {
				if(offlinePlayer.isOnline() && offlinePlayer instanceof Player) {
					Player player = (Player) offlinePlayer;
					MessageHandler.sendMessage(player, message);
				}
			}
		}
		
		public void incrementScore() {
			++score;
		}
		
		public void removeScore(int toRemove) {
			score -= toRemove;
		}
		
		public void clearScore() {
			score = 0;
		}
		
		public int getScore() {
			return score;
		}
		
		public String getScoreString() {
			return color + "" + getScore();
		}
	}
	
	public TeamHandler() {
		invName = "Team Selection";
		EventUtil.register(this);
	}
	
	public List<Team> getTeams() {
		List<Team> teams = new ArrayList<Team>();
		for(KitTeam kitTeam : KitTeam.values()) {
			teams.add(kitTeam.team);
		}
		return teams;
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
				KitTeam.RED.add(player);
			} else if(slot == 12) {
				KitTeam.BLUE.add(player);
			} else if(slot == 14) {
				KitTeam.YELLOW.add(player);
			} else if(slot == 16) {
				KitTeam.GREEN.add(player);
			}
			SpectatorHandler.remove(player);
			event.setCancelled(true);
			player.closeInventory();
			Bukkit.getPluginManager().callEvent(new TeamSelectEvent(player));
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(Player player : SpectatorHandler.getPlayers()) {
				InventoryView view = player.getOpenInventory();
				if(view == null || !view.getTitle().equals(invName)) {
					openTeamSelection(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PREMIUM.hasRank(player)) {
			SpectatorHandler.add(player);
			openTeamSelection(player);
		} else {
			KitTeam bestTeam = null;
			for(KitTeam kitTeam : KitTeam.values()) {
				if(bestTeam == null || kitTeam.getSize() <= bestTeam.getSize()) {
					bestTeam = kitTeam;
				}
			}
			bestTeam.add(player);
			Bukkit.getPluginManager().callEvent(new TeamSelectEvent(player));
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player killer = event.getKiller();
		if(killer != null && !Events.getPaused()) {
			for(KitTeam kitTeam : KitTeam.values()) {
				if(kitTeam.isOnTeam(killer)) {
					kitTeam.incrementScore();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		for(KitTeam kitTeam : KitTeam.values()) {
			kitTeam.remove(event.getPlayer());
		}
	}
}