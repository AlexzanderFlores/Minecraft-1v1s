package network.server.servers.hub.crate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import network.customevents.TimeEvent;
import network.player.MessageHandler;
import network.player.TitleDisplayer;
import network.player.account.AccountHandler;
import network.server.ChatClickHandler;
import network.server.DB;
import network.server.servers.hub.items.Features.Rarity;
import network.server.servers.hub.items.features.FeatureItem;
import network.server.servers.hub.items.features.FeatureItem.FeatureType;
import network.server.tasks.AsyncDelayedTask;
import network.server.tasks.DelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.Hologram;
import network.server.util.TimeUtil;

@SuppressWarnings({"deprecation", "unused"})
public class Beacon implements Listener {
	private Random random = null;
	private Block glass = null;
	private final String originalName;
	private String type = null;
	private Hologram armorStand = null;
	private int counter = 0;
	private boolean running = false;
	private boolean displaying = false;
	private List<FeatureItem> items = null;
	private List<String> delayed = null;
	private static final int delay = 2;
	private static String keyFragmentName = null;
	private static String votingKeyx3Name = null;
	private static String skyWarsCrateKey = null;
	private static String speedUHCCrateKey = null;
	
	public Beacon(String originalName, String type, Block glass, Vector standOffset) {
		random = new Random();
		this.glass = glass;
		this.originalName = originalName;
		this.type = type;
		Location standLoc = glass.getLocation().add(standOffset);
		standLoc.setYaw(-90.0f);
		standLoc.setPitch(0.0f);
		/*armorStand = (ArmorStand) Bukkit.getWorlds().get(0).spawnEntity(standLoc, EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomName(getName());
		armorStand.setCustomNameVisible(true);*/
		delayed = new ArrayList<String>();
		setWood();
		keyFragmentName = "Key Fragment";
		votingKeyx3Name = "Voting Key x3";
		skyWarsCrateKey = "Sky Wars Crate Key";
		speedUHCCrateKey = "Speed UHC Elimination Crate Key";
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				items = FeatureItem.getItems(FeatureType.REWARD_CRATE);
				items.add(new FeatureItem(getKeyFragmentName(), new ItemStack(Material.TRIPWIRE_HOOK), Rarity.UNCOMMON, FeatureType.REWARD_CRATE));
				items.add(new FeatureItem(getVotingKeyx3(), new ItemStack(Material.TRIPWIRE_HOOK), Rarity.RARE, FeatureType.REWARD_CRATE));
				items.add(new FeatureItem(getSkyWarsCrateKey(), new ItemStack(Material.TRIPWIRE_HOOK), Rarity.UNCOMMON, FeatureType.REWARD_CRATE));
				items.add(new FeatureItem(getSpeedUHCCrateKey(), new ItemStack(Material.TRIPWIRE_HOOK), Rarity.UNCOMMON, FeatureType.REWARD_CRATE));
			}
		});
		EventUtil.register(this);
	}
	
	public static String getKeyFragmentName() {
		return keyFragmentName;
	}
	
	public static String getVotingKeyx3() {
		return votingKeyx3Name;
	}
	
	public static String getSkyWarsCrateKey() {
		return skyWarsCrateKey;
	}
	
	public static String getSpeedUHCCrateKey() {
		return speedUHCCrateKey;
	}
	
	public String getType() {
		return type;
	}
	
	public static void giveKey(final UUID uuid, final int toAdd, final String type) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String [] keys = new String [] {"uuid", "type"};
				String [] values = new String [] {uuid.toString(), type.toLowerCase()};
				if(DB.HUB_CRATE_KEYS.isKeySet(keys, values)) {
					int amount = DB.HUB_CRATE_KEYS.getInt(keys, values, "amount") + toAdd;
					DB.HUB_CRATE_KEYS.updateInt("amount", amount, keys, values);
				} else {
					DB.HUB_CRATE_KEYS.insert("'" + uuid + "', '" + type.toLowerCase() + "', '" + toAdd + "'");
				}
				Bukkit.getLogger().info(type.toLowerCase() + ": give player key");
			}
		});
	}
	
	private void setWood() {
		glass.setType(Material.WOOD);
		glass.setData((byte) 3);
	}
	
	private void activate(final Player player) {
		if(delayed.contains(player.getName())) {
			return;
		} else {
			delayed.add(player.getName());
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(player.getName());
				}
			}, 20 * delay);
		}
		final String [] keys = new String [] {"uuid", "type"};
		final String [] values = new String [] {player.getUniqueId().toString(), type};
		if(DB.HUB_CRATE_KEYS.getInt(keys, values, "amount") <= 0) {
			if(type.equals("voting")) {
				ChatClickHandler.sendMessageToRunCommand(player, "&aClick here", "Click to vote", "/vote", "&cYou don't have any &bVoting Keys&c! Get some by voting ");
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have any &bSuper Keys&c! Get some on Buycraft: &ahttp://store.outsidetheblock.org/category/679824");
			}
			return;
		}
		running = true;
		glass.setType(Material.STAINED_GLASS);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				FeatureItem item = null;
				int chance = random.nextInt(100) + 1;
				Rarity rarity = type.equals("super") ? Rarity.RARE : chance <= 10 ? Rarity.RARE : chance <= 35 ? Rarity.UNCOMMON : Rarity.COMMON;
				do {
					item = items.get(random.nextInt(items.size()));
				} while(item.getRarity() != rarity);
				setItem(item);
				setWood();
				displaying = true;
				if(item != null && player.isOnline()) {
					item.give(player);
					final String log = item.getName();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							String uuid = player.getUniqueId().toString();
							int owned = DB.HUB_CRATE_KEYS.getInt(keys, values, "amount") - 1;
							if(owned <= 0) {
								DB.HUB_CRATE_KEYS.delete(keys, values);
								owned = 0;
							} else {
								DB.HUB_CRATE_KEYS.updateInt("amount", owned, keys, values);
							}
							Bukkit.getLogger().info(type + ": update key amount");
							MessageHandler.sendMessage(player, "You now have &e" + owned + " &xVoting Crate key" + (owned == 1 ? "" : "s") + " left");
							if(DB.HUB_LIFETIME_CRATES_OPENED.isUUIDSet(player.getUniqueId())) {
								int amount = DB.HUB_LIFETIME_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_LIFETIME_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_LIFETIME_CRATES_OPENED.insert("'" + player.getUniqueId().toString() + "', '" + type + "', '" + 1 + "'");
							}
							Bukkit.getLogger().info(type + ": update lifetime crates used");
							Calendar calendar = Calendar.getInstance();
							String month = calendar.get(Calendar.MONTH) + "";
							String [] keys = new String [] {"uuid", "type", "month"};
							String [] values = new String [] {uuid, type, month};
							if(DB.HUB_MONTHLY_CRATES_OPENED.isKeySet(keys, values)) {
								int amount = DB.HUB_MONTHLY_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_MONTHLY_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_MONTHLY_CRATES_OPENED.insert("'" + uuid + "', '" + type + "', '1', '" + month + "'");
							}
							Bukkit.getLogger().info(type + ": update monthly crates used");
							String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
							keys[2] = "week";
							values[2] = week;
							if(DB.HUB_WEEKLY_CRATES_OPENED.isKeySet(keys, values)) {
								int amount = DB.HUB_WEEKLY_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_WEEKLY_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_WEEKLY_CRATES_OPENED.insert("'" + uuid + "', '" + type + "', '1', '" + week + "'");
							}
							Bukkit.getLogger().info(type + ": update weekly crates used");
							DB.HUB_CRATE_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + type + "', '" + log + "', '" + TimeUtil.getTime() + "'");
							Bukkit.getLogger().info(type + ": update log");
						}
					});
					String rareString = "&8(" + item.getRarity().getName() + "&8)";
					MessageHandler.sendMessage(player, "&6You opened &c" + item.getName() + " " + rareString);
					if(item.getRarity() == Rarity.RARE) {
						for(Player online : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(online, "&e" + player.getName() + " Opened", "&c" + item.getName() + " " + rareString).setStay(40).display();
							MessageHandler.sendMessage(online, AccountHandler.getRank(player).getColor() + player.getName() + " &xOpened &c" + item.getName() + " " + rareString);
						}
					}
				}
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						counter = 0;
						/*armorStand.setCustomName(getName());
						if(armorStand.getPassenger() != null) {
							armorStand.getPassenger().remove();
						}*/
						running = false;
						displaying = false;
					}
				}, 20 * 5);
			}
		}, 20 * 7 + 10);
	}
	
	/*private String getName() {
		if(counter > 0) {
			return StringUtil.color("&e&n" + originalName.substring(counter, originalName.length() - counter)).replace("&", "");
		}
		return StringUtil.color("&e&n" + originalName);
	}*/
	
	private void setItem() {
		setItem(null);
	}
	
	private void setItem(FeatureItem featureItem) {
		/*if(armorStand.getPassenger() == null) {
			if(featureItem == null) {
				featureItem = items.get(random.nextInt(items.size()));
			}
			Item item = armorStand.getWorld().dropItemNaturally(armorStand.getLocation(), featureItem.getItemStack());
			armorStand.setPassenger(item);
		} else {
			Item item = (Item) armorStand.getPassenger();
			ItemStack itemStack = null;
			if(featureItem == null) {
				do {
					featureItem = items.get(random.nextInt(items.size()));
					itemStack = featureItem.getItemStack();
				} while(itemStack.equals(item.getItemStack()));
			} else {
				itemStack = featureItem.getItemStack();
			}
			item.setItemStack(itemStack);
		}
		armorStand.setCustomName(StringUtil.color("&b&n" + featureItem.getName()));*/
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 2) {
			if(running && !displaying) {
				if(counter <= 12) {
					//armorStand.setCustomName(getName());
					++counter;
				}
			}
		} else if(ticks == 5) {
			if(running && !displaying) {
				glass.setData((byte) random.nextInt(15));
				EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, glass.getLocation());
				//ParticleTypes.FIREWORK_SPARK.display(glass.getLocation().add(0, 2, 0));
				if(counter <= 12) {
					//armorStand.setCustomName(getName());
					++counter;
				}
				if(counter > 12) {
					setItem();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
			Block block = event.getClickedBlock();
			Location loc = block.getLocation();
			if(loc.getBlockX() == glass.getX() && loc.getBlockY() - 1 == glass.getY() && loc.getBlockZ() == glass.getZ() && !running) {
				activate(event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && event.getEntity().equals(armorStand) && !running) {
			Player player = (Player) event.getDamager();
			activate(player);
		}
	}
}