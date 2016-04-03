package ostb.server.servers.building;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.gameapi.KitSelection;
import ostb.gameapi.games.skywars.kits.Archer;
import ostb.gameapi.games.skywars.kits.Bomber;
import ostb.gameapi.games.skywars.kits.Builder;
import ostb.gameapi.games.skywars.kits.CowSlayer;
import ostb.gameapi.games.skywars.kits.Enchanter;
import ostb.gameapi.games.skywars.kits.Enderman;
import ostb.gameapi.games.skywars.kits.Fisherman;
import ostb.gameapi.games.skywars.kits.Looter;
import ostb.gameapi.games.skywars.kits.Medic;
import ostb.gameapi.games.skywars.kits.Miner;
import ostb.gameapi.games.skywars.kits.Ninja;
import ostb.gameapi.games.skywars.kits.Pyro;
import ostb.gameapi.games.skywars.kits.Spiderman;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;

public class Building extends ProPlugin {
	private KitSelection selection = null;
	
	public Building() {
		super("Building");
		addGroup("24/7");
		new Events();
		removeFlags();
		setAllowLeavesDecay(false);
		setAllowDefaultMobSpawning(false);
		new CommandBase("setGameSpawn", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/spawns.yml");
				int index = -1;
				if(arguments.length == 0) {
					index = config.getConfig().getKeys(false).size() + 1;
				} else if(arguments.length == 1) {
					try {
						index = Integer.valueOf(arguments[0]);
					} catch(NumberFormatException e) {
						return false;
					}
				}
				String loc = (location.getBlockX() + ".5,") + (location.getBlockY() + 1) + "," + (location.getBlockZ() + ".5,");
				config.getConfig().set(index + "", loc);
				config.save();
				MessageHandler.sendMessage(player, "Set spawn " + index);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("test", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(selection == null) {
					selection = new KitSelection(player);
				} else {
					selection.delete();
					selection = null;
					return true;
				}
				selection.update();
				return true;
			}
		};
		new Archer();
		new Builder();
		new Looter();
		new Enchanter();
		new Bomber();
		new Ninja();
		new Medic();
		new CowSlayer();
		new Enderman();
		new Fisherman();
		new Spiderman();
		new Pyro();
		new Miner();
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
}
