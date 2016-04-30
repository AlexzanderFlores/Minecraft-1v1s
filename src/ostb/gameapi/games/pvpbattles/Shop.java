package ostb.gameapi.games.pvpbattles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import npc.NPCEntity;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.CoinsHandler;
import ostb.player.TitleDisplayer;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Shop implements Listener {
	private static boolean registered = false;
	private String name = null;
	
	public Shop(World world) {
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/Shop.yml");
		double x = config.getConfig().getDouble("red.location.x");
		double y = config.getConfig().getDouble("red.location.y");
		double z = config.getConfig().getDouble("red.location.z");
		float yaw = (float) config.getConfig().getDouble("red.location.yaw");
		float pitch = (float) config.getConfig().getDouble("red.location.pitch");
		new Shop(new Location(world, x, y, z, yaw, pitch));
		x = config.getConfig().getDouble("blue.location.x");
		y = config.getConfig().getDouble("blue.location.y");
		z = config.getConfig().getDouble("blue.location.z");
		yaw = (float) config.getConfig().getDouble("blue.location.yaw");
		pitch = (float) config.getConfig().getDouble("blue.location.pitch");
		new Shop(new Location(world, x, y, z, yaw, pitch));
	}
	
	public Shop(Location location) {
		name = "Shop";
		new NPCEntity(EntityType.SKELETON, "&e&n" + name, location) {
			@Override
			public void onInteract(Player player) {
				Inventory inventory = Bukkit.createInventory(player, 9 * 4, name);
				inventory.setItem(10, new ItemCreator(Material.IRON_SWORD).setName("&bIron Sword").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(11, new ItemCreator(Material.BOW).setName("&bBow").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(12, new ItemCreator(Material.ARROW, 16).setName("&bArrow x16").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(13, new ItemCreator(Material.FLINT_AND_STEEL).setName("&bFlint and Steel").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(14, new ItemCreator(Material.FISHING_ROD).setName("&bFishing Rod").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(15, new ItemCreator(Material.DIAMOND).setName("&bDiamond").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(16, new ItemCreator(Material.STICK).setName("&bStick").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				
				inventory.setItem(19, new ItemCreator(Material.TNT).setName("&bTNT x2").setAmount(2).setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(20, new ItemCreator(Material.BOOK).setName("&bBook").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(21, new ItemCreator(Material.GOLDEN_APPLE).setName("&bGolden Apple").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(22, new ItemCreator(new Potion(PotionType.WATER).toItemStack(1)).setName("&bWater Bottle").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(23, new ItemCreator(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1)).setName("&b").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(24, new ItemCreator(new Potion(PotionType.POISON, 1, true).toItemStack(1)).setName("&b").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
				inventory.setItem(25, new ItemCreator(new Potion(PotionType.INVISIBILITY, 1, true).toItemStack(1)).setName("&b").setLores(new String [] {"", "&7Price: ", ""}).getItemStack());
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
			int price = Integer.valueOf(item.getItemMeta().getLore().get(1).split(" ")[1]);
			CoinsHandler coinsHandler = CoinsHandler.getCoinsHandler(Plugins.PVP_BATTLES);
			if(coinsHandler.getCoins(player) >= price) {
				coinsHandler.addCoins(player, price * -1);
				ItemStack newItem = new ItemStack(item.getType(), item.getAmount(), item.getData().getData());
				player.getInventory().addItem(newItem);
			} else {
				player.closeInventory();
				new TitleDisplayer(player, "&cNot have enough coins", "&eFor " + item.getItemMeta().getDisplayName()).display();
			}
			event.setCancelled(true);
		}
	}
}
