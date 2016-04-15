package ostb.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.OSTB.Plugins;
import ostb.customevents.TimeEvent;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.PlayerRankChangeEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB.Databases;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.StringUtil;

public class GeneralEvents implements Listener {
	private List<String> blockedCommands = null;
	private List<String> delayed = null;
	private int delay = 3;
	
	public GeneralEvents() {
		blockedCommands = new ArrayList<String>();
		blockedCommands.add("/me");
		blockedCommands.add("/tell");
		blockedCommands.add("/w");
		blockedCommands.add("/kill");
		blockedCommands.add("/suicide");
		blockedCommands.add("/afk");
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	private void colorPlayerTab(Player player) {
		String name = AccountHandler.getRank(player).getColor() + player.getName();
		if(name.length() > 16) {
			name = name.substring(0, 16);
		}
		try {
			player.setPlayerListName(name);
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setTicksLived(1);
		colorPlayerTab(player);
		if(player.isOp() || player.hasPermission("bukkit.command.op.give") || player.hasPermission("bukkit.command.op.take")) {
			Plugins plugin = OSTB.getPlugin();
			if(plugin != Plugins.BUILDING) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "deop " + player.getName());
			}
		}
		event.setJoinMessage(null);
		/*String top = "&aWelcome to &bplay.ProMcGames.com";
		String bottom = "&aVisit our store &c/buy";
		CraftPlayer craftPlayer = (CraftPlayer) player;
		if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
			IChatBaseComponent header = ChatSerializer.a(TextConverter.convert(StringUtil.color(top)));
			IChatBaseComponent footer = ChatSerializer.a(TextConverter.convert(StringUtil.color(bottom)));
			craftPlayer.getHandle().playerConnection.sendPacket(new PacketTabHeader(header, footer));
		}*/
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					update(player);
				}
			}
		}, 20 * 2);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		event.setLeaveMessage(null);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
	}
	
	@EventHandler
	public void onPlayerRankChange(PlayerRankChangeEvent event) {
		if(event.getPlayer().isOnline()) {
			colorPlayerTab(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			player.setSaturation(4.0f);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent event) {
		if(Ranks.OWNER.hasRank(event.getPlayer())) {
			for(int a = 0; a < 4; ++a) {
				event.setLine(a, StringUtil.color(event.getLine(a)));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		
		// Chat delay for non premium players
		if(delayed.contains(player.getName())) {
			MessageHandler.sendMessage(player, "&cSlow down! Non " + Ranks.PREMIUM.getPrefix() + "&cchat delay of &e" + delay + " &cseconds");
			event.setCancelled(true);
			return;
		} else if(AccountHandler.getRank(player) == Ranks.PLAYER) {
			final String name = player.getName();
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * delay);
		}
		
		// Color codes for premium and above
		if(Ranks.PREMIUM.hasRank(player)) {
			event.setMessage(StringUtil.color(event.getMessage()));
			
			// Be sure owner rank has all color codes, do not remove any bad ones
			if(!Ranks.OWNER.hasRank(player)) {
				for(ChatColor badColor : new ChatColor [] {ChatColor.BOLD, ChatColor.MAGIC, ChatColor.UNDERLINE, ChatColor.STRIKETHROUGH}) {
					event.setMessage(event.getMessage().replace(badColor + "", ""));
				}
			}
		}
		
		// Prevent too many capital letters
		if(!Ranks.isStaff(player)) {
			int numberOfCaps = 0;
			for(char character : event.getMessage().toCharArray()) {
				if(character >= 65 && character <= 90) {
					if(++numberOfCaps >= 5) {
						event.setMessage(event.getMessage().toLowerCase());
						break;
					}
				}
			}
		}
		
		// Final formatting
		event.setMessage(event.getMessage().replace("%", "%%"));
		event.setFormat(AccountHandler.getPrefix(player, false) + ": " + event.getMessage());
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 5) {
			for(Databases database : Databases.values()) {
				database.connect();
			}
		}
	}
	
	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		if(event.getCommand().equalsIgnoreCase("stop")) {
			ProPlugin.restartServer();
			event.setCommand("");
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(Ranks.OWNER.hasRank(event.getPlayer()) && event.getMessage().equalsIgnoreCase("/stop")) {
			ProPlugin.restartServer();
			event.setCancelled(true);
		} else if(isBlockedCommand(event.getMessage())) {
			MessageHandler.sendUnknownCommand(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	private boolean isBlockedCommand(String message) {
		message = message.toLowerCase();
		String command = "";
		for(char character : message.toCharArray()) {
			if(character == '\0' || character == ' ') {
				break;
			} else {
				command += character;
			}
		}
		return blockedCommands.contains(command) || command.contains(":");
	}
	
	private void update(Player player) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(online.canSee(player)) {
				online.hidePlayer(player);
				online.showPlayer(player);
			}
		}
	}
}
