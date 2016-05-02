package ostb.gameapi.games.pvpbattles.ctf;

import ostb.gameapi.games.pvpbattles.PVPBattles;

public class CTF extends PVPBattles {
	public CTF() {
		super("Capture the Flag");
		new ostb.gameapi.modes.CTF(3);
	}
}
