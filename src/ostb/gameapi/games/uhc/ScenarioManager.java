package ostb.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.gameapi.scenarios.Scenario;
import ostb.gameapi.scenarios.ScenarioStateChangeEvent;
import ostb.gameapi.scenarios.scenarios.Barebones;
import ostb.gameapi.scenarios.scenarios.BestPVE;
import ostb.gameapi.scenarios.scenarios.CutClean;
import ostb.gameapi.scenarios.scenarios.OrePower;
import ostb.gameapi.scenarios.scenarios.TimeBomb;
import ostb.gameapi.scenarios.scenarios.TripleOres;
import ostb.gameapi.scenarios.scenarios.TrueLove;
import ostb.gameapi.scenarios.scenarios.Vanilla;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class ScenarioManager implements Listener {
    private static Map<Integer, Scenario> scenarios = null;
    private static String name = null;

    public ScenarioManager() {
        scenarios = new HashMap<Integer, Scenario>();
        scenarios.put(10, Vanilla.getInstance());
        scenarios.put(11, CutClean.getInstance());
        scenarios.put(12, TripleOres.getInstance());
        scenarios.put(13, Barebones.getInstance());
        scenarios.put(14, TimeBomb.getInstance());
        scenarios.put(15, BestPVE.getInstance());
        scenarios.put(16, TrueLove.getInstance());
        resetScenarios();
        name = "Scenario Manager";
        new CommandBase("scenarios", true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if(!open(player)) {
                    MessageHandler.sendUnknownCommand(sender);
                }
                return true;
            }
        };
        EventUtil.register(this);
    }
    
    public static List<Scenario> getAllScenarios() {
    	return new ArrayList<Scenario>(scenarios.values());
    }

    public static List<Scenario> getActiveScenarios() {
        List<Scenario> scenarios2 = new ArrayList<Scenario>();
        for(Scenario scenario : scenarios.values()) {
            if(scenario.isEnabled()) {
                scenarios2.add(scenario);
            }
        }
        return scenarios2;
    }
    
    public static Scenario getScenario(String text) {
    	for(Scenario scenario : scenarios.values()) {
    		if(scenario.getName().equals(text) || scenario.getShortName().equals(text)) {
    			return scenario;
    		}
    	}
    	return null;
    }
    
    public static String getText() {
    	String text = "";
    	for(Scenario scenario : getActiveScenarios()) {
    		text += scenario.getShortName() + " ";
    	}
    	return text.substring(0, text.length() - 1);
    }
    
    public static boolean isScenario(String shortName) {
    	for(Scenario scenario : scenarios.values()) {
    		if(scenario.getShortName().equals(shortName)) {
    			return true;
    		}
    	}
    	return false;
    }

    public static boolean open(Player player) {
    	if(Ranks.OWNER.hasRank(player)) {
    		if(WorldHandler.isPreGenerated()) {
    			ItemStack reset = new ItemCreator(Material.EYE_OF_ENDER).setName("&aReset Scenarios").getItemStack();
                ItemStack enabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.LIME.getData()).setName("&aENABLED").addLore("&fClick the icon above to toggle").getItemStack();
                ItemStack disabled = new ItemCreator(Material.STAINED_GLASS_PANE, DyeColor.RED.getData()).setName("&cDISABLED").addLore("&fClick the icon above to toggle").getItemStack();
                Inventory inventory = Bukkit.createInventory(player, 9 * 6, name);
                for(int a : scenarios.keySet()) {
                    Scenario scenario = scenarios.get(a);
                    inventory.setItem(a, scenario.getItem());
                    if(scenario.isEnabled()) {
                        inventory.setItem(a + 9, enabled);
                    } else {
                        inventory.setItem(a + 9, disabled);
                    }
                }
                for(int a : new int[]{0, 8, 45}) {
                    inventory.setItem(a, reset);
                }
                inventory.setItem(inventory.getSize() - 1, new ItemCreator(Material.ARROW).setName("&eMove to Team Selection").getItemStack());
                player.openInventory(inventory);
    		} else {
    			MessageHandler.sendMessage(player, "&cCannot open the Scenario Manager: &eWorld is not pregenerated!");
    		}
    		return true;
        } else {
            return false;
        }
    }

    private List<Scenario> getScenarios() {
        List<Scenario> scenarios2 = new ArrayList<Scenario>();
        for(Scenario scenario : scenarios.values()) {
            scenarios2.add(scenario);
        }
        return scenarios2;
    }

    private void resetScenarios() {
        for(Scenario scenario : getScenarios()) {
            scenario.disable(false);
        }
        Vanilla.getInstance().enable(false);
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getTitle().equals(name)) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            Material type = item.getType();
            if(type == Material.EYE_OF_ENDER) {
                resetScenarios();
                open(player);
            } else if(type == Material.STAINED_GLASS_PANE) {
                open(player);
            } else if(type == Material.ARROW) {
                TeamHandler.open(player);
            } else {
                Scenario scenario = null;
                for(Scenario scenario2 : getScenarios()) {
                    if(scenario2.getItem().equals(item)) {
                        scenario = scenario2;
                        break;
                    }
                }
                if(scenario != null) {
                    if(scenario.isEnabled()) {
                        scenario.disable(false);
                    } else {
                        scenario.enable(false);
                    }
                }
                open(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onScenarioStateChange(ScenarioStateChangeEvent event) {
        Scenario scenario = event.getScenario();
        List<Scenario> scenarios = getScenarios();
        // Disable all other scenarios if Vanilla is being enabled
        if(scenario instanceof Vanilla) {
            if(event.isEnabling()) {
                for(Scenario scenario2 : scenarios) {
                    if(!(scenario2 instanceof Vanilla)) {
                        scenario2.disable(true);
                    }
                }
            }
        } else {
            // If any other scenarios are being enabled we want to disable vanilla
            Vanilla.getInstance().disable(true);
            // Be sure that Cut Clean is always enabled with Triple Ores
            if(scenario instanceof TripleOres) {
                CutClean.getInstance().enable(true);
            } else if(scenario instanceof CutClean && !event.isEnabling()) {
                TripleOres.getInstance().disable(true);
                Barebones.getInstance().disable(true);
            }
            // Be sure that Ore Power and Bare Bones are never enabled at the same time
            if(scenario instanceof Barebones && event.isEnabling()) {
                CutClean.getInstance().enable(true);
                OrePower.getInstance().disable(true);
            }
            if(scenario instanceof OrePower && event.isEnabling()) {
                Barebones.getInstance().disable(true);
            }
        }
        // Make sure at least vanilla is enabled
        boolean anyEnabled = false;
        for(Scenario scenario2 : scenarios) {
            if(scenario2.isEnabled()) {
                anyEnabled = true;
                break;
            }
        }
        if(!anyEnabled) {
            Vanilla.getInstance().enable(true);
        }
    }
}
