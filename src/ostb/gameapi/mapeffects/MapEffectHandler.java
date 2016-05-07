package ostb.gameapi.mapeffects;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class MapEffectHandler {
    private static List<MapEffectsBase> effects = null;

    public MapEffectHandler(World world) {
        for(MapEffectsBase effect : effects) {
            if(effect.getName() != null && effect.getName().equals(world.getName())) {
                effect.execute(world);
                break;
            }
        }
        effects.clear();
        effects = null;
    }

    public static void addEffect(MapEffectsBase effect) {
        if(effects == null) {
            effects = new ArrayList<MapEffectsBase>();
        }
        effects.add(effect);
    }
}
