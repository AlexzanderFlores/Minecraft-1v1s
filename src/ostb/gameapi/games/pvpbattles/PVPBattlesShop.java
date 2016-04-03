package ostb.gameapi.games.pvpbattles;

public class PVPBattlesShop {
	private static String name = null;
	private static String permission = null;
	
	public PVPBattlesShop() {
		name = "Shop - PVP Battles";
		permission = "kit.pvp_battles";
	}
	
	public static String getName() {
		if(name == null) {
			new PVPBattlesShop();
		}
		return name;
	}
	
	public static String getPermission() {
		return permission;
	}
}
