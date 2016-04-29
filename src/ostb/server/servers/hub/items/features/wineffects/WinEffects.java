package ostb.server.servers.hub.items.features.wineffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameWinEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.server.DB;
import ostb.server.servers.hub.items.Features;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.servers.hub.items.features.FeatureBase;
import ostb.server.servers.hub.items.features.FeatureItem;
import ostb.server.servers.hub.items.features.FeatureItem.FeatureType;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.ItemCreator;
import ostb.server.util.TimeUtil;

@SuppressWarnings("deprecation")
public class WinEffects extends FeatureBase {
	private static int max = 6;
	private static Map<String, WinEffect> activated = null;
	private static Map<String, Integer> owned = null;
	private static byte [] colors;
	private static byte [] fireColors;
	private static byte last = 0;
	private static byte lastFireColor = 0;
	private static Random random = null;
	
	public enum WinEffect {
		FIREWORKS(11, "Firework Win Effect", Rarity.UNCOMMON, Material.FIREWORK),
		FIREWORK_FRENZY(13, "Firework Frenzy Win Effect", Rarity.RARE, new ItemStack(Material.FIREWORK, 2)),
		DISCO_BLOCKS(15, "Disco Block Win Effect", Rarity.RARE, Material.WOOL),
		DISCO_ITEMS(29, "Disco Item Win Effect", Rarity.RARE, new ItemStack(Material.WOOL, 2)),
		FIRE_DISCO_ITEMS(31, "Fire Disco Item Win Effect", Rarity.RARE, new ItemStack(Material.WOOL, 3)),
		NONE(33, "&cNone", Rarity.COMMON, Material.BARRIER, false)
		
		;
		
		private int slot = 0;
		private String name = null;
		private ItemStack itemStack = null;
		private boolean store = true;
		private Rarity rarity = Rarity.COMMON;
		
		private WinEffect(int slot, String name, Rarity rarity, Material material) {
			this(slot, name, rarity, new ItemStack(material));
		}
		
		private WinEffect(int slot, String name, Rarity rarity, ItemStack itemStack) {
			this(slot, name, rarity, itemStack, true);
		}
		
		private WinEffect(int slot, String name, Rarity rarity, Material material, boolean store) {
			this(slot, name, rarity, new ItemStack(material), store);
		}
		
		private WinEffect(int slot, String name, Rarity rarity, ItemStack itemStack, boolean store) {
			this.slot = slot;
			this.name = name;
			this.rarity = rarity;
			this.itemStack = itemStack;
			this.store = store;
			if(store) {
				new FeatureItem(getName(), getItemStack(), getRarity(), FeatureType.REWARD_CRATE);
			}
		}
		
		public int getSlot() {
			return this.slot;
		}
		
		public String getName() {
			return this.name;
		}
		
		public ItemStack getItemStack() {
			return this.itemStack;
		}
		
		public Rarity getRarity() {
			return this.rarity;
		}
		
		public boolean owns(Player player) {
			InventoryView inventoryView = opened(player);
			if(inventoryView != null) {
				return store && inventoryView.getItem(getSlot()).getType() != Material.INK_SACK;
			}
			if(store) {
				Bukkit.getLogger().info("win effects: owns");
			}
			return store && DB.PLAYERS_WIN_EFFECTS.isKeySet(new String [] {"uuid", "name"}, new String [] {player.getUniqueId().toString(), toString()});
		}
		
		public void give(Player player) {
			if(store) {
				String [] keys = new String [] {"uuid", "name"};
				String [] values = new String [] {player.getUniqueId().toString(), toString()};
				if(owns(player) || DB.PLAYERS_WIN_EFFECTS.isKeySet(keys, values)) {
					int owned = DB.PLAYERS_WIN_EFFECTS.getInt(keys, values, "amount_owned");
					DB.PLAYERS_WIN_EFFECTS.updateInt("amount_owned", owned + 1, keys, values);
					Bukkit.getLogger().info("win effects: give more");
				} else {
					String time = TimeUtil.getTime().substring(0, 10);
					DB.PLAYERS_WIN_EFFECTS.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', '0', '1', '" + time + "'");
					Bukkit.getLogger().info("win effects: give");
				}
				owned.remove(player.getName());
			}
		}
		
		public ItemStack getItem(Player player, String action) {
			ItemCreator item = null;
			boolean own = owns(player);
			if(!store || own) {
				item = new ItemCreator(getItemStack()).setName("&b" + getName());
				if(own) {
					String [] keys = new String [] {"uuid", "name"};
					String [] values = new String [] {player.getUniqueId().toString(), toString()};
					int owned = DB.PLAYERS_WIN_EFFECTS.getInt(keys, values, "amount_owned");
					item.setLores(new String [] {
						"",
						"&7Status: &eUnlocked",
						"&7You own &e" + owned + " &7of these",
						"&7Unlocked on &e" + DB.PLAYERS_WIN_EFFECTS.getString(keys, values, "unlocked_time"),
						"&7Rarity: &e" + getRarity().getName(),
						""
					});
					Bukkit.getLogger().info("win effects: getItem");
				}
			} else {
				item = new ItemCreator(new ItemStack(Material.INK_SACK, 1, (byte) 8)).setName("&b" + getName());
				item.setLores(new String [] {
					"",
					"&7Status: &cLocked",
					"&7Unlock in: &e" + action,
					"&7Rarity: &e" + getRarity().getName(),
					"",
				});
			}
			return item.getItemStack();
		}
		
		public void decrenentAmount(Player player) {
			String [] keys = new String [] {"uuid", "name"};
			String [] values = new String [] {player.getUniqueId().toString(), toString()};
			int amount = DB.PLAYERS_WIN_EFFECTS.getInt(keys, values, "amount_owned") - 1;
			if(amount <= 0) {
				DB.PLAYERS_WIN_EFFECTS.delete(keys, values);
			} else {
				DB.PLAYERS_WIN_EFFECTS.updateInt("amount_owned", amount, keys, values);
			}
			owned.remove(player.getName());
		}
		
		public void execute() {
			if(this == FIREWORKS) {
				new Fireworks();
			} else if(this == FIREWORK_FRENZY) {
				new FireworkFrenzy();
			} else if(this == DISCO_BLOCKS) {
				new DiscoBlocks();
			} else if(this == DISCO_ITEMS) {
				new DiscoItems();
			} else if(this == FIRE_DISCO_ITEMS) {
				new FireDiscoItems();
			}
		}
	}
	
	public WinEffects() {
		super(getInvName(), 20, new ItemStack(Material.FIREWORK), null, new String [] {
			"",
			"&7Make a statement when you",
			"&7win with these cool effects!",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			""
		});
		activated = new HashMap<String, WinEffect>();
		owned = new HashMap<String, Integer>();
		colors = new byte [] {3, 4, 5, 6};
		fireColors = new byte [] {1, 14, 15};
		random = new Random();
		WinEffect.values();
	}
	
	private static String getInvName() {
		return "Win Effects";
	}
	
	private static InventoryView opened(Player player) {
		InventoryView inventoryView = player.getOpenInventory();
		return inventoryView != null && inventoryView.getTitle().equals(getInvName()) ? inventoryView : null;
	}
	
	private WinEffect getEffect(int slot) {
		for(WinEffect effect : WinEffect.values()) {
			if(effect.getSlot() == slot) {
				return effect;
			}
		}
		return null;
	}
	
	public static WinEffect getActiveEffect(Player player) {
		return activated.get(player.getName());
	}
	
	private void remove(Player player) {
		if(activated.containsKey(player.getName())) {
			final WinEffect effect = activated.get(player.getName());
			if(effect != null) {
				final UUID uuid = player.getUniqueId();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						DB.PLAYERS_WIN_EFFECTS.updateInt("active", 0, "uuid", uuid.toString());
						DB.PLAYERS_WIN_EFFECTS.updateInt("active", 1, new String [] {"uuid", "name"}, new String [] {uuid.toString(), effect.toString()});
						Bukkit.getLogger().info("win effects: selected");
					}
				});
			}
		}
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			owned.put(player.getName(), DB.PLAYERS_WIN_EFFECTS.getSize("uuid", player.getUniqueId().toString()));
			Bukkit.getLogger().info("win effects: getOwned");
		}
		return owned.get(player.getName());
	}
	
	@Override
	public int getMax() {
		return max;
	}
	
	@Override
	public void display(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				for(WinEffect effect : WinEffect.values()) {
					inventory.setItem(effect.getSlot(), effect.getItem(player, getAction()));
				}
				inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&cBack").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getInvName())) {
			Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			ItemStack item = event.getItem();
			if(item.getType() == Material.BARRIER) {
				if(activated.containsKey(player.getName())) {
					activated.remove(player.getName());
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.PLAYERS_WIN_EFFECTS.updateInt("active", 0, "uuid", uuid.toString());
							Bukkit.getLogger().info("win effects: set none");
						}
					});
					MessageHandler.sendMessage(player, "You have set &cNo Win Effect");
				}
			} else if(item.getType() == Material.INK_SACK) {
				displayLocked(player);
			} else if(item.getType() == Material.WOOD_DOOR) {
				Features.open(player);
			} else {
				final WinEffect effect = getEffect(event.getSlot());
				if(effect != null) {
					activated.put(player.getName(), effect);
					effect.execute();
					MessageHandler.sendMessage(player, "You have enabled &e" + ChatColor.stripColor(effect.getName()) + " &xfor the next time you win a game");
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 5) {
			byte color = colors[0];
			do {
				color = colors[random.nextInt(colors.length)];
			} while(color == last);
			last = color;
			byte fireColor = fireColors[0];
			do {
				fireColor = fireColors[random.nextInt(fireColors.length)];
			} while(fireColor == lastFireColor);
			lastFireColor = fireColor;
			for(Player player : Bukkit.getOnlinePlayers()) {
				InventoryView inventory = opened(player);
				if(inventory != null) {
					for(WinEffect effect : new WinEffect [] {WinEffect.DISCO_BLOCKS, WinEffect.DISCO_ITEMS, WinEffect.FIRE_DISCO_ITEMS}) {
						ItemStack item = inventory.getItem(effect.getSlot());
						if(item != null && item.getType() != Material.INK_SACK) {
							ItemStack newItem = new ItemStack(Material.WOOL, effect.getItemStack().getAmount(), effect == WinEffect.FIRE_DISCO_ITEMS ? fireColor : color);
							ItemMeta meta = effect.getItemStack().getItemMeta();
							if(meta.getLore() != null && !meta.getLore().isEmpty()) {
								List<String> lores = new ArrayList<String>();
								for(String lore : effect.getItemStack().getItemMeta().getLore()) {
									lores.add(lore);
								}
								meta.setLore(lores);
								newItem.setItemMeta(meta);
								lores.clear();
								lores = null;
							}
							inventory.setItem(effect.getSlot(), new ItemCreator(newItem).setName(item.getItemMeta().getDisplayName()).getItemStack());
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		String [] keys = new String [] {"uuid", "active"};
		if(event.getTeam() != null) {
			for(OfflinePlayer player : event.getTeam().getPlayers()) {
				UUID uuid = player.getUniqueId();
				String [] values = new String [] {uuid.toString(), "1"};
				String selected = DB.PLAYERS_WIN_EFFECTS.getString(keys, values, "name");
				WinEffect effect = WinEffect.valueOf(selected);
				if(effect != null) {
					activated.put(player.getName(), effect);
					effect.execute();
				}
			}
		} else {
			Player player = event.getPlayer();
			UUID uuid = player.getUniqueId();
			String [] values = new String [] {uuid.toString(), "1"};
			String selected = DB.PLAYERS_WIN_EFFECTS.getString(keys, values, "name");
			WinEffect effect = WinEffect.valueOf(selected);
			if(effect != null) {
				activated.put(player.getName(), effect);
				effect.execute();
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			remove(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		owned.remove(event.getPlayer().getName());
		activated.remove(event.getPlayer().getName());
		remove(event.getPlayer());
	}
}
