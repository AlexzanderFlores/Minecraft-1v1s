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
import ostb.gameapi.AutoJoinHandler;
import ostb.player.MessageHandler;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler.Ranks;
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
		final Player player = event.getPlayer();
		ItemStack item = event.getItem();
		String title = event.getTitle();
		if(title.equals(ChatColor.stripColor(getName()))) {
			if(items.containsKey(item)) {
				Plugins plugin = items.get(item);
				open(player, plugin);
			}
			event.setCancelled(true);
		} else if(watching.containsKey(player.getName())) {
			if(item.getType() == Material.WOOD_DOOR) {
				openMenu(player);
			} else if(item.getType() == Material.EYE_OF_ENDER) {
				if(Ranks.PREMIUM.hasRank(player)) {
					player.closeInventory();
					new TitleDisplayer(player, "&bSearching...").display();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							ProPlugin.sendPlayerToServer(player, AutoJoinHandler.getBestServer(watching.get(player.getName())));
						}
					}, 20);
				} else {
					MessageHandler.sendMessage(player, Ranks.PREMIUM.getNoPermission());
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				}
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
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, plugin.getDisplay());
		if(plugin != Plugins.HUB) {
			inventory.setItem(inventory.getSize() - 7, new ItemCreator(Material.EYE_OF_ENDER).setName("&bAuto Join").setLores(new String [] {
				"",
				"&7Click to join the best available game",
				"&7Requires " + Ranks.PREMIUM.getPrefix(),
				""
			}).getItemStack());
			inventory.setItem(inventory.getSize() - 3, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
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
					int limit = 9 * 4;
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT * FROM server_status WHERE game_name = '" + plugin.toString() + "' ORDER BY listed_priority, players DESC, server_number LIMIT " + limit).executeQuery();
					int playing = 0;
					while(resultSet.next()) {
						priorities.add(resultSet.getInt("listed_priority"));
						serverNumbers.add(resultSet.getInt("server_number"));
						lores.add(resultSet.getString("lore"));
						int playerCount = resultSet.getInt("players");
						playerCounts.add(playerCount);
						playing += playerCount;
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
		Inventory inventory = Bukkit.createInventory(player, 9 * 4, ChatColor.stripColor(getName()));
		ItemStack item = new ItemCreator(Material.BANNER, 11).setName("&b" + Plugins.DOM.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Domination",
			"",
			"&eHelp your team capture the command posts!",
			"&eFirst team to &a1000 &epoints wins",
			"",
			"&7Playing: &a" + getPlayers(Plugins.DOM),
			"&7Team size: &a12 vs 12",
			""
		}).getItemStack();
		items.put(item, Plugins.DOM);
		inventory.setItem(11, item);
		item = new ItemCreator(Material.GRASS).setName("&b" + Plugins.SW.getDisplay()).setLores(new String [] {
			"&7Well known game",
			"",
			"&eBe the last player standing!",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SW),
			"&7Team size: &aSolo",
			""
		}).getItemStack();
		items.put(item, Plugins.SW);
		inventory.setItem(13, item);
		item = new ItemCreator(Material.GRASS).setName("&b" + Plugins.SWT.getDisplay()).setAmount(2).setLores(new String [] {
			"&7Well known game",
			"",
			"&eBe the last team standing!",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SWT),
			"&7Team size: &a2",
			""
		}).getItemStack();
		items.put(item, Plugins.SWT);
		inventory.setItem(15, item);
		item = new ItemCreator(Material.GOLDEN_APPLE).setName("&b" + Plugins.SUHC.getDisplay()).setLores(new String [] {
			"&7Well known game",
			"",
			"&eNatural regeneration is &cOFF",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SUHC),
			""
		}).getItemStack();
		items.put(item, Plugins.SUHC);
		inventory.setItem(21, item);
		item = new ItemCreator(Material.STONE_SWORD).setName("&b" + Plugins.KITPVP.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Kit PVP",
			"",
			"&eBattle &cRED &evs &bBLUE &ein Kit PVP",
			"",
			"&7Playing: &a" + getPlayers(Plugins.KITPVP),
		}).getItemStack();
		items.put(item, Plugins.KITPVP);
		inventory.setItem(23, item);
		ItemUtil.addEnchantGlassPaneIncrement(inventory);
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
