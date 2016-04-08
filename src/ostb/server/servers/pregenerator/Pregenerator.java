package ostb.server.servers.pregenerator;

import org.bukkit.Bukkit;

import ostb.ProPlugin;
import ostb.server.BiomeSwap;

public class Pregenerator extends ProPlugin {
	public Pregenerator() {
		super("Pregenerator");
		BiomeSwap.setUpUHC();
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill cancel");
		new Events();
	}
}
