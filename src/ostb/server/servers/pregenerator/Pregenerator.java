package ostb.server.servers.pregenerator;

import ostb.ProPlugin;
import ostb.server.BiomeSwap;

public class Pregenerator extends ProPlugin {
	public Pregenerator() {
		super("Pregenerator");
		BiomeSwap.setUpUHC();
		new Events();
	}
}
