package ostb.server.nms.npcs.entities;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityCreeper;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import ostb.server.nms.npcs.NPCEntity;

public class CreeperNPC extends EntityCreeper {
	public CreeperNPC(World world) {
		super(world);
		try {
			for(String fieldName : new String [] {"b", "c"}) {
				Field field = PathfinderGoalSelector.class.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
				field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String z() {
		return null;
	}
	
	@Override
	public void collide(Entity entity) {
		LivingEntity livingEntity = (LivingEntity) getBukkitEntity();
		NPCEntity npc = NPCEntity.getNPC(livingEntity);
		npc.getLivingEntity().teleport(npc.getStartingLocation());
	}
	
	@Override
	public void move(double x, double y, double z) {
		super.move(x, y, z);
		LivingEntity livingEntity = (LivingEntity) getBukkitEntity();
		NPCEntity npc = NPCEntity.getNPC(livingEntity);
		npc.teleportArmorStand();
	}
}
