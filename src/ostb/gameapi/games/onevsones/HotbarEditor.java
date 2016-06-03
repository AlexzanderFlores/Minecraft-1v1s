package ostb.gameapi.games.onevsones;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.games.onevsones.kits.OneVsOneKit;
import ostb.gameapi.games.onevsones.kits.OneVsOneKit.ArmorSlot;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class HotbarEditor implements Listener {
    private static String name = null;
    //private static List<String> delayed = null;
    private Map<String, OneVsOneKit> kits = null;

    public HotbarEditor() {
        name = "Hotbar Editor";
        //delayed = new ArrayList<String>();
        kits = new HashMap<String, OneVsOneKit>();
        new CommandBase("done", true) {
            @Override
            public boolean execute(CommandSender sender, String[] arguments) {
                Player player = (Player) sender;
                if(kits.containsKey(player.getName())) {
                    final String name = player.getName();
                    new AsyncDelayedTask(new Runnable() {
                        @Override
                        public void run() {
                            Player player = ProPlugin.getPlayer(name);
                            if(player != null) {
                                String kit = kits.get(name).getName().replace(" ", "_");
                                String path = OSTB.getInstance().getDataFolder().getPath() + "/hot_bars/" + name + "/" + kit + ".yml";
                                File file = new File(path);
                                if(file.exists()) {
                                    MessageHandler.sendMessage(player, "&cDeleting your old hot bar set up");
                                    file.delete();
                                }
                                ConfigurationUtil config = new ConfigurationUtil(path);
                                for(ArmorSlot slot : ArmorSlot.values()) {
                                    ItemStack item = player.getInventory().getItem(slot.getSlot());
                                    if(item != null) {
                                        config.getConfig().set(slot.getSlot() + "", getItemName(item));
                                    }
                                }
                                for(int a = 0; a < player.getInventory().getSize(); ++a) {
                                    ItemStack item = player.getInventory().getContents()[a];
                                    if(item != null) {
                                        config.getConfig().set(a + "", getItemName(item));
                                    }
                                }
                                config.save();
                                LobbyHandler.spawn(player);
                                MessageHandler.sendMessage(player, "Saving your hot bar set up for kit \"&e" + kits.get(player.getName()).getName() + "&a\"");
                            }
                            kits.remove(name);
                        }
                    });
                } else {
                    MessageHandler.sendMessage(player, "&cYou are not editing a hot bar set up");
                }
                return true;
            }
        };
        EventUtil.register(this);
    }

    public static void open(Player player) {
        /*if(!delayed.contains(player.getName())) {
            final String name = player.getName();
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * 3);
			if(DB.PLAYERS_VERSUS_HOT_BAR_PASSES.getInt("uuid", player.getUniqueId().toString(), "passes") > 0) {
				player.openInventory(LobbyHandler.getKitSelectorInventory(player, HotbarEditor.name, false));
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have any hot bar editing passes. Get &e3 &cwith &b/vote");
			}
		}*/
        if(Ranks.PREMIUM.hasRank(player)) {
            player.openInventory(LobbyHandler.getKitSelectorInventory(player, HotbarEditor.name, false));
        } else {
            MessageHandler.sendMessage(player, Ranks.PREMIUM.getNoPermission());
        }
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
        if(event.getTitle().equals(name)) {
            Player player = event.getPlayer();
            if(QueueHandler.isInQueue(player) || QueueHandler.isWaitingForMap(player)) {
                QueueHandler.remove(player);
                MessageHandler.sendMessage(player, "&cYou have been removed from the queue");
            }
            PrivateBattleHandler.removeAllInvitesFromPlayer(player);
            OneVsOneKit kit = OneVsOneKit.getKit(event.getItem());
            if(kit != null) {
                kit.give(player, false);
                kits.put(player.getName(), kit);
            }
            player.closeInventory();
            event.setCancelled(true);
            MessageHandler.sendLine(player);
            MessageHandler.sendMessage(player, "&a&lYou can now edit your inventory to how you would like it");
            MessageHandler.sendMessage(player, "&a&lWhen you are done run &f&l/done");
            MessageHandler.sendLine(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        kits.remove(event.getPlayer().getName());
    }
}
