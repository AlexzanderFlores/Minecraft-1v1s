package ostb.server.servers.hub.items;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	private static Map<String, Plugins> items = null;
	private static Map<String, Plugins> watching = null;
	private static Map<Plugins, Integer> players = null;
	private static List<Integer> slots = null;
	private static final int size = 9 * 5;
	private static final String name = "Game Selector";
	
	public GameSelector() {
		super(new ItemCreator(Material.COMPASS).setName("&e" + name), 0);
		items = new HashMap<String, Plugins>();
		watching = new HashMap<String, Plugins>();
		players = new HashMap<Plugins, Integer>();
		slots = new ArrayList<Integer>();
		for(int a = 0; a < 8; ++a) {
			if(!slots.contains(a)) {
				slots.add(a);
			}
		}
		for(int a = 8, counter = 0; a <= 8 * 5; a += 8, ++counter) {
			if(!slots.contains(a + counter)) {
				slots.add(a + counter);
			}
		}
		for(int a = 9; a < 9 * 5; a += 9) {
			if(!slots.contains(a)) {
				slots.add(a);
			}
		}
		for(int a = 0; a < 9; ++a) {
			if(!slots.contains(size - 1 - a)) {
				slots.add(size - 1 - a);
			}
		}
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
			String name = item.getItemMeta().getDisplayName();
			if(items.containsKey(name)) {
				Plugins plugin = items.get(name);
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
			for(Player player : ProPlugin.getPlayers()) {
				InventoryView view = player.getOpenInventory();
				if(view.getTitle().equals(name)) {
					if(view.getItem(14).getType() == Material.GRASS) {
						view.getItem(14).setType(Material.GOLDEN_APPLE);
					} else {
						view.getItem(14).setType(Material.GRASS);
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
		Inventory inventory = Bukkit.createInventory(player, size, ChatColor.stripColor(getName()));
		ItemStack comingSoon = new ItemCreator(Material.INK_SACK, 8).setName("&7Coming Soon").getItemStack();
		ItemStack item = new ItemCreator(Material.BANNER, 1).setName("&b" + Plugins.DOM.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Domination",
			"",
			"&eHelp your team capture the command posts!",
			"&eFirst team to &a1000 &epoints wins",
			"",
			"&7Playing: &a" + getPlayers(Plugins.DOM),
			"&7Team size: &a12 vs 12",
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.DOM);
		inventory.setItem(10, item);
		item = new ItemCreator(Material.GRASS).setName("&b" + Plugins.SW.getDisplay()).setLores(new String [] {
			"&7Well known game",
			"",
			"&eBe the last player standing!",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SW),
			"&7Team size: &aSolo",
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.SW);
		inventory.setItem(12, item);
		item = new ItemCreator(Material.GRASS).setName("&b" + Plugins.UHCSW.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Sky Wars",
			"",
			"&eUHC meets Sky Wars!",
			"&eNatural regeneration is &cOFF",
			"&eBe the last player standing!",
			"",
			"&7Playing: &a" + getPlayers(Plugins.UHCSW),
			"&7Team size: &a1",
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.UHCSW);
		inventory.setItem(14, item);
		item = new ItemCreator(Material.GOLDEN_APPLE, 1).setName("&b" + Plugins.UHC.getDisplay()).setLores(new String [] {
			"&7Well known game",
			"",
			"&eNatural regeneration is &cOFF",
			"&eTwitter-based UHC events",
			"&eCheck for games: &b@OSTBUHC",
			"",
			"&7Playing: &a" + getPlayers(Plugins.UHC),
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.UHC);
		inventory.setItem(16, item);
		item = new ItemCreator(Material.GOLDEN_APPLE).setName("&b" + Plugins.SUHC.getDisplay()).setLores(new String [] {
			"&7Well known game",
			"",
			"&eNatural regeneration is &cOFF",
			"",
			"&7Playing: &a" + getPlayers(Plugins.SUHC),
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.SUHC);
		inventory.setItem(29, item);
		item = new ItemCreator(Material.FISHING_ROD).setName("&b" + Plugins.ONEVSONE.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of 1v1s",
			"",
			"&eTest your competitive PVP skills",
			"&eagainst other players",
			"",
			"&7Playing: &a" + getPlayers(Plugins.ONEVSONE),
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.ONEVSONE);
		//inventory.setItem(31, item);
		inventory.setItem(31, comingSoon);
		item = new ItemCreator(Material.IRON_SWORD).setName("&b" + Plugins.KITPVP.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Kit PVP",
			"",
			"&eBattle &cRED &evs &bBLUE &ein Kit PVP",
			"",
			"&7Playing: &a" + getPlayers(Plugins.KITPVP),
			"&7Team size: &aUp to 20",
			""
		}).getItemStack();
		items.put(item.getItemMeta().getDisplayName(), Plugins.KITPVP);
		inventory.setItem(33, item);
		int data = new Random().nextInt(15);
		for(int slot : slots) {
			inventory.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, data).setGlow(true).setName(" ").getItemStack());
		}
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
