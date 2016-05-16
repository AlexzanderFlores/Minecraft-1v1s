package ostb.gameapi.games.pvpbattles;

public class CTF extends PVPBattles {
	public CTF() {
		super("Capture the Flag");
		new ostb.gameapi.modes.CTF(2);
	}
}
