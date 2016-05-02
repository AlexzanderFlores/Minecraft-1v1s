package ostb.gameapi.games.pvpbattles;

import java.util.HashMap;
import java.util.Map;

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

import npc.NPCEntity;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.TitleDisplayer;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.StringUtil;

public class Armory implements Listener {
	private static boolean registered = false;
	private String name = null;
	private Map<Material, Integer> armorCosts = null;
	
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
		armorCosts = new HashMap<Material, Integer>();
		armorCosts.put(Material.LEATHER_CHESTPLATE, 1);
		armorCosts.put(Material.GOLD_CHESTPLATE, 2);
		armorCosts.put(Material.CHAINMAIL_CHESTPLATE, 3);
		armorCosts.put(Material.IRON_CHESTPLATE, 4);
		new NPCEntity(EntityType.ZOMBIE, "&e&n" + name, location) {
			@Override
			public void onInteract(Player player) {
				int repairCost = getRepairCost(player);
				Inventory inventory = Bukkit.createInventory(player, 9 * (repairCost == -1 ? 3 : 5), name);
				inventory.setItem(10, new ItemCreator(Material.LEATHER_CHESTPLATE).setName("&bLeather Armor").setLores(new String [] {
					"",
					"&7Cost: &e" + armorCosts.get(Material.LEATHER_CHESTPLATE) + " Level",
					""
				}).getItemStack());
				inventory.setItem(12, new ItemCreator(Material.GOLD_CHESTPLATE).setName("&bGold Armor").setLores(new String [] {
					"",
					"&7Cost: &e" + armorCosts.get(Material.GOLD_CHESTPLATE) + " Levels",
					""
				}).getItemStack());
				inventory.setItem(14, new ItemCreator(Material.CHAINMAIL_CHESTPLATE).setName("&bChain Armor").setLores(new String [] {
					"",
					"&7Cost: &e" + armorCosts.get(Material.CHAINMAIL_CHESTPLATE) + " Levels",
					""
				}).getItemStack());
				inventory.setItem(16, new ItemCreator(Material.IRON_CHESTPLATE).setName("&bIron Armor").setLores(new String [] {
					"",
					"&7Cost: &e" + armorCosts.get(Material.IRON_CHESTPLATE) + " Levels",
					""
				}).getItemStack());
				if(repairCost > -1) {
					inventory.setItem(31, new ItemCreator(Material.ANVIL).setName("&bRepair Armor").setLores(new String [] {
						"",
						"&7Cost: &e" + repairCost + " Levels",
						"",
						"&7Repairs the armor you",
						"&7have equip currently",
						""
					}).getItemStack());
				}
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
	
	private int getRepairCost(Player player) {
		ItemStack chest = player.getInventory().getChestplate();
		if(chest != null && armorCosts.containsKey(chest.getType())) {
			return armorCosts.get(chest.getType()) - 1;
		}
		return -1;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			Material type = event.getItem().getType();
			ItemStack [] items = null;
			if(type == Material.LEATHER_CHESTPLATE) {
				items = getItems(new Material [] {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET});
			} else if(type == Material.GOLD_CHESTPLATE) {
				items = getItems(new Material [] {Material.GOLD_BOOTS, Material.GOLD_LEGGINGS, Material.GOLD_CHESTPLATE, Material.GOLD_HELMET});
			} else if(type == Material.CHAINMAIL_CHESTPLATE) {
				items = getItems(new Material [] {Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET});
			} else if(type == Material.IRON_CHESTPLATE) {
				items = getItems(new Material [] {Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET});
			} else if(type == Material.ANVIL) {
				int cost = getRepairCost(player);
				if(player.getLevel() >= cost) {
					player.setLevel(player.getLevel() - cost);
					for(ItemStack item : player.getInventory().getArmorContents()) {
						if(item != null && item.getType() != Material.AIR) {
							item.setDurability((short) -1);
						}
					}
					new TitleDisplayer(player, "&bArmor Repaired").display();
					player.closeInventory();
				} else {
					new TitleDisplayer(player, "&cNot Enough Levels").display();
					player.closeInventory();
				}
			}
			if(items != null) {
				int cost = armorCosts.get(type);
				if(player.getLevel() >= cost) {
					player.setLevel(player.getLevel() - cost);
					player.getInventory().setArmorContents(items);
					new TitleDisplayer(player, "&e" + StringUtil.getFirstLetterCap(type.toString().split("_")[0]) + " Armor", "&ePurchased").display();
					player.closeInventory();
				} else {
					new TitleDisplayer(player, "&cNot Enough Levels").display();
					player.closeInventory();
				}
			}
			event.setCancelled(true);
		}
	}
}
