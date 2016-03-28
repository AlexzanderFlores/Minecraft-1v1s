package ostb.server.servers.hub.crate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.timed.FiveTickTaskEvent;
import ostb.customevents.timed.TwoTickTaskEvent;
import ostb.player.MessageHandler;
import ostb.player.Particles.ParticleTypes;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.TitleDisplayer;
import ostb.server.ChatClickHandler;
import ostb.server.CommandBase;
import ostb.server.DB;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.servers.hub.items.features.FeatureItem;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class Beacon implements Listener {
	private Random random = null;
	private World world = null;
	private Block glass = null;
	private final String originalName;
	private ArmorStand armorStand = null;
	private int counter = 0;
	private boolean running = false;
	private boolean displaying = false;
	private List<FeatureItem> items = null;
	private static Map<String, Integer> keys = null;
	
	public Beacon() {
		random = new Random();
		world = Bukkit.getWorlds().get(0);
		glass = world.getBlockAt(1651, 6, -1281);
		originalName = "Reward Crate&8 (&7Click&8)";
		Location standLoc = glass.getLocation().add(0.85, 0.75, 0.5);
		standLoc.setYaw(-90.0f);
		standLoc.setPitch(0.0f);
		armorStand = (ArmorStand) world.spawnEntity(standLoc, EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomName(getName());
		armorStand.setCustomNameVisible(true);
		keys = new HashMap<String, Integer>();
		setWood();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				items = FeatureItem.getItems();
			}
		});
		new CommandBase("giveKey", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				giveKey(uuid, Integer.valueOf(arguments[1]));
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("test", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.sendBlockChange(glass.getLocation(), Material.STAINED_GLASS, (byte) random.nextInt(15));
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static void giveKey(final UUID uuid, final int toAdd) {
		Player player = Bukkit.getPlayer(uuid);
		if(player != null && keys.containsKey(player.getName())) {
			keys.put(player.getName(), toAdd + keys.get(player.getName()));
		}
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(DB.HUB_CRATE_KEYS.isUUIDSet(uuid)) {
					int amount = DB.HUB_CRATE_KEYS.getInt("uuid", uuid.toString(), "amount") + toAdd;
					DB.HUB_CRATE_KEYS.updateInt("amount", amount, "uuid", uuid.toString());
				} else {
					DB.HUB_CRATE_KEYS.insert("'" + uuid + "', '1'");
				}
				Bukkit.getLogger().info("beacon: give player key");
			}
		});
	}
	
	private void setWood() {
		glass.setType(Material.WOOD);
		glass.setData((byte) 3);
	}
	
	private void activate(final Player player) {
		if(!keys.containsKey(player.getName())) {
			keys.put(player.getName(), DB.HUB_CRATE_KEYS.getInt("uuid", player.getUniqueId().toString(), "amount"));
			Bukkit.getLogger().info("beacon: load crate keys");
		}
		if(keys.get(player.getName()) <= 0) {
			ChatClickHandler.sendMessageToRunCommand(player, "&6click here", "Click to vote", "/vote", "&cYou do not have any &2Crate Keys&c! Get some by voting, ");
			return;
		}
		running = true;
		glass.setType(Material.STAINED_GLASS);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				FeatureItem item = null;
				int chance = random.nextInt(100) + 1;
				Rarity rarity = chance <= 10 ? Rarity.RARE : chance <= 35 ? Rarity.UNCOMMON : Rarity.COMMON;
				do {
					item = items.get(random.nextInt(items.size()));
				} while(item.getRarity() != rarity);
				setItem(item);
				setWood();
				displaying = true;
				if(item != null && player.isOnline()) {
					item.give(player);
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							String uuid = player.getUniqueId().toString();
							int owned = DB.HUB_CRATE_KEYS.getInt("uuid", uuid, "amount") - 1;
							if(owned <= 0) {
								DB.HUB_CRATE_KEYS.deleteUUID(player.getUniqueId());
								owned = 0;
							} else {
								DB.HUB_CRATE_KEYS.updateInt("amount", owned, "uuid", uuid);
							}
							Bukkit.getLogger().info("beacon: update key amount");
							keys.put(player.getName(), owned);
							MessageHandler.sendMessage(player, "You now have &e" + owned + " &xCrate Key" + (owned == 1 ? "" : "s"));
							if(DB.HUB_LIFETIME_CRATES_OPENED.isUUIDSet(player.getUniqueId())) {
								int amount = DB.HUB_LIFETIME_CRATES_OPENED.getInt("uuid", uuid, "amount") + 1;
								DB.HUB_LIFETIME_CRATES_OPENED.updateInt("amount", amount, "uuid", uuid);
							} else {
								DB.HUB_LIFETIME_CRATES_OPENED.insert("'" + player.getUniqueId().toString() + "', '" + 1 + "'");
							}
							Bukkit.getLogger().info("beacon: update lifetime crates used");
							Calendar calendar = Calendar.getInstance();
							String month = calendar.get(Calendar.MONTH) + "";
							String [] keys = new String [] {"uuid", "month"};
							String [] values = new String [] {uuid, month};
							if(DB.HUB_MONTHLY_CRATES_OPENED.isKeySet(keys, values)) {
								int amount = DB.HUB_MONTHLY_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_MONTHLY_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_MONTHLY_CRATES_OPENED.insert("'" + uuid + "', '1', '" + month + "'");
							}
							Bukkit.getLogger().info("beacon: update monthly crates used");
							String week = calendar.get(Calendar.WEEK_OF_YEAR) + "";
							keys[1] = "week";
							values[1] = week;
							if(DB.HUB_WEEKLY_CRATES_OPENED.isKeySet(keys, values)) {
								int amount = DB.HUB_WEEKLY_CRATES_OPENED.getInt(keys, values, "amount") + 1;
								DB.HUB_WEEKLY_CRATES_OPENED.updateInt("amount", amount, keys, values);
							} else {
								DB.HUB_WEEKLY_CRATES_OPENED.insert("'" + uuid + "', '1', '" + week + "'");
							}
							Bukkit.getLogger().info("beacon: update weekly crates used");
						}
					});
					String rareString = "&8(" + item.getRarity().getName() + "&8)";
					MessageHandler.sendMessage(player, "&6Reward > You opened &c" + item.getName() + " " + rareString);
					final String log = item.getName();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.HUB_CRATE_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + log + "'");
						}
					});
					if(item.getRarity() == Rarity.RARE) {
						for(Player online : Bukkit.getOnlinePlayers()) {
							new TitleDisplayer(online, "&e" + player.getName() + " Opened", "&c" + item.getName() + " " + rareString).setStay(40).display();
						}
					}
				}
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						counter = 0;
						armorStand.setCustomName(getName());
						if(armorStand.getPassenger() != null) {
							armorStand.getPassenger().remove();
						}
						running = false;
						displaying = false;
					}
				}, 20 * 5);
			}
		}, 20 * 10);
	}
	
	private String getName() {
		if(counter > 0) {
			return StringUtil.color("&e&n" + originalName.substring(counter, originalName.length() - counter)).replace("&", "");
		}
		return StringUtil.color("&e&n" + originalName);
	}
	
	private void setItem() {
		setItem(null);
	}
	
	private void setItem(FeatureItem featureItem) {
		if(armorStand.getPassenger() == null) {
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
		armorStand.setCustomName(StringUtil.color("&b&n" + featureItem.getName()));
	}
	
	@EventHandler
	public void onTwoTickTask(TwoTickTaskEvent event) {
		if(running && !displaying) {
			if(counter <= 12) {
				armorStand.setCustomName(getName());
				++counter;
			}
		}
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(running && !displaying) {
			glass.setData((byte) random.nextInt(15));
			EffectUtil.playSound(random.nextBoolean() ? Sound.FIREWORK_BLAST : Sound.FIREWORK_BLAST2, glass.getLocation());
			ParticleTypes.FIREWORK_SPARK.display(glass.getLocation().add(0, 2, 0));
			if(counter <= 12) {
				armorStand.setCustomName(getName());
				++counter;
			}
			if(counter > 12) {
				setItem();
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
	
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if(event.getRightClicked().equals(armorStand) && !running) {
			activate(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		keys.remove(event.getPlayer().getName());
	}
}
