package ostb.gameapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
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
import ostb.server.util.ItemCreator;
import ostb.server.util.ItemUtil;

public class EndingLobby implements Listener {
	private List<String> topPlayers = null;
	private Location spawn = null;
	private String thirdPlace = null;
	private String secondPlace = null;
	private String firstPlace = null;
	
	public EndingLobby() {
		topPlayers = new ArrayList<String>();
		World world = OSTB.getMiniGame().getLobby();
		spawn = new Location(world, 0.5, 5, 299.5, 0.0f, 0.0f);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		Random random = new Random();
		int range = 4;
		World world = OSTB.getMiniGame().getLobby();
		String bestKiller = null;
		int mostKills = 0;
		for(Player player : Bukkit.getOnlinePlayers()) {
			int kills = KillLogger.getKills(player);
			if(bestKiller == null || kills > mostKills) {
				bestKiller = player.getName();
				mostKills = kills;
			}
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
			} else {
				int x = random.nextBoolean() ? random.nextInt(range) * -1 : random.nextInt(range);
				int z = random.nextBoolean() ? random.nextInt(range) * -1 : random.nextInt(range);
				player.teleport(spawn.clone().add(x, 0, z));
			}
		}
		Location bestKillerLocation = new Location(world, 0.5, 8, 288.5, -360.0f, 0.0f);;
		Player killer = ProPlugin.getPlayer(bestKiller);
		if(!(killer == null || killer.getLocation().getY() >= 7)) {
			killer.teleport(bestKillerLocation);
		}
		final String finalBestKiller = bestKiller;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				String path = Bukkit.getWorldContainer().getAbsolutePath().replace("/.", "") + "/../resources/";
				new ImageMap(ImageMap.getItemFrame(world, 0, 8, 307), "First Place", path + "first.png", 1, 2);
				new ImageMap(ImageMap.getItemFrame(world, 3, 7, 308), "Second Place", path + "second.png", 1, 2);
				new ImageMap(ImageMap.getItemFrame(world, -3, 6, 308), "Third Place", path + "third.png", 1, 2);
				ItemFrame itemFrame = ImageMap.getItemFrame(world, 0, 6, 290);
				itemFrame.setItem(new ItemCreator(Material.TRIPWIRE_HOOK).setName("&a+1 Key Fragment").setGlow(true).getItemStack());
				if(killer == null || killer.getLocation().getY() >= 7) {
					ArmorStand armorStand = (ArmorStand) world.spawnEntity(bestKillerLocation, EntityType.ARMOR_STAND);
					armorStand.setHelmet(ItemUtil.getSkull(finalBestKiller));
					armorStand.setCustomNameVisible(true);
					armorStand.setCustomName(finalBestKiller);
					armorStand.setBasePlate(false);
				}
				ArmorStand armorStand = (ArmorStand) world.spawnEntity(bestKillerLocation.add(0, 1, 0), EntityType.ARMOR_STAND);
				armorStand.setGravity(false);
				armorStand.setVisible(false);
				armorStand.setCustomNameVisible(true);
				armorStand.setCustomName("&e&nBest Killer");
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
