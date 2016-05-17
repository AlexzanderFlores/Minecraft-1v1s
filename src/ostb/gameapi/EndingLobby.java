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
import ostb.server.util.EventUtil;
import ostb.server.util.FileHandler;
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
	
	private String loadImage(String ign, int index) {
		String url = "";
		switch(index) {
		case 0:
			url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=340&wt=20&abg=240&abd=130&ajg=330&ajd=30&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=186";
			break;
		case 1:
			url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=330&wt=30&abg=310&abd=50&ajg=340&ajd=30&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=727";
			break;
		case 2:
			url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=10&w=330&wt=30&abg=330&abd=110&ajg=350&ajd=10&ratio=15&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=761";
			break;
		default:
			return null;
		}
		String path = Bukkit.getWorldContainer().getPath() + "/" + OSTB.getMiniGame().getLobby().getName() + "/" + index + ".png";
		FileHandler.downloadImage(url, path);
		return path;
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
				player.teleport(new Location(world, 0.5, 12, 309.5, -180.0f, 0.0f));
			} else if(player.getName().equals(secondPlace)) {
				player.teleport(new Location(world, 3.5, 11, 310.5, -180.0f, 0.0f));
			} else if(player.getName().equals(thirdPlace)) {
				player.teleport(new Location(world, -2.5, 10, 310.5, -180.0f, 0.0f));
			} else if(player.getName().equals(bestPlayer)) {
				player.teleport(new Location(world, 0.5, 8, 288.5, -360.0f, 0.0f));
			} else {
				int x = random.nextBoolean() ? random.nextInt(range) * -1 : random.nextInt(range);
				int z = random.nextBoolean() ? random.nextInt(range) * -1 : random.nextInt(range);
				player.teleport(spawn.clone().add(x, 0, z));
			}
		}
		new ImageMap(ImageMap.getItemFrame(world, 1, 10, 307), "First Place", loadImage(firstPlace, 0));
		new ImageMap(ImageMap.getItemFrame(world, 4, 9, 308), "Second Place", loadImage(secondPlace, 1));
		new ImageMap(ImageMap.getItemFrame(world, -2, 8, 308), "Third Place", loadImage(thirdPlace, 2));
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
