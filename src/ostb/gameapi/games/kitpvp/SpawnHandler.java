package ostb.gameapi.games.kitpvp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Team;

import ostb.OSTB;
import ostb.gameapi.games.kitpvp.events.TeamSelectEvent;
import ostb.player.MessageHandler;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class SpawnHandler implements Listener {
	private Map<Team, Location> spawns = null;
	private TeamHandler teamHandler = null;
	private int range = 5;
	private Random random = null;
	
	public SpawnHandler() {
		spawns = new HashMap<Team, Location>();
		World world = OSTB.getMiniGame().getMap();
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/spawns.yml");
		teamHandler = KitPVP.getKitPVPTeamHandler();
		for(Team team : teamHandler.getTeams()) {
			double x = config.getConfig().getDouble(team.getName() + ".x");
			double y = config.getConfig().getDouble(team.getName() + ".y");
			double z = config.getConfig().getDouble(team.getName() + ".z");
			float yaw = (float) config.getConfig().getDouble(team.getName() + ".yaw");
			float pitch = (float) config.getConfig().getDouble(team.getName() + ".pitch");
			spawns.put(team, new Location(world, x, y, z, yaw, pitch));
		}
		random = new Random();
		EventUtil.register(this);
	}
	
	private Location spawn(Player player) {
		for(Team team : teamHandler.getTeams()) {
			if(team.hasPlayer(player)) {
				double x = random.nextBoolean() ? random.nextInt(range) : random.nextInt(range) * -1;
				double z = random.nextBoolean() ? random.nextInt(range) : random.nextInt(range) * -1;
				Location location = spawns.get(team).clone().add(x, 0, z);
				player.teleport(location);
				return location;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onTeamSelect(TeamSelectEvent event) {
		spawn(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(spawn(event.getPlayer()));
		event.getPlayer().setNoDamageTicks(20 * 10);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if((player.getNoDamageTicks() / 20) > 0) {
				Player damager = null;
				if(event.getDamager() instanceof Player) {
					damager = (Player) event.getDamager();
				} else if(event.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) event.getDamager();
					if(projectile.getShooter() instanceof Player) {
						damager = (Player) projectile.getShooter();
					}
				}
				if(damager != null) {
					MessageHandler.sendMessage(damager, "&cThat player is under spawn protection for &e" + (player.getNoDamageTicks() / 20) + "s");
				}
			}
		}
	}
}
