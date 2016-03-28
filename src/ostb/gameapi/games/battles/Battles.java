package ostb.gameapi.games.battles;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ostb.gameapi.MiniGame;
import ostb.server.CommandBase;

public class Battles extends MiniGame {
	public Battles(String name) {
		super(name);
		setRequiredPlayers(8);
		new Events();
		new CommandBase("test", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				new Armory(player.getLocation());
				return true;
			}
		};
	}
}
