package ostb.server.servers.hub.items;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.player.LevelHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.PlaytimeTracker;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.account.PlaytimeTracker.TimeType;
import ostb.server.DB;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class Profile extends HubItemBase {
	private static List<String> delayed = null;
	private static final int delay = 2;
	
	public Profile() {
		super(new ItemCreator(Material.SKULL_ITEM, 3).setName("&eProfile"), 6);
		delayed = new ArrayList<String>();
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		giveItem(player);
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		final Player player = event.getPlayer();
		if(isItem(player)) {
			if(!delayed.contains(player.getName())) {
				delayed.add(player.getName());
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(player.getName());
					}
				}, 20 * delay);
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String uuid = player.getUniqueId().toString();
						int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
						int month = Calendar.getInstance().get(Calendar.MONTH);
						Inventory inventory = Bukkit.createInventory(player, 9 * 5, ChatColor.stripColor(getName()));
						inventory.setItem(10, new ItemCreator(Material.EXP_BOTTLE).setName("&bLevel Information").setLores(new String [] {
							"",
							"&7Current level: &e" + LevelHandler.getLevel(player),
							"&7Progress: &e" + LevelHandler.getExp(player) + "&8/&e" + LevelHandler.getNeededForLevelUp(player) + " &8(&e" + LevelHandler.getPercentageDone(player) + "%&8)",
							""
						}).getItemStack());
						Ranks rank = AccountHandler.getRank(player);
						inventory.setItem(12, new ItemCreator(ItemUtil.getSkull(player.getName())).setName("&bAccount Information").setLores(new String [] {
							"",
							"&7First joined: &e" + DB.PLAYERS_ACCOUNTS.getString("uuid", uuid, "join_time"),
							"&7Current rank: " + (rank == Ranks.PLAYER ? Ranks.PLAYER.getColor() + "[Player]" : rank.getPrefix()),
							""
						}).getItemStack());
						try {
							inventory.setItem(14, new ItemCreator(Material.WATCH).setName("&bPlaytime").setLores(new String [] {
								"",
								"&7Lifetime: &e" + PlaytimeTracker.getPlayTime(player).getDisplay(TimeType.LIFETIME),
								"&7Monthly: &e" + PlaytimeTracker.getPlayTime(player).getDisplay(TimeType.MONTHLY),
								"&7Weekly: &e" + PlaytimeTracker.getPlayTime(player).getDisplay(TimeType.WEEKLY),
								""
							}).getItemStack());
						} catch(NullPointerException e) {
							inventory.setItem(14, new ItemCreator(Material.WATCH).setName("&bPlaytime").setLores(new String [] {
								"",
								"&7Lifetime: &4N/A &7(Reopen profile item)",
								"&7Monthly: &4N/A &7(Reopen profile item)",
								"&7Weekly: &4N/A &7(Reopen profile item)",
								""
							}).getItemStack());
						}
						inventory.setItem(16, new ItemCreator(Material.EMERALD).setName("&bAchievements").setLores(new String [] {
							"",
							"&7Click to view your achievements",
							""
						}).getItemStack());
						inventory.setItem(28, new ItemCreator(Material.DIAMOND_SWORD).setName("&bGame Stats").setLores(new String [] {
							"",
							"&cUnder development",
							"",
							"&7&mClick to view your game stats",
							"&7&mor use &c&m/stats &7&min a game's hub"
						}).getItemStack());
						inventory.setItem(30, new ItemCreator(Material.REDSTONE_COMPARATOR).setName("&bSettings").setLores(new String [] {
							"",
							"&cUnder development",
							"",
							"&7&mClick to view your settings",
							""
						}).getItemStack());
						inventory.setItem(32, new ItemCreator(Material.CHEST).setName("&bReward Crate Stats").setLores(new String [] {
							"",
							"&7Reward crate keys owned: &e" + DB.HUB_CRATE_KEYS.getInt("uuid", uuid, "amount"),
							"&7Reward crates opened lifetime: &e" + DB.HUB_LIFETIME_CRATES_OPENED.getInt("uuid", uuid, "amount"),
							"&7Reward crates opened this month: &e" + DB.HUB_MONTHLY_CRATES_OPENED.getInt(new String [] {"uuid", "month"}, new String [] {uuid, month + ""}, "amount"),
							"&7Reward crates opened this week: &e" + DB.HUB_WEEKLY_CRATES_OPENED.getInt(new String [] {"uuid", "week"}, new String [] {uuid, week + ""}, "amount"),
							"",
						}).getItemStack());
						inventory.setItem(34, new ItemCreator(Material.NAME_TAG).setName("&bVote Stats").setLores(new String [] {
							"",
							"&eClick to view the vote shop",
							"",
							"&7Vote passes: &e" + DB.PLAYERS_VOTE_PASSES.getInt("uuid", uuid, "amount"),
							"&7Lifetime votes: &e" + DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid, "amount"),
							"&7Monthly votes: &e" + DB.PLAYERS_MONTHLY_VOTES.getInt(new String [] {"uuid", "month"}, new String [] {uuid, month + ""}, "amount"),
							"&7Weekly votes: &e" + DB.PLAYERS_WEEKLY_VOTES.getInt(new String [] {"uuid", "week"}, new String [] {uuid, week + ""}, "amount"),
							""
						}).getItemStack());
						player.openInventory(inventory);
					}
				});
			}
			player.updateInventory();
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(ChatColor.stripColor(getName()))) {
			event.setCancelled(true);
		}
	}
	
	@Override
	public void giveItem(Player player) {
		player.getInventory().setItem(getSlot(), ItemUtil.getSkull(player.getName(), getItem().clone()));
	}
}
