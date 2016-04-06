package ostb.server.servers.pregenerator;

import org.bukkit.Bukkit;

import ostb.ProPlugin;

public class Pregenerator extends ProPlugin {
	public Pregenerator() {
		super("Pregenerator");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill cancel");
		new Events();
	}
}
