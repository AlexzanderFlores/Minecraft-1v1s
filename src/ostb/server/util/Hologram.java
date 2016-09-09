package ostb.server.util;

import org.bukkit.Location;

public class Hologram {
	private String text = null;
	private Location location = null;
	
	public Hologram(String text, Location location) {
		this.text = text;
		this.location = location;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
}
