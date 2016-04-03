package ostb.gameapi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import ostb.ProPlugin;
import ostb.customevents.ServerRestartEvent;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemUtil;
import ostb.server.util.StringUtil;

public class KitSelection implements Listener {
	private String name = null;
	private KitBase viewing = null;
	private List<ArmorStand> stands = null;
	private List<ArmorStand> aboveStands = null;
	private ArmorStand indexStand = null;
	private boolean delayed = false;
	private int index = 0;
	
	public KitSelection(Player player) {
		name = player.getName();
		EventUtil.register(this);
		for(Entity entity : player.getWorld().getEntities()) {
			if(entity instanceof ArmorStand) {
				entity.remove();
			}
		}
		stands = new ArrayList<ArmorStand>(3);
		aboveStands = new ArrayList<ArmorStand>(3);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 128));
		index = 0;
	}
	
	public Player getPlayer() {
		return ProPlugin.getPlayer(name);
	}
	
	public void update() {
		KitBase kit = KitBase.getKits().get(0);
		if(stands.isEmpty()) {
			spawnFirstNPCs(kit);
			return;
		}
		for(ArmorStand armorStand : stands) {
			if(armorStand == null || armorStand.isDead()) {
				spawnFirstNPCs(kit);
				return;
			}
		}
		updateKit(kit);
	}
	
	private void spawnFirstNPCs(KitBase kit) {
		Player player = getPlayer();
		if(player != null) {
			for(ArmorStand armorStand : stands) {
				armorStand.remove();
			}
			for(int a = 0; a < 3; ++a) {
				Location location = player.getLocation();
				location.setPitch(0.0f);
				float movement = 20;
				float yaw = location.getYaw() + (a == 0 ? -movement : a == 2 ? movement : 0);
				location.setYaw(yaw);
				for(int b = 0; b < (a == 1 ? 3 : 5); ++b) {
					location = location.clone().add(location.getDirection());
				}
				location = location.setDirection(player.getLocation().toVector().subtract(location.toVector()));
				ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
				armorStand.setGravity(false);
				armorStand.setBasePlate(false);
				armorStand.setArms(true);
				armorStand.setLeftArmPose(new EulerAngle(50, 0, 50));
				armorStand.setRightArmPose(new EulerAngle(250, 0, 50));
				ArmorStand aboveStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 0.35, 0), EntityType.ARMOR_STAND);
				aboveStand.setGravity(false);
				aboveStand.setVisible(false);
				aboveStand.setCustomNameVisible(true);
				if(a == 0) {
					armorStand.setCustomName(StringUtil.color("&7MOVE LEFT TO VIEW"));
				} else if(a == 1) {
					indexStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, .70, 0), EntityType.ARMOR_STAND);
					indexStand.setGravity(false);
					indexStand.setVisible(false);
					indexStand.setCustomNameVisible(true);
				} else if(a == 2) {
					armorStand.setCustomName(StringUtil.color("&7MOVE RIGHT TO VIEW"));
				}
				armorStand.setCustomNameVisible(true);
				stands.add(armorStand);
				aboveStands.add(aboveStand);
			}
			updateKit(kit);
		}
	}
	
	private void updateKit(KitBase kit) {
		if(!stands.isEmpty()) {
			Player player = getPlayer();
			
			// Front armor stand
			ArmorStand armorStand = stands.get(1);
			ArmorStand aboveStand = aboveStands.get(1);
			if(kit.owns(getPlayer())) {
				aboveStand.setCustomName(StringUtil.color("&a&lCLICK TO SELECT"));
			} else {
				aboveStand.setCustomName(StringUtil.color("&8&lLOCKED"));
			}
			armorStand.setCustomName(StringUtil.color("&e&n" + kit.getName()));
			kit.executeArt(armorStand, true, player);
			viewing = kit;
			
			// Left armor stand
			armorStand = stands.get(0);
			aboveStand = aboveStands.get(0);
			if(index > 0) {
				KitBase leftKit = KitBase.getKits().get(index - 1);
				aboveStand.setCustomName(StringUtil.color("&6" + leftKit.getName()));
				leftKit.executeArt(armorStand, false, player);
			} else {
				aboveStand.setCustomName(StringUtil.color("&f"));
				setToNone(armorStand);
			}
			
			// Right armor stand
			armorStand = stands.get(2);
			aboveStand = aboveStands.get(2);
			if(index < KitBase.getKits().size() - 1) {
				KitBase rightKit = KitBase.getKits().get(index + 1);
				aboveStand.setCustomName(StringUtil.color("&6" + rightKit.getName()));
				rightKit.executeArt(armorStand, false, player);
			} else {
				aboveStand.setCustomName(StringUtil.color("&f"));
				setToNone(armorStand);
			}
			indexStand.setCustomName(StringUtil.color("&a&l" + (index + 1) + "&e&l/&a&l" + (KitBase.getKits().size())));
		}
	}
	
	private void setToNone(ArmorStand armorStand) {
		armorStand.setHelmet(ItemUtil.getSkull("None"));
		armorStand.setChestplate(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.fromRGB(0, 0, 0)));
		armorStand.setLeggings(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.fromRGB(0, 0, 0)));
		armorStand.setBoots(ItemUtil.colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.fromRGB(0, 0, 0)));
		armorStand.setItemInHand(new ItemStack(Material.AIR));
		armorStand.setLeftArmPose(new EulerAngle(50, 0, 50));
		armorStand.setRightArmPose(new EulerAngle(50, 0, 50));
	}
	
	public void delete() {
		HandlerList.unregisterAll(this);
		Player player = getPlayer();
		if(player != null) {
			player.removePotionEffect(PotionEffectType.JUMP);
		}
		name = null;
		viewing = null;
		for(ArmorStand stand : stands) {
			stand.remove();
		}
		stands.clear();
		stands = null;
		for(ArmorStand above : aboveStands) {
			above.remove();
		}
		aboveStands.clear();
		aboveStands = null;
		indexStand.remove();
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(name.equals(event.getPlayer().getName()) && !stands.isEmpty()) {
			Location to = event.getTo();
			Location from = event.getFrom();
			double x1 = to.getX();
			double z1 = to.getZ();
			double x2 = from.getX();
			double z2 = from.getZ();
			if(x1 != x2 || z1 != z2) {
				if(!delayed) {
					Player player = event.getPlayer();
					delayed = true;
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed = false;
						}
					}, 10);
					double leftDistance = stands.get(0).getLocation().distance(to);
					double rightDistance = stands.get(2).getLocation().distance(to);
					if(index == -1) {
						index = viewing == null ? 0 : KitBase.getKits().indexOf(viewing);
					}
					if(leftDistance < rightDistance) {
						if(index > 0) {
							KitBase.getKits().get(index).disableArt(stands.get(0));
							--index;
							EffectUtil.playSound(player, Sound.NOTE_BASS);
						} else {
							EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						}
					} else {
						if(index < KitBase.getKits().size() - 1) {
							KitBase.getKits().get(index).disableArt(stands.get(2));
							++index;
							EffectUtil.playSound(player, Sound.NOTE_BASS);
						} else {
							EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						}
					}
					updateKit(KitBase.getKits().get(index));
				}
				event.setTo(from);
			}
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		delete();
	}
}