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
import org.bukkit.Location;
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
	private static Map<String, Plugins> watching = null;
	private static Map<Plugins, Integer> itemSlots = null;
	private static List<Integer> slots = null;
	private static final int rows = 3;
	private static final int size = 9 * rows;
	private static final String name = "Game Selector";
	
	public GameSelector() {
		super(new ItemCreator(Material.COMPASS).setName("&e" + name), 0);
		watching = new HashMap<String, Plugins>();
		itemSlots = new HashMap<Plugins, Integer>();
		slots = new ArrayList<Integer>();
		for(int a = 0; a < 8; ++a) {
			if(!slots.contains(a)) {
				slots.add(a);
			}
		}
		for(int a = 8, counter = 0; a <= 8 * rows; a += 8, ++counter) {
			if(!slots.contains(a + counter)) {
				slots.add(a + counter);
			}
		}
		for(int a = 9; a < size; a += 9) {
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
			if(item.getType() == Material.DIAMOND_BOOTS) {
				player.closeInventory();
				player.teleport(new Location(player.getWorld(), 1675.5, 5, -1289.5, -222.0f, 0.0f));
			} else {
				for(Plugins plugin : Plugins.values()) {
					if(itemSlots.containsKey(plugin) && itemSlots.get(plugin) == event.getSlot()) {
						open(player, plugin);
						break;
					}
				}
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
			if(itemSlots.containsKey(Plugins.UHCSW)) {
				int slot = itemSlots.get(Plugins.UHCSW);
				for(Player player : ProPlugin.getPlayers()) {
					InventoryView view = player.getOpenInventory();
					if(view.getTitle().equals(name)) {
						if(view.getItem(slot).getType() == Material.GRASS) {
							view.getItem(slot).setType(Material.GOLDEN_APPLE);
						} else {
							view.getItem(slot).setType(Material.GRASS);
						}
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
					while(resultSet.next()) {
						priorities.add(resultSet.getInt("listed_priority"));
						serverNumbers.add(resultSet.getInt("server_number"));
						lores.add(resultSet.getString("lore"));
						int playerCount = resultSet.getInt("players");
						playerCounts.add(playerCount);
						maxPlayers.add(resultSet.getInt("max_players"));
					}
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
		Plugins plugin = Plugins.UHCSW;
		ItemStack item = new ItemCreator(Material.GRASS).setName("&b" + plugin.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Sky Wars",
			"",
			"&eUHC meets Sky Wars!",
			"&eNatural regeneration is &cOFF",
			"&eBe the last player standing!",
			"",
			"&7Team size: &a1",
			""
		}).getItemStack();
		itemSlots.put(plugin, 10);
		inventory.setItem(itemSlots.get(plugin), item);
		plugin = Plugins.KITPVP;
		item = new ItemCreator(Material.IRON_SWORD).setName("&b" + plugin.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of Kit PVP",
			"",
			"&eBattle &cRED &evs &bBLUE &ein Kit PVP",
			"",
			"&7Team size: &aUp to 20"
		}).getItemStack();
		itemSlots.put(plugin, 12);
		inventory.setItem(itemSlots.get(plugin), item);
		plugin = Plugins.ONEVSONE;
		item = new ItemCreator(Material.FISHING_ROD).setName("&b" + plugin.getDisplay()).setLores(new String [] {
			"&7Unique spin-off of 1v1s",
			"",
			"&eTest your competitive PVP skills",
			"&eagainst other players",
			""
		}).getItemStack();
		//inventory.setItem(15, item);
		itemSlots.put(plugin, 14);
		inventory.setItem(itemSlots.get(plugin), comingSoon);
		item = new ItemCreator(Material.DIAMOND_BOOTS).setName("&bParkour").setLores(new String [] {
			"&7Our Unique Parkour Course",
			"",
			"&a&lEndless Parkour",
			"&eParkour forever, try to",
			"&ebeat your high score!",
			"",
			"&a&lParkour Course",
			"&eParkour on our custom course",
			"&eWatch out for obstacles though",
			""
		}).getItemStack();
		inventory.setItem(16, item);
		int data = new Random().nextInt(15);
		for(int slot : slots) {
			try {
				inventory.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE, data).setGlow(true).setName(" ").getItemStack());
			} catch(IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		player.openInventory(inventory);
	}
	
	private static byte getWoolColor(int priority) {
		return (byte) (priority == 1 ? 4 : priority == 2 ? 5 : priority == 3 ? 14 : 0);
	}
}
