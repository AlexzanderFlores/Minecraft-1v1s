package ostb.gameapi.games.skywars;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.OSTB;
import ostb.gameapi.MiniGame;
import ostb.gameapi.games.skywars.cages.BigCage;
import ostb.gameapi.games.skywars.cages.ColoredBigCage;
import ostb.gameapi.games.skywars.cages.ColoredSmallCage;
import ostb.gameapi.games.skywars.cages.SmallCage;
import ostb.player.scoreboard.BelowNameHealthScoreboardUtil;
import ostb.server.CommandBase;

public class SkyWars extends MiniGame {
	public SkyWars() {
		super("Sky Wars");
		setVotingCounter(45);
		setStartingCounter(10);
		setFlintAndSteelUses(4);
		new BelowNameHealthScoreboardUtil();
		new Events();
		new CommandBase("test", 1, 5, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				Player player = (Player) sender;
				if(arguments[0].equalsIgnoreCase("small")) {
					new SmallCage(player);
				} else if(arguments[0].equalsIgnoreCase("big")) {
					new BigCage(player);
				} else if(arguments[0].equalsIgnoreCase("coloredSmall")) {
					Material material = null;
					byte data = 0;
					if(arguments.length == 2) {
						material = Material.valueOf(arguments[1].toUpperCase());
					} else if(arguments.length == 3) {
						material = Material.valueOf(arguments[1].toUpperCase());
						data = Byte.valueOf(arguments[2]);
					} else {
						return false;
					}
					new ColoredSmallCage(player, material, data);
				} else if(arguments[0].equalsIgnoreCase("coloredBig")) {
					Material material = null;
					byte data = 0;
					if(arguments.length == 2) {
						material = Material.valueOf(arguments[1].toUpperCase());
					} else if(arguments.length == 3) {
						material = Material.valueOf(arguments[1].toUpperCase());
						data = Byte.valueOf(arguments[2]);
					} else {
						return false;
					}
					new ColoredBigCage(player, material, data);
				} else if(arguments[0].equalsIgnoreCase("setMax")) {
					OSTB.setMaxPlayers(Integer.valueOf(arguments[1]));
				} else {
					return false;
				}
				return true;
			}
		};
	}
}
