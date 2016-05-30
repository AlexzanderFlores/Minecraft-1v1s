package ostb.gameapi.scenarios.scenarios;

import org.bukkit.Material;

import ostb.gameapi.scenarios.Scenario;

public class Vanilla extends Scenario {
    private static Vanilla instance = null;

    public Vanilla() {
        super("Vanilla", Material.DIAMOND_PICKAXE);
        instance = this;
        setInfo("Default Minecraft setting, no extra changes");
    }

    public static Vanilla getInstance() {
        if(instance == null) {
            new Vanilla();
        }
        return instance;
    }
}
