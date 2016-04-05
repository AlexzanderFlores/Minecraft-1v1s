package ostb.server.servers.hub;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import npc.ostb.NPCEntity;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.MessageHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.Glow;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class DailyRewards implements Listener {
	private String name = null;
	private String rewardsName = null;
	private String streakName = null;
	
	public DailyRewards() {
		name = "Daily Rewards";
		rewardsName = "Rewards";
		streakName = "Streaks";
		Villager villager = (Villager) new NPCEntity(EntityType.VILLAGER, "&e&n" + name, new Location(Bukkit.getWorlds().get(0), 1686.5, 5, -1295.5)) {
			@Override
			public void onInteract(Player player) {
				open(player);
				EffectUtil.playSound(player, Sound.VILLAGER_IDLE);
			}
		}.getLivingEntity();
		villager.setProfession(Profession.LIBRARIAN);
		EventUtil.register(this);
	}
	
	private void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		inventory.setItem(11, new ItemCreator(Material.DIAMOND).setName("&bVote").setLores(new String [] {
			"",
			"&eVote each day for cool rewards",
			"",
			"&7Left click - &aView voting link",
			"&7Right click - &aView voting rewards",
			""
		}).getItemStack());
		inventory.setItem(13, new ItemCreator(Material.DIAMOND).setName("&bVoting Streaks").setLores(new String [] {
			"",
			"&eVoting each day will create a streak",
			"&eStreaks multiply your rewards",
			"",
			"&7Click - &aView streak information",
			""
		}).getItemStack());
		inventory.setItem(15, new ItemCreator(Material.DIAMOND).setName("&bVoting Milestone Rewards").setLores(new String [] {
			"",
			"&eReaching vote streak milestones",
			"&ewill give you exclusive perks!",
			"",
			"&716 Streak: &aAccess to beta testing servers",
			"&721 Streak: &aExclusive Rainbow Sheep hub pet",
			"&726 Streak: &aExclusive Guardian hub pet",
			"",
		}).getItemStack());
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals(name)) {
			int slot = event.getSlot();
			if(slot == 11) {
				if(event.getClickType() == ClickType.LEFT) {
					player.closeInventory();
					MessageHandler.sendMessage(player, "&cLink coming soon");
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				} else if(event.getClickType() == ClickType.RIGHT) {
					Inventory inventory = Bukkit.createInventory(player, 9 * 6, rewardsName);
					// Crate keys
					inventory.setItem(10, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bVoting Crate Key").setLores(new String [] {
						"",
						"&e+1 &aKey to the Voting crate",
						""
					}).getItemStack()));
					inventory.setItem(19, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bPVP Battles Crate Key").setLores(new String [] {
						"",
						"&e+1 &aKey to the PVP Battles crate",
						"&7(Capture the Flag & Domination)",
						""
					}).getItemStack()));
					inventory.setItem(28, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bSky Wars Crate Key").setLores(new String [] {
						"",
						"&e+1 &aKey to the Sky Wars crate",
						""
					}).getItemStack()));
					inventory.setItem(37, Glow.addGlow(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&bHardcore Elimination Crate Key").setLores(new String [] {
						"",
						"&e+1 &aKey to the Hardcore Elimination crate",
						""
					}).getItemStack()));
					
					// Coins
					inventory.setItem(16, new ItemCreator(Material.GOLD_INGOT).setName("&bPVP Battles Coins").setLores(new String [] {
						"",
						"&e+25 &aPVP Battles Coins",
						"&7(Capture the Flag & Domination)",
						"",
						""
					}).getItemStack());
					inventory.setItem(25, new ItemCreator(Material.GOLD_INGOT).setName("&bSky Wars Coins").setLores(new String [] {
						"",
						"&e+25 &aSky Wars Coins",
						""
					}).getItemStack());
					inventory.setItem(34, new ItemCreator(Material.GOLD_INGOT).setName("&bHardcore Elimination Coins").setLores(new String [] {
						"",
						"&e+25 &aHardcore Elimination Coins",
						""
					}).getItemStack());
					
					// Other
					inventory.setItem(12, new ItemCreator(Material.GOLDEN_APPLE).setName("&bHardcore Elimination Rescatter").setLores(new String [] {
						"",
						"&e+2 &aHardcore Elimination Rescatter",
						""
					}).getItemStack());
					inventory.setItem(14, new ItemCreator(Material.SKULL_ITEM).setName("&bPVP Battles Fast Respawn").setLores(new String [] {
						"",
						"&e+30 &aPVP Battles Fast Respawn",
						"&7(Capture the Flag & Domination)",
						""
					}).getItemStack());
					inventory.setItem(30, new ItemCreator(Material.EXP_BOTTLE).setName("&bNetwork Experience").setLores(new String [] {
						"",
						"&e+250 &aNetwork Experience",
						""
					}).getItemStack());
					inventory.setItem(32, new ItemCreator(Material.LEATHER_BOOTS).setName("&bHub Parkour Checkpoints").setLores(new String [] {
						"",
						"&e+20 &aHub Parkour Checkpoints",
						""
					}).getItemStack());
					inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
					player.openInventory(inventory);
				}
			} else if(slot == 13) {
				final Inventory inventory = Bukkit.createInventory(player, 9 * 6, streakName);
				player.openInventory(inventory);
				final UUID uuid = player.getUniqueId();
				final String name = player.getName();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						int streak = DB.PLAYERS_LIFETIME_VOTES.getInt("uuid", uuid.toString(), "streak");
						inventory.setItem(10, new ItemCreator(Material.NAME_TAG).setName("&bx1 Multiplier").setLores(new String [] {
							"",
							"&eVoting 1 - 5 days in a row",
							"",
							"&7All voting perk quantities",
							"&7are multiplied by 1",
							""
						}).setGlow(streak >= 1 && streak <= 5).getItemStack());
						inventory.setItem(12, new ItemCreator(Material.NAME_TAG).setName("&bx2 Multiplier").setAmount(2).setLores(new String [] {
							"",
							"&eVoting 6 - 10 days in a row",
							"",
							"&7All voting perk quantities",
							"&7are multiplied by 2",
							""
						}).setGlow(streak >= 6 && streak <= 10).getItemStack());
						inventory.setItem(14, new ItemCreator(Material.NAME_TAG).setName("&bx3 Multiplier").setAmount(3).setLores(new String [] {
							"",
							"&eVoting 11 - 15 days in a row",
							"",
							"&7All voting perk quantities",
							"&7are multiplied by 3",
							""
						}).setGlow(streak >= 11 && streak <= 15).getItemStack());
						inventory.setItem(16, new ItemCreator(Material.NAME_TAG).setName("&bx4 Multiplier").setAmount(4).setLores(new String [] {
							"",
							"&eVoting 16 - 20 days in a row",
							"",
							"&7All voting perk quantities",
							"&7are multiplied by 4",
							""
						}).setGlow(streak >= 16 && streak <= 20).getItemStack());
						inventory.setItem(29, new ItemCreator(Material.NAME_TAG).setName("&bx5 Multiplier").setAmount(5).setLores(new String [] {
							"",
							"&eVoting 21 - 25 days in a row",
							"",
							"&7All voting perk quantities",
							"&7are multiplied by 5",
							""
						}).setGlow(streak >= 21 && streak <= 25).getItemStack());
						inventory.setItem(31, new ItemCreator(Material.NAME_TAG).setName("&bx6 Multiplier").setAmount(6).setLores(new String [] {
							"",
							"&eVoting 26+ days in a row",
							"",
							"&7All voting perk quantities",
							"&7are multiplied by 6",
							""
						}).setGlow(streak >= 26).getItemStack());
						inventory.setItem(33, new ItemCreator(ItemUtil.getSkull(name)).setName("&bCurrent Streak: &a" + streak).getItemStack());
						inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
					}
				});
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals(rewardsName)) {
			open(player);
		} else if(event.getTitle().equals(streakName)) {
			open(player);
		}
	}
}
