package ostb.gameapi.uhc.scenarios.scenarios;

import org.bukkit.Material;

import ostb.gameapi.uhc.scenarios.Scenario;

public class OrePower extends Scenario {
    private static OrePower instance = null;

    public OrePower() {
        super("OrePower", "OP", Material.IRON_ORE);
        instance = this;
        setPrimary(true);
    }

    public static OrePower getInstance() {
        if(instance == null) {
            new OrePower();
        }
        return instance;
    }
}
