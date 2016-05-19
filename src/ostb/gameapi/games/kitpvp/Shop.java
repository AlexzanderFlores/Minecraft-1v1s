package ostb.gameapi.games.kitpvp;

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

import npc.NPCEntity;
import ostb.OSTB.Plugins;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.SpectatorHandler;
import ostb.player.CoinsHandler;
import ostb.player.TitleDisplayer;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

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
