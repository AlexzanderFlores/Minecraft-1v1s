package ostb.server.servers.hub.pets.entities;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntitySquid;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import ostb.server.servers.hub.pets.EntityPet;
import ostb.server.util.ReflectionUtil;

public class SquidPet extends EntitySquid implements EntityPet {
	public SquidPet(World world) {
        super(world);
        try {
            for(String fieldName : new String[]{"b", "c"}) {
                Field field = ReflectionUtil.getDeclaredField(PathfinderGoalSelector.class, fieldName);
                field.setAccessible(true);
                field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
                field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSpawn(Player player) {
    	
    }

    @Override
    public void walkTo(Player player, float speed) {

    }

    @Override
    public Inventory getOptionsInventory(Player player, Inventory inventory) {
        return inventory;
    }

    @Override
    public void clickedOnCustomOption(Player player, ItemStack clicked) {
    	
    }

    @Override
    public void togglePetStaying(Player player) {

    }

    @Override
    public void togglePetSounds(Player player) {

    }

    @Override
    public void makeSound(Player player) {
        makeSound(super.z(), 1.0f, 1.0f);//this.bf(), this.bg());
    }

    @Override
    public void makeHurtSound(Player player) {
        makeSound(super.bo(), 1.0f, 1.0f);//this.bf(), this.bg());
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    protected String z() {
        return null;
    }

    @Override
    protected String bo() {
        return null;
    }

    @Override
    protected String bp() {
        return null;
    }

    @Override
    protected void a(BlockPosition blockPosition, Block block) {

    }

    @Override
    public void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(1000.0D);
    }
}
