package ostb.gameapi.games.pvpbattles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import npc.util.EventUtil;
import ostb.OSTB;
import ostb.customevents.player.AsyncPlayerJoinEvent;
import ostb.customevents.player.PlayerItemFrameInteractEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.gameapi.EloHandler;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.ImageMap;
import ostb.server.util.StringUtil;

public class Ranking implements Listener {
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
	private List<ItemFrame> frames = null;
	
	public Ranking() {
		eloRanks = new HashMap<UUID, EloRank>();
		frames = new ArrayList<ItemFrame>();
		String path = Bukkit.getWorldContainer().getPath() + "/../resources/Elo.png";
		World lobby = OSTB.getMiniGame().getLobby();
		frames.addAll(new ImageMap(ImageMap.getItemFrame(lobby, 14, 7, -2), "Elo", path).getItemFrames());
		frames.addAll(new ImageMap(ImageMap.getItemFrame(lobby, -14, 7, 2), "Elo", path).getItemFrames());
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				DB table = DB.PLAYERS_PVP_BATTLES_ELO;
				int size = table.getSize();
				for(EloRank eloRank : EloRank.values()) {
					int start = 0;
					int end = (int) (size * eloRank.getPercentage());
					List<String> result = table.getOrdered("elo", "elo", new int [] {start, end}, true);
					int required = 0;
					if(result != null && !result.isEmpty()) {
						required = Integer.valueOf(result.get(result.size() - 1));
					}
					eloRank.setRequired(required);
				}
			}
		});
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
	
	@EventHandler
	public void onPlayerItemFrameInteract(PlayerItemFrameInteractEvent event) {
		if(frames.contains(event.getItemFrame())) {
			Player player = event.getPlayer();
			EloRank rank = getRank(player);
			MessageHandler.sendMessage(player, "You are within the percent range for " + rank.getPrefix() + " &x(Top &c" + rank.getDisplayPercentage() + "%&x)");
			if(Ranks.PREMIUM.hasRank(player)) {
				MessageHandler.sendMessage(player, "Your exact Elo value is &e" + EloHandler.getElo(player));
			} else {
				MessageHandler.sendMessage(player, "&cTo view your exact Elo value you must have " + Ranks.PREMIUM.getPrefix());
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		int elo = 1000;
		if(DB.PLAYERS_PVP_BATTLES_ELO.isUUIDSet(uuid)) {
			elo = DB.PLAYERS_PVP_BATTLES_ELO.getInt("uuid", uuid.toString(), "elo");
		}
		EloHandler.add(player, elo);
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
		if(DB.PLAYERS_PVP_BATTLES_RANK.isUUIDSet(uuid)) {
			DB.PLAYERS_PVP_BATTLES_RANK.updateString("rank", rank.toString(), "uuid", uuid.toString());
		} else {
			DB.PLAYERS_PVP_BATTLES_RANK.insert("'" + uuid.toString() + "', '" + rank.toString() + "'");
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
	public void onPlayerLeave(PlayerLeaveEvent event) {
		eloRanks.remove(event.getPlayer().getUniqueId());
	}
}
