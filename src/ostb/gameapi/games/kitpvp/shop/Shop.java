package ostb.gameapi.games.kitpvp.shop;

import java.util.HashMap;
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
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import npc.NPCEntity;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.CoinsHandler;
import ostb.player.TitleDisplayer;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Shop implements Listener {
	private static boolean registered = false;
	private String name = null;
	
	public Shop(World world, Location redSpawn, Location blueSpawn, Location yellowSpawn, Location greenSpawn) {
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/shop.yml");
		for(String key : config.getConfig().getKeys(false)) {
			double x = config.getConfig().getDouble(key + ".x");
			double y = config.getConfig().getDouble(key + ".y");
			double z = config.getConfig().getDouble(key + ".z");
			new Shop(new Location(world, x, y, z), redSpawn, blueSpawn, yellowSpawn, greenSpawn);
		}
	}
	
	public Shop(Location location, Location redSpawn, Location blueSpawn, Location yellowSpawn, Location greenSpawn) {
		name = "Shop";
		Location target = location.getWorld().getSpawnLocation();
		if(redSpawn != null && blueSpawn != null && yellowSpawn != null && greenSpawn != null) {
			Map<Location, Double> distances = new HashMap<Location, Double>();
			distances.put(redSpawn, location.distance(redSpawn));
			distances.put(blueSpawn, location.distance(blueSpawn));
			distances.put(yellowSpawn, location.distance(yellowSpawn));
			distances.put(greenSpawn, location.distance(greenSpawn));
			for(Location spawn : distances.keySet()) {
				if(distances.get(spawn) < location.distance(target)) {
					target = spawn;
				}
			}
			distances.clear();
			distances = null;
		}
		new NPCEntity(EntityType.ZOMBIE, "&e&n" + name, location, target) {
			@Override
			public void onInteract(Player player) {
				if(SpectatorHandler.contains(player)) {
					return;
				}
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
				
				// Gold Armor
				inventory.setItem(10, new ItemCreator(Material.GOLD_HELMET).setName("&bGold Helmet").setLores(new String [] {"", "&7Price: &a0", ""}).getItemStack());
				inventory.setItem(19, new ItemCreator(Material.GOLD_CHESTPLATE).setName("&bGold Chestplate").setLores(new String [] {"", "&7Price: &a0", ""}).getItemStack());
				inventory.setItem(28, new ItemCreator(Material.GOLD_LEGGINGS).setName("&bGold Leggings").setLores(new String [] {"", "&7Price: &a0", ""}).getItemStack());
				inventory.setItem(37, new ItemCreator(Material.GOLD_BOOTS).setName("&bGold Boots").setLores(new String [] {"", "&7Price: &a0", ""}).getItemStack());
				
				// Chain Armor
				inventory.setItem(11, new ItemCreator(Material.CHAINMAIL_HELMET).setName("&bChain Helmet").setLores(new String [] {"", "&7Price: &a5", ""}).getItemStack());
				inventory.setItem(20, new ItemCreator(Material.CHAINMAIL_CHESTPLATE).setName("&bChain Chestplate").setLores(new String [] {"", "&7Price: &a6", ""}).getItemStack());
				inventory.setItem(29, new ItemCreator(Material.CHAINMAIL_LEGGINGS).setName("&bChain Leggings").setLores(new String [] {"", "&7Price: &a6", ""}).getItemStack());
				inventory.setItem(38, new ItemCreator(Material.CHAINMAIL_BOOTS).setName("&bChain Boots").setLores(new String [] {"", "&7Price: &a5", ""}).getItemStack());
				
				// Iron Armor
				inventory.setItem(12, new ItemCreator(Material.IRON_HELMET).setName("&bIron Helmet").setLores(new String [] {"", "&7Price: &a10", ""}).getItemStack());
				inventory.setItem(21, new ItemCreator(Material.IRON_CHESTPLATE).setName("&bIron Chestplate").setLores(new String [] {"", "&7Price: &a11", ""}).getItemStack());
				inventory.setItem(30, new ItemCreator(Material.IRON_LEGGINGS).setName("&bIron Leggings").setLores(new String [] {"", "&7Price: &a11", ""}).getItemStack());
				inventory.setItem(39, new ItemCreator(Material.IRON_BOOTS).setName("&bIron Boots").setLores(new String [] {"", "&7Price: &a10", ""}).getItemStack());
				
				// Swords
				inventory.setItem(14, new ItemCreator(Material.STONE_SWORD).setName("&bStone Sword").setLores(new String [] {"", "&7Price: &a0", ""}).getItemStack());
				inventory.setItem(15, new ItemCreator(Material.IRON_SWORD).setName("&bIron Sword").setLores(new String [] {"", "&7Price: &a5", ""}).getItemStack());
				inventory.setItem(16, new ItemCreator(Material.DIAMOND_SWORD).setName("&bDiamond Sword").setLores(new String [] {"", "&7Price: &a10", ""}).getItemStack());
				
				// Secondaries
				inventory.setItem(23, new ItemCreator(Material.BOW).setName("&bBow").setLores(new String [] {"", "&7Price: &a6", ""}).getItemStack());
				inventory.setItem(24, new ItemCreator(Material.ARROW).setName("&bArrow x4").setAmount(4).setLores(new String [] {"", "&7Price: &a2", ""}).getItemStack());
				inventory.setItem(25, new ItemCreator(Material.FISHING_ROD).setName("&bFishing Rod").setLores(new String [] {"", "&7Price: &a2", ""}).getItemStack());
				
				inventory.setItem(32, new ItemCreator(Material.FLINT_AND_STEEL).setName("&bFlint and Steel").setLores(new String [] {"", "&7Price: &a1", "&7Uses: &a2", "&7Fire Lasts: &a3s", ""}).getItemStack());
				inventory.setItem(33, new ItemCreator(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1)).setName("&bHealth Potion").setLores(new String [] {"", "&7Price: &a2", ""}).getItemStack());
				inventory.setItem(34, new ItemCreator(Material.GOLDEN_APPLE).setName("&bGolden Apple").setLores(new String [] {"", "&7Price: &a1", ""}).getItemStack());
				
				// Enchant table
				inventory.setItem(41, new ItemCreator(Material.ENCHANTMENT_TABLE).setName("&bEnchant an Item").setLores(new String [] {"", "&7Price: &a20", "&7Price is per item", "&7Get a random enchantment", "&7On an item of your choice", ""}).getItemStack());
				inventory.setItem(42, new ItemCreator(Material.ANVIL).setName("&bRepair an Item").setLores(new String [] {"", "&7Price: &a" + RepairAnItem.getPrice(), "&7Price is per item", ""}).getItemStack());
				inventory.setItem(43, new ItemCreator(Material.ENDER_CHEST).setName("&bSave your Items").setLores(new String [] {"", "&7Save your items", ""}).getItemStack());
				
				inventory.setItem(inventory.getSize() - 5, CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData()).getItemStack(player));
				player.openInventory(inventory);
			}
		};
		if(!registered) {
			registered = true;
			EventUtil.register(this);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.GOLD_INGOT) {
				event.setCancelled(true);
				return;
			}
			if(item.getType() == Material.ENCHANTMENT_TABLE) {
				// http://puu.sh/p590k/7cd3721dac.png
				event.setCancelled(true);
				new InventoryViewer("Enchant an Item", player);
				return;
			}
			if(item.getType() == Material.ANVIL) {
				event.setCancelled(true);
				new RepairAnItem(player);
				return;
			}
			if(item.getType() == Material.ENDER_CHEST) {
				// http://puu.sh/p58tz/59aa56f606.png
				event.setCancelled(true);
				return;
			}
			int price = Integer.valueOf(ChatColor.stripColor(item.getItemMeta().getLore().get(1)).split(" ")[1]);
			CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData());
			if(coinsHandler.getCoins(player) >= price) {
				coinsHandler.addCoins(player, price * -1);
				player.getInventory().addItem(item.clone());
				InventoryView view = player.getOpenInventory();
				if(view != null) {
					view.setItem(view.getTopInventory().getSize() - 5, CoinsHandler.getCoinsHandler(Plugins.KITPVP.getData()).getItemStack(player));
				}
				EffectUtil.playSound(player, Sound.LEVEL_UP);
			} else {
				player.closeInventory();
				new TitleDisplayer(player, "&cNot enough coins", "&eFor " + item.getItemMeta().getDisplayName()).display();
				EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
			}
			event.setCancelled(true);
		}
	}
}
