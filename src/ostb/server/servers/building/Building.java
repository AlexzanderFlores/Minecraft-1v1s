package ostb.server.servers.building;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ItemCreator;
import ostb.server.util.UnicodeUtil;

public class Building extends ProPlugin {
	public Building() {
		super("Building");
		addGroup("24/7");
		new Events();
		removeFlags();
		setAllowLeavesDecay(false);
		setAllowDefaultMobSpawning(false);
		new CommandBase("setGameSpawn", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/spawns.yml");
				int index = -1;
				if(arguments.length == 0) {
					index = config.getConfig().getKeys(false).size() + 1;
				} else if(arguments.length == 1) {
					try {
						index = Integer.valueOf(arguments[0]);
					} catch(NumberFormatException e) {
						return false;
					}
				}
				String loc = (location.getBlockX() + "0.5,") + (location.getBlockY() + 1) + "," + (location.getBlockZ() + "0.5,");
				config.getConfig().set(index + "", loc);
				config.save();
				MessageHandler.sendMessage(player, "Set spawn " + index);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("test", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				String fullStar = " " + UnicodeUtil.getUnicode("2726");
				String emptyStar = " &7" + UnicodeUtil.getUnicode("2727");
				Inventory inventory = Bukkit.createInventory(player, 9, "Testing");
				inventory.setItem(0, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + emptyStar + emptyStar + emptyStar + emptyStar).getItemStack());
				inventory.setItem(2, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + emptyStar + emptyStar + emptyStar).setAmount(2).getItemStack());
				inventory.setItem(4, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + emptyStar + emptyStar).setAmount(3).getItemStack());
				inventory.setItem(6, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + fullStar + emptyStar).setAmount(4).getItemStack());
				inventory.setItem(8, new ItemCreator(Material.NETHER_STAR).setName("&6" + fullStar + fullStar + fullStar + fullStar + fullStar).setAmount(5).getItemStack());
				player.openInventory(inventory);
				return true;
			}
		};
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof ItemFrame) {
			event.setCancelled(true);
			ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
			//new ImageMap(itemFrame, OSTB.getInstance().getDataFolder().getPath() + "/Mines.png");
			Location loc = itemFrame.getLocation();
			event.getPlayer().sendMessage(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
		}
	}
}
