package ostb.server.servers.hub.main;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftGuardian;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityGuardian;
import ostb.OSTB;
import ostb.server.CommandBase;
import ostb.server.nms.npcs.NPCEntity;
import ostb.server.servers.hub.HubBase;

public class MainHub extends HubBase {
	private NPCEntity npc = null;
	
	public MainHub() {
		super("MainHub");
		addGroup("mainhub");
		new MainHubTop5();
		new CommandBase("test", 1, 3, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments[0].equalsIgnoreCase("spawn")) {
					if(arguments.length == 1) {
						player.teleport(player.getWorld().getSpawnLocation());
						return true;
					}
					if(npc != null) {
						npc.remove();
					}
					Location loc = player.getLocation();
					Location location = new Location(player.getWorld(), loc.getX(), loc.getY(), loc.getZ());
					npc = new NPCEntity(EntityType.valueOf(arguments[1].toUpperCase()), "Testing NPC", location) {
						@Override
						public void onInteract(Player player) {
							
						}
					};
				} else if(arguments[0].equalsIgnoreCase("kill")) {
					if(npc != null) {
						npc.remove();
						npc = null;
					}
				} else if(arguments[0].equalsIgnoreCase("setSize") && npc.getLivingEntity() instanceof Guardian) {
					Guardian guardian = (Guardian) npc.getLivingEntity();
					CraftGuardian craftGuardian = (CraftGuardian) guardian;
					EntityGuardian entityGuardian = (EntityGuardian) craftGuardian.getHandle();
					entityGuardian.setSize(Float.valueOf(arguments[1]), Float.valueOf(arguments[2]));
				} else if(arguments[0].equalsIgnoreCase("toggle") && npc.getLivingEntity() instanceof Guardian) {
					Guardian guardian = (Guardian) npc.getLivingEntity();
					guardian.setElder(!guardian.isElder());
				}
				return true;
			}
		};
	}
	
	public static int getHubNumber() {
		return Integer.valueOf(OSTB.getServerName().toLowerCase().replace("hub", ""));
	}
}
