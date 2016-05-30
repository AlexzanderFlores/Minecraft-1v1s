package ostb.gameapi.scenarios.scenarios;

import org.bukkit.Material;

import ostb.gameapi.scenarios.Scenario;

public class OrePower extends Scenario {
    private static OrePower instance = null;

    public OrePower() {
        super("OrePower", Material.IRON_ORE);
        instance = this;
    }

    public static OrePower getInstance() {
        if(instance == null) {
            new OrePower();
        }
        return instance;
    }
}
