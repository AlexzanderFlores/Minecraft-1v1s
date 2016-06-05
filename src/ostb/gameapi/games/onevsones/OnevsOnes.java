package ostb.gameapi.games.onevsones;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.SpectatorHandler;
import ostb.gameapi.competitive.EloHandler;
import ostb.gameapi.competitive.EloRanking;
import ostb.gameapi.competitive.StatsHandler;
import ostb.gameapi.games.onevsones.kits.Archer;
import ostb.gameapi.games.onevsones.kits.Diamond;
import ostb.gameapi.games.onevsones.kits.Gapple;
import ostb.gameapi.games.onevsones.kits.Iron;
import ostb.gameapi.games.onevsones.kits.Kohi;
import ostb.gameapi.games.onevsones.kits.NoDebuff;
import ostb.gameapi.games.onevsones.kits.SurvivalGames;
import ostb.gameapi.games.onevsones.kits.UHC;
import ostb.gameapi.games.skywars.kits.Pyro;
import ostb.player.account.AccountHandler.Ranks;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.player.scoreboard.SidebarScoreboardUtil;
import ostb.server.DB;
import ostb.server.ServerLogger;
import ostb.server.util.FileHandler;
import ostb.server.util.ImageMap;

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
        List<ItemFrame> frames = new ArrayList<ItemFrame>();
        frames.add(ImageMap.getItemFrame(world, -16, 7, 3));
        frames.add(ImageMap.getItemFrame(world, 15, 7, -2));
        new EloRanking(frames, DB.PLAYERS_ONE_VS_ONE_ELO, DB.PLAYERS_ONE_VS_ONE_RANKED);
        frames.clear();
        frames = null;
        OSTB.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
        	@Override
        	public void update(Player player) {
        		if(ServerLogger.updatePlayerCount()) {
					removeScore(8);
					removeScore(5);
				}
				int size = ProPlugin.getPlayers().size();
				setText(new String [] {
					" ",
					"&e&lPlaying",
					"&b" + size + " &7/&b " + OSTB.getMaxPlayers(),
					"  ",
					"&e&lQueue Times",
					Ranks.PLAYER.getColor() + "Default: &b5s",
					Ranks.PREMIUM.getColor() + "Premium: &b1s /buy",
					"   ",
					"&e&lPlayers Qualified",
					"&e&lFor Tournament:",
					"&b0 /tourney",
					"    ",
					"&a&lOutsideTheBlock.org",
					"&e&lServer &b&l1V1" + OSTB.getServerName().replaceAll("[^\\d.]", ""),
					"     "
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
