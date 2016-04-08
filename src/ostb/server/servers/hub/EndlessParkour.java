package ostb.server.servers.hub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import ostb.customevents.TimeEvent;
import ostb.customevents.player.PlayerLeaveEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class EndlessParkour implements Listener {
	private Map<String, Block> blocks = null;
	private Map<String, SidebarScoreboardUtil> scoreboards = null;
	private Map<String, Integer> scores = null;
	private Map<String, Block> lastScoredOn = null;
	private int counter = 0;
	private Random random = null;
	private String topPlayer = null;
	private int topScore = 0;
	
	public EndlessParkour() {
		blocks = new HashMap<String, Block>();
		scoreboards = new HashMap<String, SidebarScoreboardUtil>();
		scores = new HashMap<String, Integer>();
		lastScoredOn = new HashMap<String, Block>();
		random = new Random();
		loadTopData();
		EventUtil.register(this);
	}
	
	private boolean start(Player player) {
		if(counter <= 0) {
			counter = 5;
			if(player.getAllowFlight()) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
			Block block = Bukkit.getWorlds().get(0).getBlockAt(1586, 4, -1263);
			lastScoredOn.put(player.getName(), block);
			blocks.put(player.getName(), block);
			place(player.getName());
			Location location = block.getLocation().clone().add(1.5, 1, 0.5);
			location.setYaw(-270.0f);
			location.setPitch(25.0f);
			player.teleport(location);
			scores.put(player.getName(), 1);
			final int personalBest = DB.HUB_PARKOUR_ENDLESS_SCORES.getInt("uuid", player.getUniqueId().toString(), "best_score");
			Events.removeSidebar(player);
			SidebarScoreboardUtil sidebar = new SidebarScoreboardUtil(" &aEndless Parkour ") {
				@Override
				public void update(Player player) {
					removeScore(11);
					setText(new String [] {
						" ",
						"&eCurrent Score",
						"&b" + scores.get(player.getName()) + " ",
						"  ",
						"&ePersonal Best",
						"&b" + personalBest + "  ",
						"   ",
						"&eTop Player",
						"&b" + topPlayer,
						"    ",
						"&eTop Score",
						"&b" + topScore + "   ",
						"     ",
					});
				}
			};
			scoreboards.put(player.getName(), sidebar);
			player.setScoreboard(sidebar.getScoreboard());
			sidebar.update(player);
			return true;
		}
		return false;
	}
	
	private void place(String name) {
		final Block oldBlock = blocks.get(name);
		int offsetX = -5;
		int offsetZ = random.nextBoolean() ? random.nextInt(1) + 1 : (random.nextInt(1) + 1) * -1;
		int index = random.nextInt(3);
		int offsetY = index == 0 ? 0 : index == 1 ? 1 : -1;
		if(oldBlock.getY() < 5 && offsetY < 0) {
			offsetY = 1;
		} else if(oldBlock.getY() > 10 && offsetY > 0) {
			offsetY = -1;
			--offsetX;
		} else if(random.nextInt(100) <= 15) {
			--offsetX;
			if(offsetY > 0) {
				offsetY = 0;
			}
		}
		Block newBlock = scores.containsKey(name) ? oldBlock.getRelative(offsetX, offsetY, offsetZ) : oldBlock;
		newBlock.getChunk().load(true);
		newBlock.setType(Material.STAINED_GLASS);
		newBlock.setData((byte) random.nextInt(15));
		for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0)}) {
			Block near = newBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
			near.setType(Material.STAINED_GLASS);
			near.setData((byte) random.nextInt(15));
		}
		blocks.put(name, newBlock);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(int a = 0; a < 2; ++a) {
					oldBlock.setType(Material.AIR);
					oldBlock.setData((byte) 0);
					for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0)}) {
						Block near = oldBlock.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
						near.setType(Material.AIR);
						near.setData((byte) 0);
					}
				}
			}
		}, 20 * 2);
	}
	
	private void remove(final Player player, boolean teleport) {
		String name = player.getName();
		if(blocks.containsKey(name)) {
			Block block = blocks.get(name);
			blocks.remove(name);
			block.setType(Material.AIR);
			block.setData((byte) 0);
			for(Vector offset : new Vector [] {new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1), new Vector(0, -1, 0)}) {
				Block near = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
				near.setType(Material.AIR);
				near.setData((byte) 0);
			}
			if(Ranks.PREMIUM.hasRank(player)) {
				player.setAllowFlight(true);
			}
			if(teleport) {
				player.teleport(ParkourNPC.getEndlessLocation());
			}
		}
		final int score = scores.get(name);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				UUID uuid = player.getUniqueId();
				int bestScore = DB.HUB_PARKOUR_ENDLESS_SCORES.getInt("uuid", uuid.toString(), "best_score");
				if(score > bestScore) {
					if(DB.HUB_PARKOUR_ENDLESS_SCORES.isUUIDSet(uuid)) {
						DB.HUB_PARKOUR_ENDLESS_SCORES.updateInt("best_score", score, "uuid", uuid.toString());
					} else {
						DB.HUB_PARKOUR_ENDLESS_SCORES.insert("'" + uuid.toString() + "', '" + score + "'");
					}
					MessageHandler.sendMessage(player, "&6New Personal Best: &e" + score);
					List<String> top = DB.HUB_PARKOUR_ENDLESS_SCORES.getOrdered("best_score", "uuid", 1, true);
					if(!top.isEmpty() && top.get(0).equals(uuid.toString())) {
						MessageHandler.sendMessage(player, "&4&k|||&6 New Top Score: &e" + score + " &4&k|||");
					}
				} else {
					MessageHandler.sendMessage(player, "Your score: &e" + score);
				}
			}
		});
		scores.remove(name);
		if(scoreboards.containsKey(name)) {
			scoreboards.get(name).remove();
			scoreboards.remove(name);
			Events.giveSidebar(player);
		}
		lastScoredOn.remove(name);
	}
	
	private void loadTopData() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<String> top = DB.HUB_PARKOUR_ENDLESS_SCORES.getOrdered("best_score", "uuid", 1, true);
				if(top.isEmpty()) {
					topPlayer = "None";
				} else {
					UUID topPlayerUUID = UUID.fromString(top.get(0));
					topPlayer = AccountHandler.getName(topPlayerUUID);
					topScore = DB.HUB_PARKOUR_ENDLESS_SCORES.getInt("uuid", topPlayerUUID.toString(), "best_score");
				}
			}
		});
	}
	
	@EventHandler
	public void onTime(TimeEvent event) {
		long ticks = event.getTicks();
		if(ticks == 17) {
			for(String name : blocks.keySet()) {
				place(name);
			}
		} else if(ticks == 20) {
			--counter;
		} else if(ticks == (20 * 60)) {
			loadTopData();
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		if(blocks.containsKey(player.getName())) {
			if(to.getY() < 0) {
				remove(player, true);
				return;
			}
			String name = player.getName();
			Block below = player.getLocation().getBlock().getRelative(0, -1, 0);
			if(below.getType() == Material.STAINED_GLASS && below.getRelative(0, -1, 0).getType() == Material.STAINED_GLASS && !lastScoredOn.get(name).equals(below)) {
				lastScoredOn.put(name, below);
				scores.put(name, scores.get(name) + 1);
				scoreboards.get(name).update(player);
			}
		}
		int x = to.getBlockX();
		if(x == 1589) {
			int y = to.getBlockY();
			if(y == 5) {
				int z = to.getBlockZ();
				if(z >= -1264 && z <= -1262) {
					if(!start(player)) {
						player.teleport(ParkourNPC.getEndlessLocation());
						MessageHandler.sendMessage(player, "&cPlease wait &e" + counter + " &csecond" + (counter == 1 ? "" : "s"));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer(), false);
	}
}
