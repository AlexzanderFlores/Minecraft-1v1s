package ostb.gameapi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class TeamHandler implements Listener {
	private List<Team> teams = null;
	private boolean enableTeamSelectorItem = false;
	private ItemStack item = null;
	private String name = null;
	
	public TeamHandler() {
		teams = new ArrayList<Team>();
		name = "Team Selector";
		item = new ItemCreator(Material.WOOL).setName("&a" + name).getItemStack();
		EventUtil.register(this);
	}
	
	public List<Team> getTeams() {
		return teams;
	}
	
	public Team addTeam(String name) {
		Team team = OSTB.getScoreboard().registerNewTeam(name);
		teams.add(team);
		return team;
	}
	
	public void removeTeam(String team) {
		if(teams.contains(team)) {
			teams.remove(team);
			OSTB.getScoreboard().getTeam(team).unregister();
		}
	}
	
	public Team getTeam(String name) {
		for(Team team : teams) {
			if(team.getName().equals(name)) {
				return team;
			}
		}
		return null;
	}
	
	public Team getTeam(Player player) {
		for(Team team : teams) {
			if(isOnTeam(player, team)) {
				return team;
			}
		}
		return null;
	}
	
	public boolean isOnTeam(Player player, Team team) {
		return team.hasPlayer(player);
	}
	
	public boolean isOnSameTeam(Player playerOne, Player playerTwo) {
		return getTeam(playerOne) == getTeam(playerTwo);
	}
	
	public List<Player> getPlayers(Team team) {
		List<Player> players = new ArrayList<Player>();
		for(OfflinePlayer offlinePlayer : team.getPlayers()) {
			if(offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				players.add(player);
			}
		}
		return players;
	}
	
	public boolean getEnableTeamItem() {
		return enableTeamSelectorItem;
	}
	
	public void toggleTeamItem() {
		enableTeamSelectorItem = !enableTeamSelectorItem;
	}
	
	public void setTeam(Player player, Team newTeam) {
		for(Team team : teams) {
			team.removePlayer(player);
		}
		newTeam.addPlayer(player);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(enableTeamSelectorItem && OSTB.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			player.getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PREMIUM.hasRank(player)) {
			ItemStack item = player.getItemInHand();
			if(item != null && item.equals(this.item)) {
				Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
				inventory.setItem(11, new ItemCreator(Material.WOOL, DyeColor.RED.getData()).setName("&cRed Team").getItemStack());
				inventory.setItem(15, new ItemCreator(Material.WOOL, DyeColor.BLUE.getData()).setName("&bBlue Team").getItemStack());
				player.openInventory(inventory);
			}
		} else {
			MessageHandler.sendMessage(player, Ranks.PREMIUM.getNoPermission());
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			byte data = event.getItem().getData().getData();
			Team team = null;
			if(data == DyeColor.RED.getData()) {
				team = getTeam("red");
			} else if(data == DyeColor.BLUE.getData()) {
				team = getTeam("blue");
			}
			if(team == null) {
				MessageHandler.sendMessage(player, "&cError: team not found, please report this");
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else if(team.hasPlayer(player)) {
				new TitleDisplayer(player, "&cAlready on the", team.getPrefix() + " &eTeam").display();
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			} else {
				new TitleDisplayer(player, "&eYou joined the", team.getPrefix() + " &eTeam").display();
				MessageHandler.sendMessage(player, "You joined the " + team.getPrefix() + " &xteam");
				setTeam(player, team);
				EffectUtil.playSound(player, Sound.LEVEL_UP);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
