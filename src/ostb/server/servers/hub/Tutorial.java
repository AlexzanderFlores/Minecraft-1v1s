package ostb.server.servers.hub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import npc.NPCEntity;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class Tutorial implements Listener {
	private String name = null;
	
	public Tutorial() {
		name = "Tutorial";
		new NPCEntity(EntityType.GUARDIAN, "&e&n" + name, new Location(Bukkit.getWorlds().get(0), 1682.5, 6.5, -1295.5)) {
			@Override
			public void onInteract(Player player) {
				EffectUtil.playSound(player, Sound.SWIM);
			}
		};
		EventUtil.register(this);
	}
}
