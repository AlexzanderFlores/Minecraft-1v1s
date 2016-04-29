package ostb.server.servers.hub.items.features.blocks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
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
public class SpinningBlocks extends FeatureBase {
	private static int max = 7;
	private static Map<String, Integer> owned = null;
	private static Map<String, SpinBlockEntity> blocks = null;
	
	public enum SpinBlock {
		PUMPKIN(10, "Pumpkin", Rarity.RARE, Material.PUMPKIN),
		JACK_O_LANTERN(12, "Jack o Lantern", Rarity.RARE, Material.JACK_O_LANTERN),
		TNT(14, "TNT", Rarity.RARE, Material.TNT),
		GLOWSTONE(16, "Glowstone", Rarity.RARE, Material.GLOWSTONE),
		JUKEBOX(28, "Jukebox", Rarity.RARE, Material.JUKEBOX),
		BEACON(30, "Beacon", Rarity.RARE, Material.BEACON),
		REDSTONE_LAMP(32, "Redstone Lamp", Rarity.RARE, Material.REDSTONE_LAMP_OFF),
		NO_BLOCKS(34, "&cNo Spinning Block", Rarity.COMMON, Material.BARRIER, false),
		
		;
		
		private int slot = 0;
		private String name = null;
		private ItemStack itemStack = null;
		private boolean store = true;
		private Rarity rarity = Rarity.COMMON;
		
		private SpinBlock(int slot, String name, Rarity rarity, Material material) {
			this(slot, name, rarity, new ItemStack(material));
		}
		
		private SpinBlock(int slot, String name, Rarity rarity, ItemStack itemStack) {
			this(slot, name, rarity, itemStack, true);
		}
		
		private SpinBlock(int slot, String name, Rarity rarity, Material material, boolean store) {
			this(slot, name, rarity, new ItemStack(material), store);
		}
		
		private SpinBlock(int slot, String name, Rarity rarity, ItemStack itemStack, boolean store) {
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
				Bukkit.getLogger().info("spinning blocks: owns");
			}
			return store && DB.HUB_SPINNING_BLOCKS.isKeySet(new String [] {"uuid", "name"}, new String [] {player.getUniqueId().toString(), toString()});
		}
		
		public void give(Player player) {
			if(store) {
				String [] keys = new String [] {"uuid", "name"};
				String [] values = new String [] {player.getUniqueId().toString(), toString()};
				if(owns(player) || DB.HUB_SPINNING_BLOCKS.isKeySet(keys, values)) {
					int owned = DB.HUB_SPINNING_BLOCKS.getInt(keys, values, "amount_owned");
					DB.HUB_SPINNING_BLOCKS.updateInt("amount_owned", owned + 1, keys, values);
					Bukkit.getLogger().info("spinning blocks: give more");
				} else {
					String time = TimeUtil.getTime().substring(0, 10);
					DB.HUB_SPINNING_BLOCKS.insert("'" + player.getUniqueId().toString() + "', '" + toString() + "', '1', '" + time + "'");
					Bukkit.getLogger().info("spinning blocks: give");
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
					int owned = DB.HUB_SPINNING_BLOCKS.getInt(keys, values, "amount_owned");
					item.setLores(new String [] {
						"",
						"&7Status: &eUnlocked",
						"&7You own &e" + owned + " &7of these",
						"&7Unlocked on &e" + DB.HUB_SPINNING_BLOCKS.getString(keys, values, "unlocked_time"),
						"&7Rarity: &e" + getRarity().getName(),
						""
					});
					Bukkit.getLogger().info("spinning blocks: getItem");
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
			int amount = DB.HUB_SPINNING_BLOCKS.getInt(keys, values, "amount_owned") - 1;
			if(amount <= 0) {
				DB.HUB_SPINNING_BLOCKS.delete(keys, values);
			} else {
				DB.HUB_SPINNING_BLOCKS.updateInt("amount_owned", amount, keys, values);
			}
			owned.remove(player.getName());
		}
	}
	
	public SpinningBlocks() {
		super(getInvName(), 14, new ItemStack(Material.WOOL, 1, (byte) 4), null, new String [] {
			"",
			"&7Have blocks spin around you in hubs!",
			"",
			"&7Owned: &eXX&8/&e" + max + " &7(&eYY%&7)",
			"&7Collect from: &eZZ",
			"",
			Ranks.PREMIUM_PLUS.getPrefix() + "&7is required to prevent",
			"&7lag and improve server performance",
			""
		});
		owned = new HashMap<String, Integer>();
		blocks = new HashMap<String, SpinBlockEntity>();
		SpinBlock.values();
	}
	
	private static String getInvName() {
		return "Spinning Blocks";
	}
	
	private static InventoryView opened(Player player) {
		InventoryView inventoryView = player.getOpenInventory();
		return inventoryView != null && inventoryView.getTitle().equals(getInvName()) ? inventoryView : null;
	}
	
	@Override
	public int getOwned(Player player) {
		if(!owned.containsKey(player.getName())) {
			owned.put(player.getName(), DB.HUB_SPINNING_BLOCKS.getSize("uuid", player.getUniqueId().toString()));
			Bukkit.getLogger().info("spinning blocks: getOwned");
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
				for(SpinBlock spinBlock : SpinBlock.values()) {
					inventory.setItem(spinBlock.getSlot(), spinBlock.getItem(player, getAction()));
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
			ItemStack item = event.getItem();
			if(item.getType() == Material.BARRIER) {
				String name = event.getItemTitle();
				if(blocks.containsKey(player.getName())) {
					blocks.get(player.getName()).remove();
					blocks.remove(player.getName());
					MessageHandler.sendMessage(player, "You have set " + name);
				}
			} else if(item.getType() == Material.INK_SACK) {
				displayLocked(player);
			} else if(item.getType() == Material.WOOD_DOOR) {
				Features.open(player);
			} else if(Ranks.PREMIUM_PLUS.hasRank(player)) {
				if(blocks.containsKey(player.getName())) {
					blocks.get(player.getName()).remove();
				}
				blocks.put(player.getName(), new SpinBlockEntity(item.getType(), item.getData().getData(), player));
			} else {
				player.closeInventory();
				MessageHandler.sendMessage(player, Ranks.PREMIUM_PLUS.getNoPermission());
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		owned.remove(event.getPlayer().getName());
		if(blocks.containsKey(event.getPlayer().getName())) {
			blocks.get(event.getPlayer().getName()).remove();
			blocks.remove(event.getPlayer().getName());
		}
	}
}
