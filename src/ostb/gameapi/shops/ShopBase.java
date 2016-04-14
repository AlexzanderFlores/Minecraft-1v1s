package ostb.gameapi.shops;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.customevents.player.CoinUpdateEvent;
import ostb.customevents.player.InventoryItemClickEvent;
import ostb.customevents.player.MouseClickEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.customevents.player.PlayerPostKitPurchaseEvent;
import ostb.gameapi.kit.KitBase;
import ostb.player.CoinsHandler;
import ostb.server.DB;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public abstract class ShopBase implements Listener {
	private String name = null;
	private String permission = null;
	private Plugins plugin = null;
	protected Map<String, Integer> pages = null;
	private int maxPages = 1;
	private ItemStack itemStack = null;
	
	public class KitData {
		private String title = null;
		private int total = 0;
		private int owned = 0;
		private int percentage = 0;
		
		public KitData(Player player, String title, String kitType) {
			this(player, title, kitType, "");
		}
		
		public KitData(Player player, String title, String kitType, String kitSubType) {
			this.title = title;
			total = 0;
			owned = 0;
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getPlugin() == plugin && kit.getKitType().equals(kitType) && kit.getKitSubType().equals(kitSubType)) {
					++total;
					if(kit.owns(player)) {
						++owned;
					}
				}
			}
			percentage = (int) (owned * 100.0 / total + 0.5);
		}
		
		public ItemStack getItem() {
			return new ItemCreator(Material.DIAMOND).setName("&7" + title + ": &e" + owned + "&8/&e" + total + " &7(&e" + percentage + "%&7)").getItemStack();
		}
	}
	
	public ShopBase(String name, String permission, DB table, Plugins plugin, int maxPages) {
		this.name = name;
		this.permission = permission;
		new CoinsHandler(table, plugin);
		this.plugin = plugin;
		this.maxPages = maxPages;
		pages = new HashMap<String, Integer>();
		itemStack = new ItemCreator(Material.CHEST).setName("&b" + plugin.getDisplay() + " Shop").getItemStack();
		EventUtil.register(this);
	}
	
	public String getName() {
		return name;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public int getPage(Player player) {
		if(!pages.containsKey(player.getName())) {
			pages.put(player.getName(), 1);
		}
		return pages.get(player.getName());
	}
	
	public void updateCoinsItem(Player player) {
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			player.getOpenInventory().setItem(player.getOpenInventory().getTopInventory().getSize() - 4, CoinsHandler.getCoinsHandler(plugin).getItemStack(player));
		}
	}
	
	public void updateCoinsItem(Player player, Inventory inventory) {
		inventory.setItem(inventory.getSize() - 4, CoinsHandler.getCoinsHandler(plugin).getItemStack(player));
	}
	
	public void openShop(Player player) {
		openShop(player, getPage(player));
	}
	
	protected boolean hasCrate(Player player, InventoryView view) {
		return view.getTitle().equals(getName()) && view.getItem(4).getType() != Material.AIR;
	}
	
	protected void setBackItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page > 1) {
			inventory.setItem(0, new ItemCreator(Material.ARROW).setName("&bPage #" + (page - 1)).getItemStack());
		}
	}
	
	protected void setNextItem(Player player, Inventory inventory) {
		int page = getPage(player);
		if(page < maxPages) {
			inventory.setItem(8, new ItemCreator(Material.ARROW).setName("&bPage #" + (page + 1)).getItemStack());
		}
	}
	
	protected void updateItems(Player player, Inventory inventory) {
		setBackItem(player, inventory);
		setNextItem(player, inventory);
		updateInfoItem(player, inventory);
		inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.WOOD_DOOR).setName("&bBack").getItemStack());
		updateCoinsItem(player, inventory);
	}
	
	public abstract void openShop(Player player, int page);
	public abstract void updateInfoItem(Player player);
	public abstract void updateInfoItem(Player player, Inventory inventory);
	public abstract void onInventoryItemClick(InventoryItemClickEvent event);
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(OSTB.getMiniGame() != null && OSTB.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			player.getInventory().addItem(itemStack);
		} else {
			PlayerJoinEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(OSTB.getMiniGame() != null && OSTB.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			if(player.getItemInHand().equals(itemStack)) {
				EffectUtil.playSound(player, Sound.CHEST_OPEN);
				openShop(player);
			}
		} else {
			MouseClickEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onPlayerPostKitPurchase(PlayerPostKitPurchaseEvent event) {
		Player player = event.getPlayer();
		String title = player.getOpenInventory().getTitle();
		if(title != null && title.equals(getName())) {
			player.getOpenInventory().setItem(event.getKit().getSlot(), event.getKit().getIcon(player));
		}
	}
	
	@EventHandler
	public void onCoinUpdate(CoinUpdateEvent event) {
		updateCoinsItem(event.getPlayer());
		updateInfoItem(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		pages.remove(event.getPlayer().getName());
	}
}
