package network.gameapi.games.onevsones;

import java.io.File;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import network.Network;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.gameapi.competitive.EloHandler;
import network.gameapi.competitive.EloRanking;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.games.onevsones.kits.Archer;
import network.gameapi.games.onevsones.kits.Diamond;
import network.gameapi.games.onevsones.kits.Gapple;
import network.gameapi.games.onevsones.kits.Iron;
import network.gameapi.games.onevsones.kits.Kohi;
import network.gameapi.games.onevsones.kits.NoDebuff;
import network.gameapi.games.onevsones.kits.SurvivalGames;
import network.gameapi.games.onevsones.kits.UHC;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.CPSDetector;
import network.server.DB;
import network.server.ServerLogger;
import network.server.util.FileHandler;
import network.server.util.ImageMap;

public class OnevsOnes extends ProPlugin {
	public OnevsOnes() {
		super("1v1");
		setAllowEntityDamage(true);
        setAllowEntityDamageByEntities(true);
        setAllowPlayerInteraction(true);
        setAllowBowShooting(true);
        setAllowInventoryClicking(true);
        setFlintAndSteelUses(2);
        setAllowEntityCombusting(true);
        setAllowInventoryClicking(true);
        setAutoVanishStaff(true);
        World world = Bukkit.getWorlds().get(0);
        new SpectatorHandler();
		new ServerLogger();
		new StatsHandler(DB.PLAYERS_STATS_ONE_VS_ONE, DB.PLAYERS_STATS_ONE_VS_ONE_MONTHLY, DB.PLAYERS_STATS_ONE_VS_ONE_WEEKLY);
		new LobbyHandler();
        new QueueHandler();
        new BattleHandler();
        new MapProvider(world);
        new SpectatorHandler();
        new BelowNameHealthScoreboardUtil();
        new PrivateBattleHandler();
        new HotbarEditor();
        new EloHandler(DB.PLAYERS_ONE_VS_ONE_ELO, 1400);
        new ServerLogger();
        new EloRanking(Arrays.asList(ImageMap.getItemFrame(world, -16, 10, -34)), DB.PLAYERS_ONE_VS_ONE_ELO, DB.PLAYERS_ONE_VS_ONE_RANKED);
        new CPSDetector(new Location(world, -18.5, 8, -23.5), new Location(world, 0.5, 7, -30.5));
        new MultiplayerNPCs();
        Network.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
        	@Override
        	public void update(Player player) {
        		if(ServerLogger.updatePlayerCount()) {
					removeScore(11);
				}
				int size = ProPlugin.getPlayers().size();
				setText(new String [] {
					" ",
					"&e&lPlaying",
					"&b" + size + " &7/&b " + Network.getMaxPlayers(),
					"  ",
					"&e&lQueue Times",
					Ranks.PLAYER.getColor() + "Default: &b5s",
					Ranks.PREMIUM.getColor() + "VIP: &b1s /buy",
					"   ",
					"&e&lServer",
					"&b&l1V1S" + Network.getServerName().replaceAll("[^\\d.]", ""),
					"    ",
					"&a&l1v1s.org",
					"     ",
				});
				super.update(player);
        	}
        });
        // Kits
        new Iron();
        new Diamond();
        new SurvivalGames();
        new Archer();
        new UHC();
        new Gapple();
        new Kohi();
        new NoDebuff();
	}
	
	@Override
    public void disable() {
        super.disable();
        String container = "/root/" + Network.getServerName().toLowerCase() + "/";
        Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
        File newWorld = new File("/root/resources/maps/1v1s");
        if(newWorld.exists() && newWorld.isDirectory()) {
            FileHandler.delete(new File(container + "/1v1s"));
            FileHandler.copyFolder(newWorld, new File(container + "/1v1s"));
        }
    }
}
