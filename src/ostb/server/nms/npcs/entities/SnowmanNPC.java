package ostb.server.nms.npcs.entities;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.EntitySnowman;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;

import ostb.server.nms.npcs.NPCEntity;

public class SnowmanNPC extends EntitySnowman {
	public SnowmanNPC(World world) {
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
	public void g(double x, double y, double z) {
		LivingEntity livingEntity = (LivingEntity) getBukkitEntity();
		if(getBukkitEntity().getTicksLived() <= NPCEntity.ableToMove && NPCEntity.getNPC(livingEntity).getSpawnMover()) {
			super.g(x, y, z);
		}
	}
	
	@Override
	public void move(double x, double y, double z) {
		LivingEntity livingEntity = (LivingEntity) getBukkitEntity();
		if(getBukkitEntity().getTicksLived() <= NPCEntity.ableToMove && NPCEntity.getNPC(livingEntity).getSpawnMover()) {
			super.move(x, y, z);
		}
	}
}
