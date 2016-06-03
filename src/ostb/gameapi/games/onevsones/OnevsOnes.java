package ostb.gameapi.games.onevsones;

import java.io.File;

import org.bukkit.Bukkit;

import ostb.ProPlugin;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.StatsHandler;
import ostb.gameapi.games.onevsones.kits.Archer;
import ostb.gameapi.games.onevsones.kits.Chain;
import ostb.gameapi.games.onevsones.kits.Diamond;
import ostb.gameapi.games.onevsones.kits.Gapple;
import ostb.gameapi.games.onevsones.kits.Gold;
import ostb.gameapi.games.onevsones.kits.Iron;
import ostb.gameapi.games.onevsones.kits.Kohi;
import ostb.gameapi.games.onevsones.kits.Leather;
import ostb.gameapi.games.onevsones.kits.NoDebuff;
import ostb.gameapi.games.onevsones.kits.SurvivalGames;
import ostb.gameapi.games.onevsones.kits.Swordsman;
import ostb.gameapi.games.onevsones.kits.UHC;
import ostb.gameapi.games.skywars.kits.Pyro;
import ostb.player.TeamScoreboardHandler;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
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
		new LobbyHandler();
        new QueueHandler();
        new BattleHandler();
        new MapProvider(Bukkit.getWorlds().get(0));
        new SpectatorHandler();
        new TeamScoreboardHandler();
        new BelowNameHealthScoreboardUtil();
        new PrivateBattleHandler();
        new HotbarEditor();
        new VersusElo();
        new ServerLogger();
        // Kits
        new Leather();
        new Gold();
        new Chain();
        new Iron();
        new Diamond();
        new SurvivalGames();
        new Archer();
        new UHC();
        new Swordsman();
        new Pyro();
        new Gapple();
        new Kohi();
        new NoDebuff();
	}
	
	@Override
    public void disable() {
        super.disable();
        String container = Bukkit.getWorldContainer().getPath();
        Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
        File newWorld = new File(container + "/../resources/maps/onevsone");
        if(newWorld.exists() && newWorld.isDirectory()) {
            FileHandler.delete(new File(container + "/lobby"));
            FileHandler.copyFolder(newWorld, new File(container + "/lobby"));
        }
    }
}
