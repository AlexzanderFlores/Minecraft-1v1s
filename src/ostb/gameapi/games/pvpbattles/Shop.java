package ostb.gameapi.games.pvpbattles;

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
import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.MiniGame.GameStates;
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
	
	public Shop(World world, Location redSpawn, Location blueSpawn) {
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/pvpbattles/shop.yml");
		double x = config.getConfig().getDouble("red.x");
		double y = config.getConfig().getDouble("red.y");
		double z = config.getConfig().getDouble("red.z");
		new Shop(new Location(world, x, y, z), redSpawn, blueSpawn);
		x = config.getConfig().getDouble("blue.x");
		y = config.getConfig().getDouble("blue.y");
		z = config.getConfig().getDouble("blue.z");
		new Shop(new Location(world, x, y, z), redSpawn, blueSpawn);
	}
	
	public Shop(Location location, Location redSpawn, Location blueSpawn) {
		name = "Shop";
		Location target = location.distance(redSpawn) < location.distance(blueSpawn) ? redSpawn : blueSpawn;
		new NPCEntity(EntityType.ZOMBIE, "&e&n" + name, location, target) {
			@Override
			public void onInteract(Player player) {
				if(SpectatorHandler.contains(player) || OSTB.getMiniGame().getGameState() != GameStates.STARTED) {
					return;
				}
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
				inventory.setItem(10, new ItemCreator(Material.IRON_SWORD).setName("&bIron Sword").setLores(new String [] {"", "&7Price: &a2"}).getItemStack());
				inventory.setItem(11, new ItemCreator(Material.BOW).setName("&bBow").setLores(new String [] {"", "&7Price: &a10", ""}).getItemStack());
				inventory.setItem(12, new ItemCreator(Material.ARROW).setAmount(16).setName("&bArrow x16").setLores(new String [] {"", "&7Price: &a5", ""}).getItemStack());
				inventory.setItem(13, new ItemCreator(Material.FLINT_AND_STEEL).setName("&bFlint and Steel").setLores(new String [] {"", "&7Price: &a7", "&7Uses: &a4", "&7Fire Lasts: &a3 Seconds", ""}).getItemStack());
				inventory.setItem(14, new ItemCreator(Material.FISHING_ROD).setName("&bFishing Rod").setLores(new String [] {"", "&7Price: &a10", ""}).getItemStack());
				inventory.setItem(15, new ItemCreator(Material.DIAMOND).setName("&bDiamond").setLores(new String [] {"", "&7Price: &a15", ""}).getItemStack());
				inventory.setItem(16, new ItemCreator(Material.STICK).setName("&bStick").setLores(new String [] {"", "&7Price: &a5", ""}).getItemStack());
				
				inventory.setItem(19, new ItemCreator(Material.TNT).setName("&bTNT x2").setAmount(2).setLores(new String [] {"", "&7Price: &a15", "&7Left Click: &aThrow", "&7Place: &aAuto-Ignite", ""}).getItemStack());
				inventory.setItem(20, new ItemCreator(Material.BOOK).setName("&bBook").setLores(new String [] {"", "&7Price: &a15", ""}).getItemStack());
				inventory.setItem(21, new ItemCreator(Material.GOLDEN_APPLE).setName("&bGolden Apple").setLores(new String [] {"", "&7Price: &a15", ""}).getItemStack());
				inventory.setItem(22, new ItemCreator(new Potion(PotionType.WATER).toItemStack(1)).setName("&bWater Bottle").setLores(new String [] {"", "&7Price: &a2", ""}).getItemStack());
				inventory.setItem(23, new ItemCreator(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1)).setName("&bHealth Potion").setLores(new String [] {"", "&7Price: &a15", ""}).getItemStack());
				inventory.setItem(24, new ItemCreator(new Potion(PotionType.POISON, 1, true).toItemStack(1)).setName("&bPoison Potion").setLores(new String [] {"", "&7Price: &a15", ""}).getItemStack());
				inventory.setItem(25, new ItemCreator(new Potion(PotionType.INVISIBILITY, 1, true).toItemStack(1)).setName("&bInvis Potion").setLores(new String [] {"", "&7Price: &a15", ""}).getItemStack());
				
				inventory.setItem(30, new ItemCreator(Material.EXP_BOTTLE).setName("&bExp Bottle").setLores(new String [] {"", "&7Price: &a3", ""}).getItemStack());
				inventory.setItem(32, new ItemCreator(Material.WORKBENCH).setName("&bCrafting Table").setLores(new String [] {"", "&7Price: &a10", "&7Click to open", "&7Does &cNOT &7place", ""}).getItemStack());
				
				inventory.setItem(inventory.getSize() - 5, CoinsHandler.getCoinsHandler(Plugins.PVP_BATTLES.getData()).getItemStack(player));
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
			int price = Integer.valueOf(ChatColor.stripColor(item.getItemMeta().getLore().get(1)).split(" ")[1]);
			CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.PVP_BATTLES.getData());
			if(coinsHandler.getCoins(player) >= price) {
				coinsHandler.addCoins(player, price * -1);
				player.getInventory().addItem(item.clone());
				InventoryView view = player.getOpenInventory();
				if(view != null) {
					view.setItem(view.getTopInventory().getSize() - 5, CoinsHandler.getCoinsHandler(Plugins.PVP_BATTLES.getData()).getItemStack(player));
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
