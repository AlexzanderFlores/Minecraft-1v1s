package ostb.gameapi.games.onevsones;

import java.io.File;

import org.bukkit.Bukkit;

import ostb.ProPlugin;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.StatsHandler;
import ostb.player.TeamScoreboardHandler;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.FileHandler;

public class OnevsOnes extends ProPlugin {
	public OnevsOnes() {
		super("1v1s");
		setAllowEntityDamage(true);
        setAllowEntityDamageByEntities(true);
        setAllowPlayerInteraction(true);
        setAllowBowShooting(true);
        setAllowInventoryClicking(true);
        setFlintAndSteelUses(2);
        setAllowEntityCombusting(true);
        setAllowInventoryClicking(true);
        setAutoVanishStaff(true);
        new SpectatorHandler();
		new ServerLogger();
		new TeamScoreboardHandler();
		new StatsHandler(DB.PLAYERS_STATS_ONE_VS_ONE, DB.PLAYERS_STATS_ONE_VS_ONE_MONTHLY, DB.PLAYERS_STATS_ONE_VS_ONE_WEEKLY);
	}
	
	@Override
    public void disable() {
        super.disable();
        String container = Bukkit.getWorldContainer().getPath();
        Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
        File newWorld = new File(container + "/../resources/maps/onevsone");
        if (newWorld.exists() && newWorld.isDirectory()) {
            FileHandler.delete(new File(container + "/lobby"));
            FileHandler.copyFolder(newWorld, new File(container + "/lobby"));
        }
    }
}
