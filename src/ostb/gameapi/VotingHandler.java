package ostb.gameapi;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import ostb.OSTB;
import ostb.customevents.game.GameStartingEvent;
import ostb.customevents.player.PlayerItemFrameInteractEvent;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.DB;
import ostb.server.tasks.AsyncDelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
import ostb.server.util.ImageMap;
import ostb.server.util.StringUtil;

public class VotingHandler implements Listener {
	private Map<String, Integer> mapVotes = null;
	private Map<String, String> playerVotes = null;
	private Map<String, ArmorStand> holograms = null;
	private Map<ItemFrame, String> itemFrames = null;
	private Map<Block, String> walls = null;
	private List<String> maps = null;
	
	public VotingHandler() {
		mapVotes = new HashMap<String, Integer>();
		playerVotes = new HashMap<String, String>();
		holograms = new HashMap<String, ArmorStand>();
		itemFrames = new HashMap<ItemFrame, String>();
		walls = new HashMap<Block, String>();
		maps = new ArrayList<String>();
		String path = Bukkit.getWorldContainer().getPath() + "/../resources/maps/";
		String name = OSTB.getPlugin().getData();
		name = path + name.replace(" ", "_");
		File file = new File(name);
		String [] folders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		if(folders == null) {
			return;
		}
		for(String folder : folders) {
			if(new File(file.getPath() + "/" + folder + "/Render.png").exists()) {
				maps.add(folder);
			}
		}
		while(maps.size() > 3) {
			Random random = new Random();
			int index = random.nextInt(maps.size());
			String map = maps.get(index);
			Bukkit.getLogger().info("Removing map " + map);
			maps.remove(index);
		}
		for(String map : maps) {
			Bukkit.getLogger().info("Map remaining: " + map);
		}
		World lobby = OSTB.getMiniGame().getLobby();
		Vector [] vectors = new Vector [] {new Vector(10.5, 6.75, 6.5), new Vector(0.5, 6.75, 6.5), new Vector(-10.5, 6.75, 6.5)};
		for(int a = 0; a < maps.size(); ++a) {
			String map = maps.get(a);
			mapVotes.put(map, 0);
			ArmorStand armorStand = (ArmorStand) lobby.spawnEntity(vectors[a].toLocation(lobby), EntityType.ARMOR_STAND);
			armorStand.setGravity(false);
			armorStand.setVisible(false);
			armorStand.setCustomNameVisible(true);
			holograms.put(map, armorStand);
			updateHologram(map);
		}
		try {
			ItemFrame [] itemFrames = new ItemFrame [] {ImageMap.getItemFrame(lobby, 12, 7, 7), ImageMap.getItemFrame(lobby, 2, 7, 7), ImageMap.getItemFrame(lobby, -8, 7, 7)};
			for(int a = 0; a < maps.size(); ++a) {
				String map = maps.get(a);
				String render = name + "/" + map + "/Render.png";
				for(ItemFrame itemFrame : new ImageMap(itemFrames[a], map, render).getItemFrames()) {
					this.itemFrames.put(itemFrame, map);
					walls.put(lobby.getBlockAt(itemFrame.getLocation()).getRelative(0, 0, 1), map);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		ArmorStand armorStand = (ArmorStand) lobby.spawnEntity(new Location(lobby, 0.5, 5, 2.5), EntityType.ARMOR_STAND);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setCustomName(StringUtil.color("&e&nVote by clicking the map image"));
		armorStand.setCustomNameVisible(true);
		EventUtil.register(this);
	}
	
	private void updateHologram(String map) {
		holograms.get(map).setCustomName(StringUtil.color("&a" + map + "&b (" + mapVotes.get(map) + " Votes)"));
	}
	
	private void vote(Player player, String map) {
		int votes = Ranks.getVotes(player);
		if(playerVotes.containsKey(player.getName())) {
			String oldMap = playerVotes.get(player.getName());
			mapVotes.put(oldMap, mapVotes.get(oldMap) - votes);
			MessageHandler.sendMessage(player, "&c-" + votes + " Votes for &a" + oldMap);
			playerVotes.remove(player.getName());
			updateHologram(oldMap);
		}
		playerVotes.put(player.getName(), map);
		if(mapVotes.containsKey(map)) {
			mapVotes.put(map, mapVotes.get(map) + votes);
		} else {
			mapVotes.put(map, votes);
		}
		MessageHandler.sendMessage(player, "+" + votes + " Votes for &a" + map);
		updateHologram(map);
	}
	
	@EventHandler
	public void onPlayerItemFrameInteract(PlayerItemFrameInteractEvent event) {
		ItemFrame itemFrame = event.getItemFrame();
		if(itemFrames.containsKey(itemFrame)) {
			String map = itemFrames.get(itemFrame);
			vote(event.getPlayer(), map);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if(block != null && walls.containsKey(block)) {
			String map = walls.get(block);
			vote(event.getPlayer(), map);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onGameStarting(GameStartingEvent event) {
		String winner = null;
		for(String map : mapVotes.keySet()) {
			if(winner == null || mapVotes.get(map) > mapVotes.get(winner) || (mapVotes.get(map) == mapVotes.get(winner) && new Random().nextBoolean())) {
				winner = map;
			}
		}
		final String worldName = winner.replace(" ", "_");
		String path = Bukkit.getWorldContainer().getPath() + "/../resources/maps/" + OSTB.getPlugin().getData() + "/" + worldName;
		File world = new File(Bukkit.getWorldContainer().getPath() + "/" + worldName);
		if(world.exists()) {
			FileHandler.delete(world);
		}
		FileHandler.copyFolder(path, world.getPath());
		World map = Bukkit.createWorld(new WorldCreator(worldName));
		for(Entity entity : map.getEntities()) {
			if(entity instanceof LivingEntity && !(entity instanceof Player)) {
				entity.remove();
			}
		}
		OSTB.getMiniGame().setMap(map);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String game = OSTB.getPlugin().toString();
				String [] keys = new String [] {"game_name", "map"};
				String [] values = new String [] {game, worldName};
				if(DB.NETWORK_MAP_VOTES.isKeySet(keys, values)) {
					int times = DB.NETWORK_MAP_VOTES.getInt(keys, values, "times_voted") + 1;
					DB.NETWORK_MAP_VOTES.updateInt("times_voted", times, keys, values);
				} else {
					DB.NETWORK_MAP_VOTES.insert("'" + game + "', '" + worldName + "', '1'");
				}
			}
		});
		HandlerList.unregisterAll(this);
	}
}
