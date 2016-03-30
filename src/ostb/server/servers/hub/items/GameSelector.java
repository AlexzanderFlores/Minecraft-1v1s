package ostb.server.servers.hub.items;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.server.DB.Databases;
import ostb.server.servers.hub.HubBase;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class GameSelector extends HubItemBase {
	private static Map<ItemStack, Plugins> items = null;
	private static Map<String, Plugins> watching = null;
	private static Map<Plugins, Integer> players = null;
	
	public GameSelector() {
		super(new ItemCreator(Material.COMPASS).setName("&eGame Selector"), 0);
		items = new HashMap<ItemStack, Plugins>();
		watching = new HashMap<String, Plugins>();
		players = new HashMap<Plugins, Integer>();
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
		Player player = event.getPlayer();
		if(isItem(player)) {
			EffectUtil.playSound(player, Sound.CHEST_OPEN);
			openMenu(player);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		String title = event.getTitle();
		if(title.equals(ChatColor.stripColor(getName()))) {
			if(items.containsKey(item)) {
				Plugins plugin = items.get(item);
				open(player, plugin);
			} else if(event.getItemTitle().contains("Profile")) {
				player.closeInventory();
				Profile.open(player);
			}
			event.setCancelled(true);
		} else if(watching.containsKey(player.getName())) {
			if(item.getType() == Material.WOOD_DOOR) {
				openMenu(player);
			} else {
				ProPlugin.sendPlayerToServer(player, ChatColor.stripColor(event.getItemTitle()));
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20) {
			for(Plugins plugin : Plugins.values()) {
				for(String name : watching.keySet()) {
					if(watching.get(name) == plugin) {
						update(plugin);
						break;
					}
				}
			}
		}
	}
	
	public static void open(Player player, Plugins plugin) {
		Inventory inventory = Bukkit.createInventory(player, (plugin == Plugins.HUB ? ItemUtil.getInventorySize(ProPlugin.getNumberOfHubs()) : 9 * 3), plugin.getDisplay());
		if(plugin != Plugins.HUB) {
			inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
		}
		player.openInventory(inventory);
		watching.put(player.getName(), plugin);
		update(plugin);
	}
	
	private static void update(final Plugins plugin) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ResultSet resultSet = null;
				try {
					List<Integer> priorities = new ArrayList<Integer>();
					List<Integer> serverNumbers = new ArrayList<Integer>();
					List<String> lores = new ArrayList<String>();
					List<Integer> playerCounts = new ArrayList<Integer>();
					List<Integer> maxPlayers = new ArrayList<Integer>();
					int limit = plugin == Plugins.HUB ? 9 * 6 : 9;
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT * FROM server_status WHERE game_name = '" + plugin.toString() + "' ORDER BY server_number LIMIT " + limit).executeQuery();
					int playing = 0;
					while(resultSet.next()) {
						priorities.add(resultSet.getInt("listed_priority"));
						serverNumbers.add(resultSet.getInt("server_number"));
						lores.add(resultSet.getString("lore"));
						playerCounts.add(resultSet.getInt("players"));
						maxPlayers.add(resultSet.getInt("max_players"));
					}
					players.put(plugin, playing);
					for(String name : watching.keySet()) {
						if(watching.get(name) == plugin) {
							Player player = ProPlugin.getPlayer(name);
							if(player != null) {
								InventoryView inventoryView = player.getOpenInventory();
								String title = inventoryView.getTitle();
								if(title != null && title.equals(plugin.getDisplay())) {
									for(int a = 0; a < serverNumbers.size() && a < inventoryView.getTopInventory().getSize(); ++a) {
										int serverNumber = serverNumbers.get(a);
										String server = "&b" + plugin.getServer() + serverNumber;
										byte data = getWoolColor(priorities.get(a));
										if(plugin == Plugins.HUB) {
											if(HubBase.getHubNumber() == serverNumber) {
												data = (byte) 3;
											} else {
												data = (byte) 5;
											}
										}
										int currentPlayers = playerCounts.get(a);
										int maxPlayerCount = maxPlayers.get(a);
										int percentage = (int) (currentPlayers * 100.0 / maxPlayerCount + 0.5);
										String [] lore = null;
										if(plugin == Plugins.HUB) {
											lore = new String [] {
												"",
												data == 3 ? "&7You are on this hub" : "&7Click to join &eHub #" + serverNumber,
												"",
												"&e" + currentPlayers + "&8/&e" + maxPlayerCount + " &7(&e" + percentage + "% Full&7)",
												""
											};
										} else {
											 lore = new String [] {
												"",
												"&7Click to play &e" + plugin.getDisplay() + "&7!",
												"",
												"&e" + currentPlayers + "&8/&e" + maxPlayerCount + " &7(&e" + percentage + "% Full&7)",
												""
											};
										}
										inventoryView.setItem(a, new ItemCreator(Material.WOOL, data).setAmount(serverNumber).setName(server).setLores(lore).getItemStack());
									}
									for(int a = serverNumbers.size(); a < (plugin == Plugins.HUB ? ItemUtil.getInventorySize(ProPlugin.getNumberOfHubs()) : 9); ++a) {
										if(inventoryView.getItem(a).getType() == Material.WOOL) {
											inventoryView.setItem(a, new ItemStack(Material.AIR));
										}
									}
								}
							}
						}
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					if(resultSet != null) {
						try {
							resultSet.close();
						} catch(SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getInventory().getTitle().equals(ChatColor.stripColor(getName()))) {
			watching.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		watching.remove(event.getPlayer().getName());
	}
	
	private void openMenu(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, ChatColor.stripColor(getName()));
		ItemStack item = new ItemCreator(Material.BANNER, 1).setName("&bPVP Battles - " + Plugins.CTF.getDisplay()).setLores(new String [] {
			"",
			"&eHelp your team capture the enemy's flag!",
			"&eFirst team to &a3 &ecaptures wins",
			"",
			"&7Playing: &a" + getPlayers(Plugins.CTF),
			"&7Team size: &a12 vs 12",
			""
		}).getItemStack();
		items.put(item, Plugins.CTF);
		inventory.setItem(10, item);
		item = new ItemCreator(Material.BANNER, 11).setName("&bPVP Battles - " + Plugins.DOM.getDisplay()).setLores(new String [] {
			"",
			"&eHelp your team capture the command posts!",
			"&eFirst team to &a1000 &epoints wins",
			"",
			"&7Playing: &a" + getPlayers(Plugins.DOM),
			"&7Team size: &a12 vs 12",
			""
		}).getItemStack();
		items.put(item, Plugins.DOM);
		inventory.setItem(12, item);
		item = new ItemCreator(Material.GRASS).setName("&b" + Plugins.SKY_WARS_SOLO.getDisplay()).setLores(new String [] {
			"",
			"&eBe the last player standing!",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SKY_WARS_SOLO),
			"&7Team size: &aSolo",
			""
		}).getItemStack();
		items.put(item, Plugins.SKY_WARS_SOLO);
		inventory.setItem(14, item);
		item = new ItemCreator(Material.GRASS).setName("&b" + Plugins.SKY_WARS_TEAMS.getDisplay()).setAmount(2).setLores(new String [] {
			"",
			"&eBe the last team standing!",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SKY_WARS_TEAMS),
			"&7Taem size: &a2",
			""
		}).getItemStack();
		items.put(item, Plugins.SKY_WARS_TEAMS);
		inventory.setItem(16, item);
		item = new ItemCreator(Material.GOLDEN_APPLE).setName("&b" + Plugins.HE_KITS.getDisplay()).setLores(new String [] {
			"",
			"&eGather resources before 1v1 PVP",
			"&eNatural regeneration is &cOFF",
			"",
			"&7Playing: &a" + getPlayers(Plugins.HE_KITS),
			"&7Kits: &aEnabled",
			""
		}).getItemStack();
		items.put(item, Plugins.HE_KITS);
		inventory.setItem(30, item);
		item = new ItemCreator(Material.GOLDEN_APPLE).setName("&b" + Plugins.HE_NO_KITS.getDisplay()).setAmount(2).setLores(new String [] {
			"",
			"&eGather resources before 1v1 PVP",
			"&eNatural regeneration is &cOFF",
			"",
			"&7Playing: &a" + getPlayers(Plugins.HE_KITS),
			"&7Kits: &cDisabled",
			""
		}).getItemStack();
		items.put(item, Plugins.HE_NO_KITS);
		inventory.setItem(32, item);
		inventory.setItem(49, Profile.getItem(player));
		player.openInventory(inventory);
	}
	
	private int getPlayers(Plugins plugin) {
		if(players.containsKey(plugin)) {
			return players.get(plugin);
		} else {
			return 0;
		}
	}
	
	private static byte getWoolColor(int priority) {
		return (byte) (priority == 1 ? 4 : priority == 2 ? 5 : priority == 3 ? 14 : 0);
	}
}
