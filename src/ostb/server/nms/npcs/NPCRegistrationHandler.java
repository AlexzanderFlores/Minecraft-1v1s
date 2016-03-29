package ostb.server.nms.npcs;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.entity.EntityType;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityCreeper;
import net.minecraft.server.v1_8_R3.EntityGuardian;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntitySnowman;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.EntityZombie;
import ostb.server.nms.npcs.entities.CreeperNPC;
import ostb.server.nms.npcs.entities.GuardianNPC;
import ostb.server.nms.npcs.entities.SkeletonNPC;
import ostb.server.nms.npcs.entities.SnowmanNPC;
import ostb.server.nms.npcs.entities.VillagerNPC;
import ostb.server.nms.npcs.entities.ZombieNPC;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class NPCRegistrationHandler {
	public enum NPCs {
		CREEPER(EntityCreeper.class, CreeperNPC.class),
		SKELETON(EntitySkeleton.class, SkeletonNPC.class),
		SNOWMAN(EntitySnowman.class, SnowmanNPC.class),
		VILLAGER(EntityVillager.class, VillagerNPC.class),
		ZOMBIE(EntityZombie.class, ZombieNPC.class),
		GUARDIAN(EntityGuardian.class, GuardianNPC.class);
		
		private Class<? extends Entity> defaultClass = null;
		private Class<? extends Entity> customClass = null;
		
		private NPCs(Class<? extends Entity> defaultClass, Class<? extends Entity> customClass) {
			this.defaultClass = defaultClass;
			this.customClass = customClass;
		}
		
		public void register() {
			try {
				((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), customClass);
				((Map) getPrivateStatic(EntityTypes.class, "d")).put(customClass, toString());
				int typeId = Integer.valueOf(EntityType.valueOf(toString()).getTypeId());
				((Map) getPrivateStatic(EntityTypes.class, "e")).put(typeId, customClass);
				((Map) getPrivateStatic(EntityTypes.class, "f")).put(customClass, typeId);
				((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), typeId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void unregister() {
			try {
				((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), defaultClass);
				((Map) getPrivateStatic(EntityTypes.class, "d")).put(defaultClass, toString());
				int typeId = Integer.valueOf(EntityType.valueOf(toString()).getTypeId());
				((Map) getPrivateStatic(EntityTypes.class, "e")).put(typeId, defaultClass);
				((Map) getPrivateStatic(EntityTypes.class, "f")).put(defaultClass, typeId);
				((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), typeId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private static Object getPrivateStatic(Class clazz, String f) throws Exception {
			Field field = clazz.getDeclaredField(f);
			field.setAccessible(true);
			return field.get(null);
		}
	}
}
