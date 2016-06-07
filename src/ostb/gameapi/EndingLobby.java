package ostb.gameapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GameDeathEvent;
import ostb.customevents.game.GameEndingEvent;
import ostb.server.tasks.DelayedTask;
import ostb.server.util.EventUtil;
import ostb.server.util.ImageMap;

public class EndingLobby implements Listener {
	private List<String> topPlayers = null;
	private Location spawn = null;
	private String thirdPlace = null;
	private String secondPlace = null;
	private String firstPlace = null;
	private static String bestPlayer = null;
	
	public EndingLobby() {
		topPlayers = new ArrayList<String>();
		spawn = new Location(OSTB.getMiniGame().getLobby(), 0.5, 5, 299.5, 0.0f, 0.0f);
		EventUtil.register(this);
	}
	
	public void setBestPlayer(String name) {
		bestPlayer = name;
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		Random random = new Random();
		int range = 4;
		World world = OSTB.getMiniGame().getLobby();
		for(Player player : Bukkit.getOnlinePlayers()) {
			ProPlugin.resetPlayer(player);
			if(SpectatorHandler.isEnabled()) {
				SpectatorHandler.remove(player);
			}
			if(player.getName().equals(firstPlace)) {
				player.teleport(new Location(world, 0.5, 9, 309.5, -180.0f, 0.0f));
			} else if(player.getName().equals(secondPlace)) {
				player.teleport(new Location(world, 3.5, 8, 310.5, -180.0f, 0.0f));
			} else if(player.getName().equals(thirdPlace)) {
				player.teleport(new Location(world, -2.5, 7, 310.5, -180.0f, 0.0f));
			} else if(player.getName().equals(bestPlayer)) {
				player.teleport(new Location(world, 0.5, 8, 288.5, -360.0f, 0.0f));
			} else {
				int x = random.nextBoolean() ? random.nextInt(range) * -1 : random.nextInt(range);
				int z = random.nextBoolean() ? random.nextInt(range) * -1 : random.nextInt(range);
				player.teleport(spawn.clone().add(x, 0, z));
			}
		}
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				String path = Bukkit.getWorldContainer().getAbsolutePath().replace("/.", "") + "/../resources/";
				new ImageMap(ImageMap.getItemFrame(world, 0, 8, 307), "First Place", path + "first.png", 1, 2);
				new ImageMap(ImageMap.getItemFrame(world, 3, 7, 308), "Second Place", path + "second.png", 1, 2);
				new ImageMap(ImageMap.getItemFrame(world, -3, 6, 308), "Third Place", path + "third.png", 1, 2);
			}
		}, 20 * 2);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		if(OSTB.getMiniGame().getPlayersHaveOneLife()) {
			List<Player> players = ProPlugin.getPlayers();
			int size = players.size() - 1;
			Player player = event.getPlayer();
			if(size <= 3 && topPlayers.isEmpty()) {
				for(int a = players.size() - 1; a >= 0; --a) {
					String name = players.get(a).getName();
					if(!name.equals(player.getName())) {
						topPlayers.add(name);
					}
				}
			} else if(topPlayers.size() > 0) {
				if(thirdPlace == null) {
					thirdPlace = player.getName();
				} else if(secondPlace == null) {
					secondPlace = player.getName();
					for(Player alive : ProPlugin.getPlayers()) {
						if(!alive.getName().equals(secondPlace)) {
							firstPlace = alive.getName();
						}
					}
				}
			}
		}
	}
}
