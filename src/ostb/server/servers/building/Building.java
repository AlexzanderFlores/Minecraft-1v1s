package ostb.server.servers.building;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.ConfigurationUtil;
import ostb.server.util.FileHandler;

public class Building extends ProPlugin {
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
		new CommandBase("test", 2, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String ign = arguments[0];
				int ratio = Integer.valueOf(arguments[1]);
				String url = "http://www.minecraft-skin-viewer.net/3d.php?layers=true&aa=true&a=0&w=330&wt=10&abg=330&abd=40&ajg=340&ajd=20&ratio=" + ratio + "&format=png&login=" + ign + "&headOnly=false&displayHairs=true&randomness=314";
				FileHandler.downloadImage(url, Bukkit.getWorldContainer().getPath() + "/plugins/test.png");
				
				return true;
			}
		};
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(OSTB.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
}
