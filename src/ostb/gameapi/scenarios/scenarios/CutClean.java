package ostb.gameapi.scenarios.scenarios;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.scenarios.Scenario;
import ostb.server.util.EffectUtil;

@SuppressWarnings("deprecation")
public class CutClean extends Scenario {
	private static CutClean instance = null;
	
	public CutClean() {
		super("Cut Clean", Material.IRON_INGOT);
		instance = this;
		enable(true);
	}
	
	public static CutClean getInstance() {
		if(instance == null) {
			new CutClean();
		}
		return instance;
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material material = event.getEntity().getItemStack().getType();
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if(material == Material.GOLD_ORE) {
			world.dropItem(event.getLocation(), new ItemStack(Material.GOLD_INGOT));
			ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(event.getLocation(), EntityType.EXPERIENCE_ORB);
			exp.setExperience(1);
			entity.remove();
		} else if(material == Material.IRON_ORE) {
			world.dropItem(event.getLocation(), new ItemStack(Material.IRON_INGOT));
			ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(event.getLocation(), EntityType.EXPERIENCE_ORB);
			exp.setExperience(1);
			entity.remove();
		} else if(material == Material.RAW_BEEF) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_BEEF));
			entity.remove();
		} else if(material == Material.RAW_CHICKEN) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_CHICKEN));
			entity.remove();
		} else if(material == Material.RAW_FISH) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_FISH));
			entity.remove();
		} else if(material == Material.POTATO) {
			world.dropItem(event.getLocation(), new ItemStack(Material.BAKED_POTATO));
			entity.remove();
		} else if(material == Material.PORK) {
			world.dropItem(event.getLocation(), new ItemStack(Material.GRILLED_PORK));
			entity.remove();
		} else if(material == Material.MUTTON) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_MUTTON));
			entity.remove();
		} else if(material == Material.RABBIT) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_RABBIT));
			entity.remove();
		} else if(material == Material.GRAVEL && new Random().nextBoolean()) {
			world.dropItem(event.getLocation(), new ItemStack(Material.FLINT));
			entity.remove();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			Block block = event.getBlock();
			Material below = block.getRelative(0, -1, 0).getType();
			if(below == Material.DIRT || below == Material.SAND) {
				Location location = block.getLocation().clone();
				int id = block.getTypeId();
				while(id == 162 || id == 17) {
					EffectUtil.displayParticles(block.getType(), block.getLocation());
					for(ItemStack itemStack : block.getDrops()) {
						block.getWorld().dropItem(location, itemStack);
					}
					block.setType(Material.AIR);
					block = block.getRelative(0, 1, 0);
					id = block.getTypeId();
				}
			}
		}
	}
}
