package ostb.gameapi.games.uhc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerSpectatorEvent;
import ostb.customevents.player.PlayerSpectatorEvent.SpectatorState;
import ostb.gameapi.MiniGame.GameStates;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class HostHandler implements Listener {
    private static UUID mainHost = null;
    private static ItemStack center = null;
    private static String name = null;

    public HostHandler() {
        center = new ItemCreator(Material.COMPASS).setName("&aTeleport to &e0, 0").getItemStack();
        name = "World Selection";
        EventUtil.register(this);
    }

    public static Player getMainHost() {
        return Bukkit.getPlayer(mainHost);
    }

    @EventHandler
    public void onPlayerSpectator(PlayerSpectatorEvent event) {
        if(!event.isCancelled() && event.getState() == SpectatorState.ADDED) {
            Player player = event.getPlayer();
            if(Ranks.OWNER.hasRank(player) && OSTB.getMiniGame().getGameState() != GameStates.STARTED) {
                final String name = player.getName();
                new AsyncDelayedTask(new Runnable() {
                    @Override
                    public void run() {
                        Player player = ProPlugin.getPlayer(name);
                        if(player != null) {
                            player.getInventory().addItem(center);
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        Player player = event.getPlayer();
        if(ItemUtil.isItem(player.getItemInHand(), center)) {
            if(OptionsHandler.isNetherEnabled()) {
                Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
                inventory.setItem(11, new ItemCreator(Material.GRASS).setName("&aTeleport to &eWorld").getItemStack());
                inventory.setItem(15, new ItemCreator(Material.NETHERRACK).setName("&aTeleport to &cNether").getItemStack());
                player.openInventory(inventory);
            } else {
                player.teleport(WorldHandler.getWorld().getSpawnLocation());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryItemClickEvent event) {
        if(event.getTitle().equals(name)) {
            Player player = event.getPlayer();
            Material type = event.getItem().getType();
            if(type == Material.GRASS) {
                player.teleport(WorldHandler.getWorld().getSpawnLocation());
            } else if(type == Material.NETHERRACK) {
                player.teleport(WorldHandler.getNether().getSpawnLocation());
            }
            player.closeInventory();
            event.setCancelled(true);
        }
    }
}
