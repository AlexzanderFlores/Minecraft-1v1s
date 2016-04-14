package ostb.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.ProPlugin;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.MiniGame;
import ostb.gameapi.SpawnPointHandler;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.games.skywars.cages.Cage;
import ostb.gameapi.games.skywars.cages.SmallCage;
import ostb.gameapi.kit.KitBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public class Events implements Listener {
	private static List<Block> oppenedChests = null;
	private List<ItemStack> possibleItems = null;
	private List<Material> giveOnce = null;
	private Map<String, Integer> chestsOpened = null;
	private Map<String, List<Material>> alreadyGotten = null;
	private enum Rarity {COMMON, UNCOMMON, RARE}
	
	public Events() {
		oppenedChests = new ArrayList<Block>();
		possibleItems = new ArrayList<ItemStack>();
		giveOnce = new ArrayList<Material>();
		chestsOpened = new HashMap<String, Integer>();
		alreadyGotten = new HashMap<String, List<Material>>();
		
		giveOnce.add(Material.COOKED_BEEF);
		giveOnce.add(Material.GRILLED_PORK);
		giveOnce.add(Material.STONE_SWORD);
		giveOnce.add(Material.IRON_AXE);
		giveOnce.add(Material.STONE_PICKAXE);
		giveOnce.add(Material.FISHING_ROD);
		giveOnce.add(Material.LAVA_BUCKET);
		giveOnce.add(Material.IRON_PICKAXE);
		giveOnce.add(Material.GOLD_BOOTS);
		giveOnce.add(Material.GOLD_LEGGINGS);
		giveOnce.add(Material.GOLD_CHESTPLATE);
		giveOnce.add(Material.GOLD_HELMET);
		giveOnce.add(Material.CHAINMAIL_BOOTS);
		giveOnce.add(Material.CHAINMAIL_LEGGINGS);
		giveOnce.add(Material.CHAINMAIL_CHESTPLATE);
		giveOnce.add(Material.CHAINMAIL_HELMET);
		giveOnce.add(Material.IRON_SWORD);
		giveOnce.add(Material.DIAMOND_PICKAXE);
		giveOnce.add(Material.DIAMOND_AXE);
		giveOnce.add(Material.IRON_BOOTS);
		giveOnce.add(Material.IRON_LEGGINGS);
		giveOnce.add(Material.IRON_CHESTPLATE);
		giveOnce.add(Material.IRON_HELMET);
		giveOnce.add(Material.DIAMOND_SWORD);
		giveOnce.add(Material.DIAMOND_BOOTS);
		giveOnce.add(Material.DIAMOND_LEGGINGS);
		giveOnce.add(Material.DIAMOND_CHESTPLATE);
		giveOnce.add(Material.DIAMOND_HELMET);
		
		addItem(Material.COOKED_BEEF, Rarity.COMMON);
		addItem(Material.GRILLED_PORK, Rarity.COMMON);
		addItem(Material.STONE_SWORD, Rarity.COMMON);
		addItem(Material.IRON_SWORD, Rarity.COMMON);
		addItem(Material.DIAMOND_AXE, Rarity.COMMON);
		addItem(Material.STONE_PICKAXE, Rarity.COMMON);
		addItem(Material.WOOD, 32, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, 5, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, 10, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 32, Rarity.COMMON);
		
		addItem(Material.GOLD_INGOT, 4, Rarity.UNCOMMON);
		addItem(Material.FISHING_ROD, Rarity.UNCOMMON);
		addItem(Material.WATER_BUCKET, Rarity.UNCOMMON);
		addItem(Material.LAVA_BUCKET, Rarity.UNCOMMON);
		addItem(Material.EGG, 16, Rarity.UNCOMMON);
		addItem(Material.SNOW_BALL, 16, Rarity.UNCOMMON);
		addItem(Material.GOLD_BOOTS, Rarity.UNCOMMON);
		addItem(Material.GOLD_LEGGINGS, Rarity.UNCOMMON);
		addItem(Material.GOLD_CHESTPLATE, Rarity.UNCOMMON);
		addItem(Material.GOLD_HELMET, Rarity.UNCOMMON);
		addItem(Material.ARROW, Rarity.UNCOMMON);
		addItem(Material.BOW, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_PICKAXE, Rarity.UNCOMMON);
		addItem(Material.IRON_BOOTS, Rarity.UNCOMMON);
		addItem(Material.IRON_LEGGINGS, Rarity.UNCOMMON);
		addItem(Material.IRON_HELMET, Rarity.UNCOMMON);

		addItem(Material.IRON_PICKAXE, Rarity.RARE);
		addItem(Material.CHAINMAIL_BOOTS, Rarity.RARE);
		addItem(Material.CHAINMAIL_LEGGINGS, Rarity.RARE);
		addItem(Material.CHAINMAIL_CHESTPLATE, Rarity.RARE);
		addItem(Material.CHAINMAIL_HELMET, Rarity.RARE);
		addItem(Material.FLINT_AND_STEEL, Rarity.RARE);
		addItem(Material.IRON_CHESTPLATE, Rarity.RARE);
		addItem(Material.GOLDEN_APPLE, Rarity.RARE);
		addItem(Material.DIAMOND_SWORD,Rarity.RARE);
		addItem(Material.ENDER_PEARL,Rarity.RARE);
		addItem(Material.DIAMOND_BOOTS,Rarity.RARE);
		addItem(Material.DIAMOND_LEGGINGS,Rarity.RARE);
		addItem(Material.DIAMOND_CHESTPLATE,Rarity.RARE);
		addItem(Material.DIAMOND_HELMET,Rarity.RARE);
		EventUtil.register(this);
	}
	
	public static void restock(Block block) {
		oppenedChests.remove(block);
	}
	
	private void addItem(Material material, Rarity rarity) {
		addItem(material, 1, rarity);
	}
	
	private void addItem(Material material, int amount, Rarity rarity) {
		addItem(material, amount, 0, rarity);
	}
	
	private void addItem(Material material, int amount, int data, Rarity rarity) {
		addItem(new ItemStack(material, amount, (byte) data), rarity);
	}
	
	private void addItem(ItemStack itemStack, Rarity rarity) {
		for(int a = 0; a < (rarity == Rarity.COMMON ? 4 : rarity == Rarity.UNCOMMON ? 3 : rarity == Rarity.RARE ? 2 : 1); ++a) {
			possibleItems.add(itemStack);
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		World world = OSTB.getMiniGame().getMap();
		SpawnPointHandler spawnPointHandler = new SpawnPointHandler(world);
		List<Player> players = ProPlugin.getPlayers();
		spawnPointHandler.teleport(players);
		for(Player player : players) {
			boolean usedCage = false;
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPlugin() == Plugins.SKY_WARS_SOLO && kit.has(player) && kit.getKitType().equals("cage")) {
					kit.execute(player);
					usedCage = true;
					break;
				}
			}
			if(!usedCage) {
				new SmallCage(new ItemCreator(Material.GLASS).setName("Default Cage").setLores(new String [] {}).getItemStack(), 0).execute(player);
			}
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(Cage.getCages() != null) {
			for(Cage cage : Cage.getCages()) {
				cage.remove();
			}
			Cage.getCages().clear();
		}
		MiniGame miniGame = OSTB.getMiniGame();
		miniGame.setAllowFoodLevelChange(true);
		miniGame.setAllowDroppingItems(true);
		miniGame.setAllowPickingUpItems(true);
		miniGame.setDropItemsOnLeave(true);
		miniGame.setAllowBuilding(true);
		miniGame.setAllowEntityCombusting(true);
		miniGame.setAllowPlayerInteraction(true);
		miniGame.setAllowBowShooting(true);
		miniGame.setAllowInventoryClicking(true);
		miniGame.setAllowItemSpawning(true);
		miniGame.setFlintAndSteelUses(4);
		miniGame.setCounter(60 * 8);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MiniGame miniGame = OSTB.getMiniGame();
				miniGame.setAllowEntityDamageByEntities(true);
				miniGame.setAllowEntityDamage(true);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		String type = event.getBlock().getType().toString();
		if(type.contains("LAVA") || type.contains("WATER")) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(oppenedChests != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !SpectatorHandler.contains(event.getPlayer())) {
			Block block = event.getClickedBlock();
			if((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) && !oppenedChests.contains(block)) {
				oppenedChests.add(block);
				Player player = event.getPlayer();
				Chest chest = (Chest) block.getState();
				chest.getInventory().clear();
				Random random = new Random();
				int numberOfTimes = random.nextInt(5) + 2;
				for(int a = 0; a < numberOfTimes; ++a) {
					ItemStack itemStack = null;
					Material type = null;
					do {
						itemStack = possibleItems.get(random.nextInt(possibleItems.size()));
						type = itemStack.getType();
						if((itemStack.getEnchantments() == null || itemStack.getEnchantments().isEmpty()) && giveOnce.contains(type)) {
							List<Material> got = alreadyGotten.get(player.getName());
							if(got == null) {
								got = new ArrayList<Material>();
								alreadyGotten.put(player.getName(), got);
							}
							if(!got.contains(type)) {
								got.add(type);
								alreadyGotten.put(player.getName(), got);
								break;
							}
						} else {
							break;
						}
					} while(true);
					if(type == Material.ARROW || type.isEdible()) {
						if(type == Material.ARROW) {
							itemStack = new ItemStack(type, random.nextInt(10) + 10);
						} else if(type != Material.GOLDEN_APPLE) {
							itemStack = new ItemStack(type, random.nextInt(5) + 5);
						}
					}
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), itemStack);
				}
				int counter = 0;
				if(chestsOpened.containsKey(player.getName())) {
					counter = chestsOpened.get(player.getName());
				}
				chestsOpened.put(player.getName(), ++counter);
				if(counter == 1) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.STONE_PICKAXE));
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.STONE_SWORD));
				} else if(counter == 2) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.WOOD, 32));
				} else if(counter == 3) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.SNOW_BALL, 16));
				}
			}
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material type = event.getEntity().getItemStack().getType();
		if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Material type = event.getBlock().getType();
		if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
			Player player = event.getPlayer();
			player.setItemInHand(new ItemStack(Material.AIR));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	private void remove(Player player) {
		chestsOpened.remove(player.getName());
		if(alreadyGotten.containsKey(player.getName())) {
			alreadyGotten.get(player.getName()).clear();
			alreadyGotten.remove(player.getName());
		}
	}
}
