package ostb.server.servers.hub.items;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.server.DB.Databases;
import ostb.server.nms.npcs.NPCEntity;
import ostb.server.servers.hub.HubBase;
import ostb.server.servers.hub.HubItemBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class GameSelector extends HubItemBase {
	private static Map<ItemStack, Plugins> items = null;
	private static Map<String, Plugins> watching = null;
	private static Map<Plugins, Integer> players = null;
	private String hardcoreEliminationTitle = null;
	private String skyWarsTitle = null;
	
	public GameSelector() {
		super(new ItemCreator(Material.COMPASS).setName("&eGame Selector"), 0);
		items = new HashMap<ItemStack, Plugins>();
		watching = new HashMap<String, Plugins>();
		players = new HashMap<Plugins, Integer>();
		hardcoreEliminationTitle = Plugins.HE_KITS.getDisplay() + " Type";
		skyWarsTitle = Plugins.SKY_WARS.getDisplay() + " Type";
		World world = Bukkit.getWorlds().get(0);
		new NPCEntity(EntityType.SKELETON, "&e&nCapture the Flag", new Location(world, 1681.5, 5, -1296.5), new ItemStack(Material.WOOL, 1, (byte) 14)) {
			@Override
			public void onInteract(Player player) {
				playSound(player, Plugins.CTF, Sound.HURT_FLESH);
				open(player, Plugins.CTF);
			}
		};
		new NPCEntity(EntityType.SKELETON, "&e&nDomination", new Location(world, 1683.5, 5, -1297.5), new ItemStack(Material.WOOL, 1, (byte) 4)) {
			@Override
			public void onInteract(Player player) {
				playSound(player, Plugins.DOM, Sound.HURT_FLESH);
				open(player, Plugins.DOM);
			}
		};
		new NPCEntity(EntityType.SKELETON, "&e&nSky Wars", new Location(world, 1685.5, 5, -1297.5), Material.STONE_SWORD) {
			@Override
			public void onInteract(Player player) {
				playSound(player, Plugins.SKY_WARS, Sound.HURT_FLESH);
				open(player, Plugins.SKY_WARS);
			}
		};
		new NPCEntity(EntityType.SKELETON, "&e&nHardcore Elimination", new Location(world, 1687.5, 5, -1296.5), Material.GOLDEN_APPLE) {
			@Override
			public void onInteract(final Player player) {
				playSound(player, Plugins.HE_KITS, Sound.EAT);
				open(player, Plugins.HE_KITS);
			}
		};
	}
	
	private void playSound(final Player player, final Plugins plugin, final Sound sound) {
		for(long delay : new long [] {3, 7, 12}) {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					String title = player.getOpenInventory().getTitle();
					if(title != null && title.equals(plugin.getDisplay())) {
						EffectUtil.playSound(player, sound);
					}
				}
			}, delay);
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
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		String title = event.getTitle();
		if(title.equals(ChatColor.stripColor(getName()))) {
			if(items.containsKey(item)) {
				Plugins plugin = items.get(item);
				if(plugin == Plugins.HE_KITS) {
					openHardcoreElimination(player);
				} else if(plugin == Plugins.SKY_WARS) {
					openSkyWars(player);
				} else {
					open(player, plugin);
				}
			}
			event.setCancelled(true);
		} else if(title.equals(hardcoreEliminationTitle)) {
			if(item.getType() == Material.WOOD_DOOR) {
				openMenu(player);
			} else {
				open(player, item.getAmount() == 1 ? Plugins.HE_NO_KITS : Plugins.HE_KITS);
			}
			event.setCancelled(true);
		} else if(title.equals(skyWarsTitle)) {
			if(item.getType() == Material.WOOD_DOOR) {
				openMenu(player);
			} else {
				open(player, item.getAmount() == 1 ? Plugins.SKY_WARS : Plugins.SKY_WARS_TEAMS);
			}
			event.setCancelled(true);
		} else if(watching.containsKey(player.getName())) {
			if(item.getType() == Material.WOOD_DOOR) {
				Plugins plugin = watching.get(player.getName());
				if(plugin == Plugins.HE_KITS || plugin == Plugins.HE_NO_KITS) {
					openHardcoreElimination(player);
				} else if(plugin == Plugins.SKY_WARS || plugin == Plugins.SKY_WARS_TEAMS) {
					openSkyWars(player);
				} else {
					openMenu(player);
				}
			} else {
				ProPlugin.sendPlayerToServer(player, ChatColor.stripColor(event.getItemTitle()));
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Plugins plugin : Plugins.values()) {
			for(String name : watching.keySet()) {
				if(watching.get(name) == plugin) {
					update(plugin);
					break;
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
					//listed_priority, players DESC, 
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
									for(int a = 0; a < serverNumbers.size(); ++a) {
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
	
	private void openSkyWars(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 4, skyWarsTitle);
		inventory.setItem(11, new ItemCreator(Material.EMERALD).setName("&bSolo Sky Wars").getItemStack());
		inventory.setItem(15, new ItemCreator(Material.EMERALD).setName("&bSky Wars Teams").setAmount(2).getItemStack());
		inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
		player.openInventory(inventory);
	}
	
	private void openHardcoreElimination(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 4, hardcoreEliminationTitle);
		inventory.setItem(11, new ItemCreator(Material.GOLDEN_APPLE).setName("&bNo Kits").getItemStack());
		inventory.setItem(15, new ItemCreator(Material.GOLDEN_APPLE).setName("&bKits Enabled").setAmount(2).getItemStack());
		inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
		player.openInventory(inventory);
	}
	
	private void openMenu(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.stripColor(getName()));
		/*ItemStack item = new ItemCreator(Material.DRAGON_EGG).setName("&bDragon Races").setLores(new String [] {
			"&7Unique game",
			"",
			"&aRace against &e7 &aother players",
			"&awhile riding Ender Dragons!",
			"",
			"&7Playing: &e" + players.get(Plugins.DRAGON_RACES),
			""
		}).getItemStack();
		items.put(item, Plugins.DRAGON_RACES);
		inventory.setItem(0, item);*/
		ItemStack item = new ItemCreator(Material.DIAMOND_SWORD).setName("&bCapture the Flag").setLores(new String [] {
			"&7Unique spin-off of CTF",
			"",
			"&aTwo-Team CTF PVP with unique gameplay",
			"",
			"&7Playing: &e" + players.get(Plugins.CTF),
		}).getItemStack();
		items.put(item, Plugins.CTF);
		inventory.setItem(1, item);
		item = new ItemCreator(Material.DIAMOND_SWORD).setName("&bDomination").setLores(new String [] {
			"&7Unique spin-off of Domination",
			"",
			"&aTwo-Team DOM PVP with unique gameplay",
			"",
			"&7Playing: &e" + players.get(Plugins.DOM),
		}).getItemStack();
		items.put(item, Plugins.DOM);
		inventory.setItem(3, item);
		item = new ItemCreator(Material.EMERALD).setName("&bSky Wars").setLores(new String [] {
			"&7Well known game",
			"",
			"&aClassic Sky Wars PVP mini-game",
			"",
			"&7Playing: &e" + players.get(Plugins.SKY_WARS),
			""
		}).getItemStack();
		items.put(item, Plugins.SKY_WARS);
		inventory.setItem(5, item);
		item = new ItemCreator(Material.GOLDEN_APPLE).setName("&bHardcore Elimination").setLores(new String [] {
			"&7Unique spin-off of Ultra Hardcore",
			"",
			"&aYou have &e10 &aminutes to gather resources",
			"&aand prepare yourself for 1v1 battle",
			"&aagainst other players. Natural regen is &cOFF",
			"",
			"&7Playing: &e" + players.get(Plugins.HE_KITS),
			""
		}).getItemStack();
		items.put(item, Plugins.HE_KITS);
		inventory.setItem(7, item);
		player.openInventory(inventory);
	}
	
	private static byte getWoolColor(int priority) {
		return (byte) (priority == 1 ? 4 : priority == 2 ? 5 : priority == 3 ? 14 : 0);
	}
}
