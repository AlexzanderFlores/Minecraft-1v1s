package network.gameapi.games.onevsones;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import network.Network;
import network.ProPlugin;
import network.gameapi.SpectatorHandler;
import network.gameapi.competitive.EloHandler;
import network.gameapi.competitive.EloRanking;
import network.gameapi.competitive.StatDisplayer;
import network.gameapi.competitive.StatsHandler;
import network.gameapi.competitive.EloRanking.EloRank;
import network.gameapi.games.onevsones.kits.Archer;
import network.gameapi.games.onevsones.kits.BuildUHC;
import network.gameapi.games.onevsones.kits.NoDebuff;
import network.gameapi.games.onevsones.kits.Skywars;
import network.gameapi.games.onevsones.kits.SpeedUHC;
import network.gameapi.games.onevsones.kits.SurvivalGames;
import network.gameapi.uhc.GoldenHead;
import network.player.TeamScoreboardHandler;
import network.player.account.AccountHandler.Ranks;
import network.player.scoreboard.BelowNameHealthScoreboardUtil;
import network.player.scoreboard.SidebarScoreboardUtil;
import network.server.CPSDetector;
import network.server.DB;
import network.server.ServerLogger;
import network.server.tasks.DelayedTask;
import network.server.util.FileHandler;
import network.server.util.StringUtil;

public class OnevsOnes extends ProPlugin {
	private static String oldPlayerCount = null;
	
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
        setAllowItemSpawning(true);
        setAutoVanishStaff(true);
        final World world = Bukkit.getWorlds().get(0);
        Location target = new Location(world, 0.5, 7, -30.5);
		new ServerLogger();
		new StatsHandler(DB.PLAYERS_STATS_ONE_VS_ONE, DB.PLAYERS_STATS_ONE_VS_ONE_MONTHLY, DB.PLAYERS_STATS_ONE_VS_ONE_WEEKLY);
		new LobbyHandler();
        new QueueHandler();
        new BattleHandler();
        new MapProvider(world);
        new SpectatorHandler().createNPC(new Location(world, 13.5, 8, -22.5), target);
        new PrivateBattleHandler();
        new HotbarEditor();
        new EloHandler(DB.PLAYERS_ONE_VS_ONE_ELO, 1400);
        new ServerLogger();
        //Arrays.asList(ImageMap.getItemFrame(world, -16, 10, -34))
        new EloRanking(new ArrayList<ItemFrame>(), DB.PLAYERS_ONE_VS_ONE_ELO, DB.PLAYERS_ONE_VS_ONE_RANKED);
        new CPSDetector(new Location(world, -24, 8, -23), target);
        new MultiplayerNPCs();
        new GoldenHead();
        new RankedHandler();
        new DelayedTask(new Runnable() {
			@Override
			public void run() {
				List<Location> locations = Arrays.asList(
					new Location(world, -5.5, 13.5, -47),
					new Location(world, 0.5, 13.5, -49),
					new Location(world, 6.5, 13.5, -47)
				);
				new StatDisplayer(locations);
				List<Hologram> holograms = new ArrayList<Hologram>();
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 12.5, -38.5), StringUtil.color("&e&nElo ranks are based off of")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 12, -38.5), StringUtil.color("&e&nyour percentile of elo value")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 11, -38.5), StringUtil.color(EloRank.BRONZE.getPrefix() + " &aTop " + EloRank.BRONZE.getDisplayPercentage() + "%")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 10.5, -38.5), StringUtil.color(EloRank.SILVER.getPrefix() + " &aTop " + EloRank.SILVER.getDisplayPercentage() + "%")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 10, -38.5), StringUtil.color(EloRank.GOLD.getPrefix() + " &aTop " + EloRank.GOLD.getDisplayPercentage() + "%")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 9.5, -38.5), StringUtil.color(EloRank.DIAMOND.getPrefix() + " &aTop " + EloRank.DIAMOND.getDisplayPercentage() + "%")));
				holograms.add(HologramAPI.createHologram(new Location(world, -14.5, 9, -38.5), StringUtil.color(EloRank.PLATINUM.getPrefix() + " &aTop " + EloRank.PLATINUM.getDisplayPercentage() + "%")));
				for(Hologram hologram : holograms) {
					hologram.spawn();
				}
			}
		}, 20);
        oldPlayerCount = "";
        Network.setSidebar(new SidebarScoreboardUtil(" &a&l" + getDisplayName() + " ") {
        	@Override
        	public void update(Player player) {
				int size = ProPlugin.getPlayers().size();
        		String playerCount = "&b" + size + " &7/&b " + Network.getMaxPlayers();
        		if(!oldPlayerCount.equals(playerCount)) {
        			oldPlayerCount = playerCount;
        			removeScore(11);
        		}
				setText(new String [] {
					" ",
					"&e&lPlaying",
					playerCount,
					"  ",
					"&e&lQueue Times",
					Ranks.PLAYER.getColor() + "Default: &b5s",
					Ranks.VIP.getColor() + "VIP: &b1s /buy",
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
        new BelowNameHealthScoreboardUtil();
        new TeamScoreboardHandler();
        // Kits
        new SurvivalGames();
        new Archer();
        new BuildUHC();
        new NoDebuff();
        new Skywars();
        new SpeedUHC();
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
