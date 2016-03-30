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

import npc.ostb.NPCEntity;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.MessageHandler;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class Armory implements Listener {
	private static boolean registered = false;
	private String name = null;
	
	public Armory(World world) {
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/armory.yml");
		double x = config.getConfig().getDouble("red.location.x");
		double y = config.getConfig().getDouble("red.location.y");
		double z = config.getConfig().getDouble("red.location.z");
		float yaw = (float) config.getConfig().getDouble("red.location.yaw");
		float pitch = (float) config.getConfig().getDouble("red.location.pitch");
		new Armory(new Location(world, x, y, z, yaw, pitch));
		x = config.getConfig().getDouble("blue.location.x");
		y = config.getConfig().getDouble("blue.location.y");
		z = config.getConfig().getDouble("blue.location.z");
		yaw = (float) config.getConfig().getDouble("blue.location.yaw");
		pitch = (float) config.getConfig().getDouble("blue.location.pitch");
		new Armory(new Location(world, x, y, z, yaw, pitch));
	}
	
	public Armory(Location location) {
		name = "Armory";
		new NPCEntity(EntityType.SKELETON, "&e&n" + name, location) {
			@Override
			public void onInteract(Player player) {
				Inventory inventory = Bukkit.createInventory(player, 9 * 5, name);
				inventory.setItem(10, new ItemCreator(Material.LEATHER_CHESTPLATE).setName("&bLeather Armor").setLores(new String [] {"", "&7Cost: &e1 Level"}).getItemStack());
				inventory.setItem(12, new ItemCreator(Material.GOLD_CHESTPLATE).setName("&bGold Armor").setLores(new String [] {"", "&7Cost: &e2 Levels"}).getItemStack());
				inventory.setItem(14, new ItemCreator(Material.CHAINMAIL_CHESTPLATE).setName("&bChain Armor").setLores(new String [] {"", "&7Cost: &e3 Levels"}).getItemStack());
				inventory.setItem(16, new ItemCreator(Material.IRON_CHESTPLATE).setName("&bIron Armor").setLores(new String [] {"", "&7Cost: &e4 Levels"}).getItemStack());
				inventory.setItem(31, new ItemCreator(Material.ANVIL).setName("&bRepair Armor").setLores(new String [] {"", "&7Cost: &e3 Levels", "&7Repairs the armor you", "&7have equip currently"}).getItemStack());
				player.openInventory(inventory);
			}
		};
		if(!registered) {
			registered = true;
			EventUtil.register(this);
		}
	}
	
	private ItemStack [] getItems(Material [] materials) {
		ItemStack [] items = new ItemStack[materials.length];
		for(int a = 0; a < materials.length; ++a) {
			items[a] = new ItemStack(materials[a]);
		}
		return items;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			String message = "&cYou do not have enough levels for this item";
			Material type = event.getItem().getType();
			ItemStack [] items = null;
			int required = 0;
			if(type == Material.LEATHER_CHESTPLATE) {
				items = getItems(new Material [] {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET});
				required = 1;
			} else if(type == Material.GOLD_CHESTPLATE) {
				items = getItems(new Material [] {Material.GOLD_BOOTS, Material.GOLD_LEGGINGS, Material.GOLD_CHESTPLATE, Material.GOLD_HELMET});
				required = 2;
			} else if(type == Material.CHAINMAIL_CHESTPLATE) {
				items = getItems(new Material [] {Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET});
				required = 3;
			} else if(type == Material.IRON_CHESTPLATE) {
				items = getItems(new Material [] {Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET});
				required = 4;
			} else if(type == Material.ANVIL) {
				if(player.getLevel() >= 3) {
					player.setLevel(player.getLevel() - 3);
					for(ItemStack item : player.getInventory().getArmorContents()) {
						if(item != null && item.getType() != Material.AIR) {
							item.setDurability((short) -1);
						}
					}
					MessageHandler.sendMessage(player, "Armor repaired");
					player.closeInventory();
				} else {
					MessageHandler.sendMessage(player, message);
				}
			}
			if(items != null && required > 0) {
				if(player.getLevel() >= required) {
					player.setLevel(player.getLevel() - required);
					player.getInventory().setArmorContents(items);
					MessageHandler.sendMessage(player, "You have equipped &e" + type.toString().split("_")[0] + " &xarmor");
					player.closeInventory();
				} else {
					MessageHandler.sendMessage(player, message);
				}
			}
			event.setCancelled(true);
		}
	}
}
