package ostb.gameapi.games.uhc;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import ostb.ProPlugin;
import ostb.customevents.game.GameStartEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.uhc.scenarios.scenarios.CutClean;
import ostb.player.MessageHandler;
import ostb.server.CommandBase;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class OptionsHandler implements Listener {
    private static boolean rush = false;
    private static boolean nether = false;
    private static int appleRates = 1;
    private static boolean horses = true;
    private static boolean horseHealing = true;
    private static boolean pearlDamage = true;
    private static boolean absorption = true;
    private static boolean crossTeaming = false;
    private static boolean end = false;
    private static String name = null;

    public OptionsHandler() {
        name = "Other Options";
        new CommandBase("isNetherOn") {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                MessageHandler.sendMessage(sender, "Nether: " + (isNetherEnabled() ? "&eYes" : "&cNo"));
                return true;
            }
        };
        EventUtil.register(this);
    }

    public static boolean isRush() {
        return rush;
    }
    
    public static void setRush(boolean rush) {
    	OptionsHandler.rush = rush;
    }

    public static boolean isNetherEnabled() {
        return nether;
    }
    
    public static void setAllowNether(boolean nether) {
    	OptionsHandler.nether = nether;
    	if(nether) {
    		WorldHandler.generateNether();
    	}
    }

    public static int getAppleRates() {
        return appleRates;
    }
    
    public static void setAppleRates(int appleRates) {
    	OptionsHandler.appleRates = appleRates;
    }

    public static boolean getAllowHorses() {
        return horses;
    }
    
    public static void setAllowHorses(boolean horses) {
    	OptionsHandler.horses = horses;
    	if(!horses) {
    		for(World world : Bukkit.getWorlds()) {
    			for(Entity entity : world.getEntities()) {
    				if(entity instanceof Horse) {
    					entity.remove();
    				}
    			}
    		}
    	}
    }

    public static boolean getAllowHorseHealing() {
        return horseHealing;
    }
    
    public static void setAllowHorseHealing(boolean horseHealing) {
    	OptionsHandler.horseHealing = horseHealing;
    }

    public static boolean getAllowPearlDamage() {
        return pearlDamage;
    }
    
    public static void setAllowPearlDamage(boolean pearlDamage) {
    	OptionsHandler.pearlDamage = pearlDamage;
    }

    public static boolean getAllowAbsorption() {
        return absorption;
    }
    
    public static void setAllowAbsorption(boolean absorption) {
    	OptionsHandler.absorption = absorption;
    }

    public static boolean getCrossTeaming() {
        return crossTeaming;
    }

    public static void setCrossTeaming(boolean crossTeaming) {
        OptionsHandler.crossTeaming = crossTeaming;
    }

    public static boolean isEndEnabled() {
        return end;
    }
    
    public static void setAllowEnd(boolean end) {
    	OptionsHandler.end = end;
    	if(end) {
    		WorldHandler.generateEnd();
    	}
    }

    public static boolean applyOptions(String options) {
    	//1234456789
		//1 = team size
		//2 = bool for rush
		//3 = bool for nether
		//44 = apple rates, 00 = 100%
		//5 = bool for horses
		//6 = bool for horse healing
		//7 = bool for pearl damage
		//8 = bool for absorption
		//9 = bool for end
    	if(options.length() == 10) {
    		try {
    			Integer.valueOf(options);
    		} catch(NumberFormatException e) {
    			return false;
    		}
    		TeamHandler.setMaxTeamSize(Integer.valueOf(options.charAt(0)) - 48);
    		setRush(options.charAt(1) == '1');
    		setAllowNether(options.charAt(2) == '1');
    		int rates = Integer.valueOf(options.substring(3, 5));
    		setAppleRates(rates == 00 ? 100 : rates);
    		setAllowHorses(options.charAt(5) == '1');
    		setAllowHorseHealing(options.charAt(6) == '1');
    		setAllowPearlDamage(options.charAt(7) == '1');
    		setAllowAbsorption(options.charAt(8) == '1');
    		setAllowEnd(options.charAt(9) == '1');
    		return true;
    	}
    	return false;
    }
    
    public static void open(Player player) {
        ItemStack enabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData()).setName("&aENABLED").addLore("&fClick the icon above to toggle").getItemStack();
        ItemStack disabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setName("&cDISABLED").addLore("&fClick the icon above to toggle").getItemStack();
        ItemStack back = new ItemCreator(Material.ARROW).setName("&eBack").getItemStack();
        ItemStack finish = new ItemCreator(Material.ARROW).setName("&eFinish Set Up").getItemStack();
        Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
        ItemStack [] items = new ItemStack[]{
                new ItemCreator(Material.DIAMOND_BOOTS).setName("&bRush").getItemStack(),
                new ItemCreator(Material.NETHERRACK).setName("&bNether Enabled").getItemStack(),
                new ItemCreator(Material.APPLE).setName("&bApple Rates").addLore("&4NOTE: &fEdit this with:")
                        .addLore("&f/appleRates [percentage]").addLore("").addLore("&aCurrent Rates: &c" + getAppleRates() + "&a%").getItemStack(),
                new ItemCreator(Material.SADDLE).setName("&bHorses").getItemStack(),
                new ItemCreator(Material.BREAD).setName("&bHorse Healing").getItemStack(),
                new ItemCreator(Material.ENDER_PEARL).setName("&bEnder Pearl Damage").getItemStack(),
                new ItemCreator(Material.GOLDEN_APPLE).setName("&bAbsorption").getItemStack(),
                new ItemCreator(Material.ENDER_STONE).setName("&bEnd Enabled").getItemStack()
        };
        boolean [] states = new boolean [] {isRush(), isNetherEnabled(), false, getAllowHorses(), getAllowHorseHealing(), getAllowPearlDamage(), getAllowAbsorption(), isEndEnabled()};
        int [] slots = new int [] {10, 11, 12, 13, 14, 15, 16, 28};
        for(int a = 0; a < items.length; ++a) {
            inventory.setItem(slots[a], items[a]);
            if(states[a]) {
                inventory.setItem(slots[a] + 9, enabled);
            } else {
                inventory.setItem(slots[a] + 9, disabled);
            }
        }
        inventory.setItem(inventory.getSize() - 9, back);
        inventory.setItem(inventory.getSize() - 1, finish);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getTitle().equals(name)) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            Material type = item.getType();
            if(type == Material.ARROW) {
                String name = event.getItemTitle();
                if(name.contains("Back")) {
                    TeamHandler.open(player);
                } else {
                    player.closeInventory();
                }
            } else if(type == Material.DIAMOND_BOOTS) {
                setRush(!rush);
                open(player);
            } else if(type == Material.NETHERRACK) {
                setAllowNether(!nether);
                open(player);
            } else if(type == Material.APPLE) {
                open(player);
            } else if(type == Material.SADDLE) {
            	setAllowHorses(!getAllowHorses());
                if(!getAllowHorses()) {
                    setAllowHorseHealing(false);
                }
                open(player);
            } else if(type == Material.BREAD) {
                if(getAllowHorses()) {
                    setAllowHorseHealing(!getAllowHorseHealing());
                } else {
                    setAllowHorseHealing(false);
                }
                open(player);
            } else if(type == Material.ENDER_PEARL) {
                setAllowPearlDamage(!pearlDamage);
                open(player);
            } else if(type == Material.GOLDEN_APPLE) {
            	setAllowAbsorption(!getAllowAbsorption());
            	open(player);
            } else if(type == Material.ENDER_STONE) {
                setAllowEnd(!end);
                open(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.getEntityType() == EntityType.HORSE && !getAllowHorses()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if(event.getEntity() instanceof Horse && !getAllowHorseHealing()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(!getAllowPearlDamage() && event.getCause() == TeleportCause.ENDER_PEARL) {
            event.getPlayer().teleport(event.getTo());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if(!getAllowAbsorption() && event.getItem().getType() == Material.GOLDEN_APPLE) {
            final String name = event.getPlayer().getName();
            new DelayedTask(new Runnable() {
                @Override
                public void run() {
                    Player player = ProPlugin.getPlayer(name);
                    if(player != null) {
                        player.removePotionEffect(PotionEffectType.ABSORPTION);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        if(ScenarioManager.getActiveScenarios().contains(CutClean.getInstance())) {
            appleRates = 4;
        }
        if(!getAllowHorses()) {
            for(Entity entity : Bukkit.getWorlds().get(1).getEntities()) {
                if(entity instanceof Horse) {
                    entity.remove();
                }
            }
        }
    }
}
