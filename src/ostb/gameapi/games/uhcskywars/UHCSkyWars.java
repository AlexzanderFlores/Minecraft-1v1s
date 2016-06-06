package ostb.gameapi.games.uhcskywars;

import ostb.gameapi.games.skywars.SkyWars;
import ostb.gameapi.uhc.GoldenHeadUtil;
import ostb.gameapi.uhc.SkullPikeUtil;

public class UHCSkyWars extends SkyWars {
	public UHCSkyWars() {
		super("UHC Sky Wars");
		new Events();
		new SkullPikeUtil();
		new GoldenHeadUtil();
	}
}
