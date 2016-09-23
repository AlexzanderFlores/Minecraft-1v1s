package network.gameapi.competitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import network.Network;
import network.customevents.TimeEvent;
import network.customevents.game.GameEndingEvent;
import network.customevents.player.AsyncPlayerJoinEvent;
import network.customevents.player.PlayerItemFrameInteractEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.player.MessageHandler;
import network.player.account.AccountHandler.Ranks;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EffectUtil;
import network.server.util.EventUtil;
import network.server.util.ImageMap;
import network.server.util.StringUtil;
import npc.util.DelayedTask;

public class EloRanking implements Listener {
	public enum EloRank {
		BRONZE(-1, 100, "&6[Bronze]"),
		SILVER(0.60, 60, "&7[Silver]"),
		GOLD(0.36, 36, "&e[Gold]"),
		DIAMOND(0.18, 18, "&b[Diamond]"),
		PLATINUM(0.05, 5, "&d[Platinum]");
		
		private double percentage = 0;
		private int displayPercentage = 0;
		private int required = 0;
		private String prefix = null;
		
		private EloRank(double percentage, int displayPercentage, String prefix) {
			this.percentage = percentage;
			this.displayPercentage = displayPercentage;
			this.prefix = StringUtil.color(prefix);
		}
		
		public double getPercentage() {
			return this.percentage;
		}
		
		public int getDisplayPercentage() {
			return this.displayPercentage;
		}
		
		public String getPrefix() {
			return this.prefix;
		}
		
		public int getRequired() {
			return this.required;
		}
		
		public void setRequired(int required) {
			this.required = required;
		}
	};
	private static Map<UUID, EloRank> eloRanks = null;
	private static List<ItemFrame> frames = null;
	private static DB eloDB = null;
	private static DB rankDB = null;
	
	public EloRanking(List<ItemFrame> itemFrames, DB eloDB, DB rankDB) {
		eloRanks = new HashMap<UUID, EloRank>();
		frames = new ArrayList<ItemFrame>();
		EloRanking.eloDB = eloDB;
		EloRanking.rankDB = rankDB;
		String path = "/root/resources/Elo.png";
		for(ItemFrame itemFrame : itemFrames) {
			frames.addAll(new ImageMap(itemFrame, "Elo", path).getItemFrames());
		}
		loadData();
		EventUtil.register(this);
	}
	
	public static EloRank getRank(Player player) {
		EloRank rank = eloRanks.get(player.getUniqueId());
		if(rank == null) {
			eloRanks.put(player.getUniqueId(), EloRank.BRONZE);
			return EloRank.BRONZE;
		} else {
			return rank;
		}
	}
	
	public static void loadData() {
		MessageHandler.alert("Reloading elo ranks...");
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int size = eloDB.getSize();
				for(EloRank eloRank : EloRank.values()) {
					int start = 0;
					int end = (int) (size * eloRank.getPercentage());
					List<String> result = eloDB.getOrdered("elo", "elo", new int [] {start, end}, true);
					int required = 0;
					if(result != null && !result.isEmpty()) {
						required = Integer.valueOf(result.get(result.size() - 1));
					}
					eloRank.setRequired(required);
				}
			}
		});
	}
	
	public static void updateRanks() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			int elo = EloHandler.getElo(player);
			EloRank [] ranks = EloRank.values();
			for(int a = ranks.length - 1; a >= 0; --a) {
				EloRank eloRank = ranks[a];
				if(elo >= eloRank.getRequired()) {
					int currentRank = eloRanks.get(player.getUniqueId()).ordinal();
					int newRank = eloRank.ordinal();
					if(currentRank != newRank) {
						MessageHandler.sendMessage(player, "");
						if(newRank > currentRank) {
							MessageHandler.sendMessage(player, "&aYou have ranked up! New rank is " + eloRank.getPrefix() + " &x(Top &c" + eloRank.getDisplayPercentage() + "%&x)");
						} else if(currentRank > newRank) {
							MessageHandler.sendMessage(player, "&cYou have ranked down! New rank is " + eloRank.getPrefix() + " &x(Top &c" + eloRank.getDisplayPercentage() + "%&x)");
						}
						EffectUtil.playSound(player, Sound.LEVEL_UP);
						eloRanks.put(player.getUniqueId(), EloRank.values()[newRank]);
						MessageHandler.sendMessage(player, "");
					}
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemFrameInteract(PlayerItemFrameInteractEvent event) {
		if(frames.contains(event.getItemFrame())) {
			Player player = event.getPlayer();
			EloRank rank = getRank(player);
			MessageHandler.sendMessage(player, "You are within the percent range for " + rank.getPrefix() + " &x(Top &c" + rank.getDisplayPercentage() + "%&x)");
			if(Ranks.VIP.hasRank(player)) {
				MessageHandler.sendMessage(player, "Your exact Elo value is &e" + EloHandler.getElo(player));
			} else {
				MessageHandler.sendMessage(player, "&cTo view your exact Elo value you must have " + Ranks.VIP.getPrefix());
			}
		}
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		updateRanks();
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		int elo = EloHandler.getElo(player);
		EloRank rank = EloRank.BRONZE;
		EloRank [] ranks = EloRank.values();
		for(int a = ranks.length - 1; a >= 0; --a) {
			EloRank eloRank = ranks[a];
			if(elo >= eloRank.getRequired()) {
				rank = eloRank;
				break;
			}
		}
		eloRanks.put(uuid, rank);
		if(rankDB.isUUIDSet(uuid)) {
			rankDB.updateString("rank", rank.toString(), "uuid", uuid.toString());
		} else {
			rankDB.insert("'" + uuid.toString() + "', '" + rank.toString() + "'");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(eloRanks.containsKey(player.getUniqueId())) {
			event.setFormat(eloRanks.get(player.getUniqueId()).getPrefix() + " " + event.getFormat());
		}
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 20 * 60 * 5) {
			if(Network.getMiniGame() == null) {
				loadData();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						updateRanks();
					}
				}, 20);
			} else {
				TimeEvent.getHandlerList().unregister(this);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		eloRanks.remove(event.getPlayer().getUniqueId());
	}
}
