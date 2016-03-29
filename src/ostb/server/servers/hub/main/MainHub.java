package ostb.server.servers.hub.main;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import ostb.OSTB;
import ostb.server.CommandBase;
import ostb.server.nms.PathfinderGoalWalkToLocation;
import ostb.server.nms.npcs.NPCEntity;
import ostb.server.servers.hub.HubBase;

public class MainHub extends HubBase {
	private NPCEntity npc = null;
	
	public MainHub() {
		super("MainHub");
		addGroup("mainhub");
		new MainHubTop5();
		new CommandBase("test", 1, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments[0].equalsIgnoreCase("spawn")) {
					if(npc != null) {
						npc.remove();
					}
					npc = new NPCEntity(EntityType.valueOf(arguments[1].toUpperCase()), "Testing NPC", player.getLocation()) {
						@Override
						public void onInteract(Player player) {
							
						}
					};
				} else if(arguments[0].equalsIgnoreCase("walk")) {
					CraftLivingEntity craftLivingEntity = (CraftLivingEntity) npc.getLivingEntity();
					EntityLiving entityLiving = (EntityLiving) craftLivingEntity.getHandle();
					EntityInsentient entityInsentient = (EntityInsentient) entityLiving;
					npc.setPathfinder(new PathfinderGoalWalkToLocation(entityInsentient, 1.0f, player.getLocation()));
				} else if(arguments[0].equalsIgnoreCase("kill")) {
					if(npc != null) {
						npc.remove();
						npc = null;
					}
				}
				return true;
			}
		};
	}
	
	public static int getHubNumber() {
		return Integer.valueOf(OSTB.getServerName().toLowerCase().replace("hub", ""));
	}
}
