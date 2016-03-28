package ostb.player.trophies;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.player.MessageHandler;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;



public abstract class Trophy implements Listener {
	private Plugins plugin = null;
	private int slot = 0;
	
	public Trophy(Plugins plugin, int slot) {
		this.plugin = plugin;
		this.slot = slot;
	}
	
	public Plugins getPlugin() {
		return plugin;
	}
	
	public String getName() {
		return ChatColor.stripColor(getIcon().getItemMeta().getDisplayName());
	}
	
	public int getSlot() {
		return slot;
	}
	
	public boolean canRegister() {
		return getPlugin() == OSTB.getPlugin();
	}
	
	public boolean addToInventory(Player player, Inventory inventory) {
		if(hasAchieved(player)) {
			inventory.setItem(getSlot(), getIcon());
			return true;
		} else {
			ItemStack icon = getIcon();
			icon.setType(Material.BEDROCK);
			icon.setAmount(1);
			inventory.setItem(getSlot(), icon);
			return false;
		}
	}
	
	public void setAchieved(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(!hasAchieved(player)) {
					DB.PLAYERS_ACHIEVEMENTS.insert("'" + player.getUniqueId().toString() + "', '" + getPlugin().toString() + "', '" + getName() + "'");
					MessageHandler.sendLine(player, "&6");
					MessageHandler.sendMessage(player, "&4&k&lab&a Trophy Unlocked: \"&e" + getName() + "&a\" &4&k&lab");
					MessageHandler.sendMessage(player, "&bView your trophies: &6/trophies");
					MessageHandler.sendLine(player, "&6");
				}
			}
		});
	}
	
	public boolean hasAchieved(Player player) {
		String [] keys = new String [] {"uuid", "game_name", "achievement"};
		String [] values = new String [] {player.getUniqueId().toString(), getPlugin().toString(), getName()};
		return DB.PLAYERS_ACHIEVEMENTS.isKeySet(keys, values);
	}
	
	public abstract ItemStack getIcon();
}
