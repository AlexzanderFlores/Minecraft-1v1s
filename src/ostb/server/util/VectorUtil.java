package ostb.server.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtil {
	public static Vector getDirectionVector(Location init, Location dest, double speed) {
		double x = dest.getX() - init.getX();
		double y = dest.getY() - init.getY();
		double z = dest.getZ() - init.getZ();
		double min = 999999999;
		if(x < min) {
			min = x;
		}
		if(y < min) {
			min = y;
		}
		if(z < min) {
			min = z;
		}
		min = Math.abs(min);
		x /= min;
		y /= min;
		z /= min;
		x *= speed;
		y *= speed;
		z *= speed;
		return new Vector(x / 2.0d, y + 0.5d, z / 2.0d);
	}
}
