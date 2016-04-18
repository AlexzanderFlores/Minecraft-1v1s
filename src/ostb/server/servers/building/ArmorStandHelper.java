package ostb.server.servers.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import npc.util.EventUtil;
import ostb.customevents.TimeEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.ItemCreator;

public class ArmorStandHelper implements Listener {
	private static ItemStack locked = null;
	private Map<ArmorStand, Location> standLocations = null;
	private Map<String, ArmorStand> editing = null;
	private Map<String, List<StandData>> copying = null;
	
	public class StandData {
		public ArmorStand armorStand = null;
		public double x = 0;
		public double y = 0;
		public double z = 0;
		
		public StandData(ArmorStand armorStand, double x, double y, double z) {
			this.armorStand = armorStand;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public ArmorStandHelper() {
		locked = new ItemCreator(Material.TRIPWIRE_HOOK).setName("&eToggle Movement Lock").getItemStack();
		standLocations = new HashMap<ArmorStand, Location>();
		editing = new HashMap<String, ArmorStand>();
		copying = new HashMap<String, List<StandData>>();
		EventUtil.register(this);
		new CommandBase("copyArmorStands", 1, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location pLoc = player.getLocation();
				if(arguments[0].equalsIgnoreCase("paste") && arguments.length == 1) {
					if(copying.containsKey(player.getName()) && !copying.get(player.getName()).isEmpty()) {
						for(StandData data : copying.get(player.getName())) {
							ArmorStand armorStand = data.armorStand;
							Location newLoc = pLoc.add(data.x, data.y, data.z);
							armorStand.teleport(newLoc);
						}
					} else {
						MessageHandler.sendMessage(sender, "&cYou do not have any armor stands copied! To copy run &e/copyArmorStands copy");
					}
				} else if(arguments[0].equalsIgnoreCase("copy") && arguments.length == 2) {
					try {
						double radius = Double.valueOf(arguments[1]);
						List<StandData> nearByStands = new ArrayList<StandData>();
						for(Entity entity : player.getNearbyEntities(radius, radius, radius)) {
							if(entity instanceof ArmorStand) {
								ArmorStand armorStand = (ArmorStand) entity;
								Location asLoc = armorStand.getLocation();
								double x = pLoc.getX() - asLoc.getX();
								double y = pLoc.getY() - asLoc.getY();
								double z = pLoc.getZ() - asLoc.getZ();
								ArmorStand clone = (ArmorStand) armorStand.getWorld().spawnEntity(asLoc, EntityType.ARMOR_STAND);
								clone.setArms(armorStand.hasArms());
								clone.setBasePlate(armorStand.hasBasePlate());
								clone.setBodyPose(armorStand.getBodyPose());
								clone.setBoots(armorStand.getBoots());
								clone.setCanPickupItems(armorStand.getCanPickupItems());
								clone.setChestplate(armorStand.getChestplate());
								clone.setCustomName(armorStand.getCustomName());
								clone.setGravity(armorStand.hasGravity());
								clone.setHeadPose(armorStand.getHeadPose());
								clone.setHelmet(armorStand.getHelmet());
								clone.setItemInHand(armorStand.getItemInHand());
								clone.setLastDamage(armorStand.getLastDamage());
								clone.setLastDamageCause(armorStand.getLastDamageCause());
								if(armorStand.isLeashed()) {
									clone.setLeashHolder(armorStand.getLeashHolder());
								}
								clone.setLeftArmPose(armorStand.getLeftArmPose());
								clone.setLeftLegPose(armorStand.getLeftLegPose());
								clone.setLeggings(armorStand.getLeggings());
								clone.setRemoveWhenFarAway(armorStand.getRemoveWhenFarAway());
								clone.setRightArmPose(armorStand.getRightArmPose());
								clone.setRightLegPose(armorStand.getRightLegPose());
								clone.setSmall(armorStand.isSmall());
								clone.setTicksLived(armorStand.getTicksLived());
								clone.setVisible(armorStand.isVisible());
								nearByStands.add(new StandData(clone, x, y, z));
							}
						}
						if(nearByStands.isEmpty()) {
							MessageHandler.sendMessage(sender, "&cThere are no armor stands near you within &e" + radius + " &cradius");
						} else {
							copying.put(player.getName(), nearByStands);
							MessageHandler.sendMessage(sender, "Copied &e" + nearByStands.size() + " &xarmor stands within &e" + radius + " &xradius of you. To paste run &e/copyArmorStands paste");
						}
					} catch(NumberFormatException e) {
						return false;
					}
				} else {
					return false;
				}
				return true;
			}
		};
	}
	
	private void update(Player player, ArmorStand armorStand) {
		InventoryView view = player.getOpenInventory();
		if(view != null && view.getTitle().equals("Armor Stand")) {
			boolean lockOn = standLocations.containsKey(armorStand);
			ItemCreator locked = new ItemCreator(ArmorStandHelper.locked.clone()).addLore("&bMovement Lock: " + (lockOn ? "&cLocked" : "&aUn-Locked"));
			view.setItem(view.getTopInventory().getSize() - 1, locked.getItemStack());
			editing.put(player.getName(), armorStand);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getEntity() instanceof ArmorStand) {
			final ArmorStand armorStand = (ArmorStand) event.getEntity();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					armorStand.setHelmet(new ItemStack(Material.AIR));
					armorStand.setChestplate(new ItemStack(Material.AIR));
					armorStand.setLeggings(new ItemStack(Material.AIR));
					armorStand.setBoots(new ItemStack(Material.AIR));					
				}
			}, 5);
		}
	}
	
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if(event.getRightClicked() instanceof ArmorStand) {
			final Player player = event.getPlayer();
			final ArmorStand armorStand = (ArmorStand) event.getRightClicked();
			ItemStack item = player.getItemInHand();
			if(item.getType() == Material.NETHER_STAR) {
				String name = item.getItemMeta().getDisplayName();
				if(name != null && ChatColor.stripColor(name).equals("GUI Multi-Tool")) {
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							update(player, armorStand);
						}
					});
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item.getType() == Material.NETHER_STAR) {
			String name = item.getItemMeta().getDisplayName();
			if(name != null && ChatColor.stripColor(name).equals("GUI Multi-Tool")) {
				InventoryView view = player.getOpenInventory();
				if(view != null && view.getTitle().equals("Armor Stand")) {
					ItemStack clicked = event.getItem();
					if(clicked.getType() == Material.TRIPWIRE_HOOK) {
						ArmorStand armorStand = editing.get(player.getName());
						if(standLocations.containsKey(armorStand)) {
							standLocations.remove(armorStand);
						} else {
							standLocations.put(armorStand, armorStand.getLocation());
						}
						update(player, armorStand);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 1) {
			for(ArmorStand stand : standLocations.keySet()) {
				Location location = standLocations.get(stand);
				stand.teleport(location);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		editing.remove(event.getPlayer().getName());
	}
}
