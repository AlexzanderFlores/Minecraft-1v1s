package ostb.gameapi.games.kitpvp.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ostb.customevents.player.InventoryItemClickEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EffectUtil;
import ostb.server.util.ItemCreator;

public class SaveYourItems extends InventoryViewer {
	public SaveYourItems(Player player) {
		super("Save Your Items", player, false);
		Inventory inventory = Bukkit.createInventory(player, 9 * 3, name);
		inventory.setItem(11, new ItemCreator(Material.ENDER_CHEST).setName("&bEnder Chest #1").setLores(new String [] {"", "&7Price: &a50", ""}).getItemStack());
		inventory.setItem(13, new ItemCreator(Material.ENDER_CHEST).setAmount(2).setName("&bEnder Chest #2").setLores(new String [] {"", "&7Price: &a100", "&7Requires " + Ranks.PREMIUM.getPrefix(), ""}).getItemStack());
		inventory.setItem(15, new ItemCreator(Material.ENDER_CHEST).setAmount(3).setName("&bEnder Chest #3").setLores(new String [] {"", "&7Price: &a150", "&7Requires " + Ranks.PREMIUM_PLUS.getPrefix(), ""}).getItemStack());
		player.openInventory(inventory);
	}
	
	private void open(final Player player, final int chest) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID uuid = player.getUniqueId();
				String sUUID = uuid.toString();
				String [] keys = new String [] {"uuid", "id_owned"};
				String [] values = new String [] {sUUID, chest + ""};
				if(!DB.PLAYERS_KITPVP_CHESTS.isKeySet(keys, values)) {
					int price = 50 * chest;
					if(coinsHandler.getCoins(player) >= price) {
						coinsHandler.addCoins(player, price * -1);
						DB.PLAYERS_KITPVP_CHESTS.insert("'" + sUUID + "', '" + chest + "'");
						MessageHandler.sendMessage(player, "Unlocked &eChest #" + chest);
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have enough coins, get more with &a/vote");
						EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
						return;
					}
				}
				DB db = DB.valueOf("KITPVP_CHEST_" + chest);
				Inventory inventory = Bukkit.createInventory(player, 9 * 3, "Chest #" + chest);
				player.openInventory(inventory);
				ResultSet resultSet = null;
				try {
					resultSet = db.getConnection().prepareStatement("SELECT material,data,enchant,durability,slot FROM " + db.getName() + " WHERE uuid = '" + sUUID + "'").executeQuery();
					while(resultSet.next()) {
						Material material = Material.valueOf(resultSet.getString("material"));
						int data = resultSet.getInt("data");
						String enchant = resultSet.getString("enchant");
						int durability = resultSet.getInt("durability");
						int slot = resultSet.getInt("slot");
						ItemStack item = new ItemStack(material, 1, (byte) data);
						item.setDurability((short) durability);
						if(!enchant.equals("none")) {
							String [] split = enchant.split(":");
							Enchantment ench = null;
							for(Enchantment enchantment : Enchantment.values()) {
								if(enchantment.toString().equals(enchant)) {
									ench = enchantment;
									break;
								}
							}
							if(ench != null) {
								item.addUnsafeEnchantment(ench, Integer.valueOf(split[1]));
							}
						}
						inventory.setItem(slot, item);
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(resultSet);
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		final Inventory inventory = event.getInventory();
		if(event.getPlayer() instanceof Player && inventory.getTitle().startsWith("Chest #")) {
			final Player player = (Player) event.getPlayer();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					int chest = Integer.valueOf(inventory.getTitle().split("Chest #")[1]);
					DB db = DB.valueOf("KITPVP_CHEST_" + chest);
					for(ItemStack item : inventory.getContents()) {
						
					}
					MessageHandler.sendMessage(player, "&e" + inventory.getName() + " &xhas been saved");
				}
			});
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			int slot = event.getSlot();
			if(slot == 11) {
				open(player, 1);
			} else if(slot == 12) {
				if(Ranks.PREMIUM.hasRank(player)) {
					open(player, 2);
				} else {
					MessageHandler.sendMessage(player, Ranks.PREMIUM.getNoPermission());
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				}
			} else if(slot == 13) {
				if(Ranks.PREMIUM_PLUS.hasRank(player)) {
					open(player, 3);
				} else {
					MessageHandler.sendMessage(player, Ranks.PREMIUM_PLUS.getNoPermission());
					EffectUtil.playSound(player, Sound.NOTE_BASS_GUITAR, 1000.0f);
				}
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
