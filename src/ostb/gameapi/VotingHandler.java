package ostb.gameapi;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import npc.ostb.util.EventUtil;
import ostb.OSTB;
import ostb.OSTB.Plugins;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.util.ImageMap;

public class VotingHandler implements Listener {
	private Map<String, Integer> mapVotes = null;
	private Map<String, String> playerVotes = null;
	private Map<ItemFrame, String> itemFrames = null;
	private List<String> maps = null;
	
	public VotingHandler() {
		mapVotes = new HashMap<String, Integer>(3);
		playerVotes = new HashMap<String, String>();
		itemFrames = new HashMap<ItemFrame, String>(15);
		maps = new ArrayList<String>();
		String path = Bukkit.getWorldContainer().getPath() + "/../resources/maps/";
		String name = OSTB.getMiniGame().getDisplayName().toLowerCase();
		Plugins plugin = OSTB.getPlugin();
		if(plugin == Plugins.SKY_WARS_SOLO || plugin == Plugins.SKY_WARS_TEAMS) {
			name = "sky_wars";
		}
		name = path + name.replace(" ", "_");
		File file = new File(name);
		String [] folders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		for(String folder : folders) {
			if(new File(file.getPath() + "/" + folder + "/Render.png").exists()) {
				maps.add(folder);
			}
		}
		Random random = new Random();
		while(maps.size() > 3) {
			maps.remove(random.nextInt(maps.size()));
		}
		World lobby = OSTB.getMiniGame().getLobby();
		ItemFrame [] itemFrames = new ItemFrame [] {ImageMap.getItemFrame(lobby, 12, 7, 7), ImageMap.getItemFrame(lobby, 2, 7, 7), ImageMap.getItemFrame(lobby, -8, 7, 7)};
		for(int a = 0; a < maps.size(); ++a) {
			String map = maps.get(a);
			String render = name + "/" + map + "/Render.png";
			for(ItemFrame itemFrame : new ImageMap(itemFrames[a], render).getItemFrames()) {
				this.itemFrames.put(itemFrame, map);
			}
		}
		EventUtil.register(this);
	}
	
	private void vote(Player player, String map) {
		int votes = Ranks.getVotes(player);
		if(playerVotes.containsKey(player.getName())) {
			String oldMap = playerVotes.get(player.getName());
			mapVotes.put(oldMap, mapVotes.get(oldMap) - votes);
			MessageHandler.sendMessage(player, "&c-" + votes + " Votes for &a" + oldMap);
			playerVotes.remove(player.getName());
		}
		playerVotes.put(player.getName(), map);
		if(mapVotes.containsKey(map)) {
			mapVotes.put(map, mapVotes.get(map) + votes);
		} else {
			mapVotes.put(map, votes);
		}
		MessageHandler.sendMessage(player, "+" + votes + " Votes for &a" + map);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof ItemFrame && event.getDamager() instanceof Player) {
			ItemFrame itemFrame = (ItemFrame) event.getEntity();
			if(itemFrames.containsKey(itemFrame)) {
				String map = itemFrames.get(itemFrame);
				Player player = (Player) event.getDamager();
				vote(player, map);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
			if(itemFrames.containsKey(itemFrame)) {
				String map = itemFrames.get(itemFrame);
				vote(event.getPlayer(), map);
			}
		}
	}
}
