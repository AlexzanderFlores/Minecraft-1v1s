package ostb.gameapi.games.onevsones;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.games.onevsones.kits.OneVsOneKit;
import ostb.player.MessageHandler;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class HotbarEditor implements Listener {
    private static Map<String, OneVsOneKit> kits = null;
    private static String path = null;

    public HotbarEditor() {
        kits = new HashMap<String, OneVsOneKit>();
        path = Bukkit.getWorldContainer().getPath() + "/../resources/1v1/hotbars/%/%%.yml";
        EventUtil.register(this);
    }

    public static void open(Player player, OneVsOneKit kit) {
    	if(QueueHandler.isInQueue(player) || QueueHandler.isWaitingForMap(player)) {
            QueueHandler.remove(player);
            MessageHandler.sendMessage(player, "&cYou have been removed from the queue");
        }
    	PrivateBattleHandler.removeAllInvitesFromPlayer(player);
    	kits.put(player.getName(), kit);
    }

    private String getItemName(ItemStack item) {
        if(item != null) {
            int id = item.getTypeId();
            byte data = item.getData().getData();
            int amount = item.getAmount();
            String name = id + ":" + data + ":" + amount;
            if(item.getType() == Material.POTION) {
                Potion potion = Potion.fromItemStack(item);
                name += ":" + potion.getType().toString() + ":" + potion.getLevel() + ":" + (potion.isSplash() ? 1 : 0);
            } else {
                name += ":NULL:0:0";
            }
            Map<Enchantment, Integer> enchants = item.getEnchantments();
            for(Enchantment enchantment : enchants.keySet()) {
                name += ":" + enchantment.getName() + ":" + enchants.get(enchantment);
            }
            return name;
        }
        return "0:0";
    }
    
    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
    	Player player = event.getPlayer();
    	if(kits.containsKey(player.getName())) {
    		if(event.getItem().getType() == Material.BARRIER) {
    			String name = kits.get(player.getName()).getName();
    			//String kit = kits.get(name).getName().replace(" ", "_");
                File file = new File(path.replace("%%", name).replace("%", player.getName()));
                if(file.exists()) {
                    MessageHandler.sendMessage(player, "&cDeleting your old hot bar set up");
                    file.delete();
                }
                Bukkit.getLogger().info("Saving to " + file.getAbsolutePath());
                ConfigurationUtil config = new ConfigurationUtil(file.getName());
                Inventory inventory = event.getInventory();
                for(int a = 0; a < inventory.getSize() - 10; ++a) {
                    ItemStack item = inventory.getContents()[a];
                    if(item != null && item.getType() != Material.AIR) {
                        config.getConfig().set(a + "", getItemName(item));
                    }
                }
                config.save();
                MessageHandler.sendMessage(player, "Saving your hot bar set up for kit \"&e" + kits.get(player.getName()).getName() + "&a\"");
    			event.setCancelled(true);
        		player.closeInventory();
    		}
    	}
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
    	kits.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        kits.remove(event.getPlayer().getName());
    }
}
