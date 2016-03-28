package ostb.gameapi.scenarios;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import ostb.server.util.EventUtil;

public abstract class Scenario implements Listener {
	private String name = null;
	private boolean enabled = false;
	
	public String getName() {
		return this.name;
	}
	
	public void enable(boolean fromEvent) {
		disable(fromEvent);
		EventUtil.register(this);
		enabled = true;
		if(!fromEvent) {
			//Bukkit.getPluginManager().callEvent(new ScenarioStateChangeEvent(this, true));
		}
	}
	
	public void disable(boolean fromEvent) {
		HandlerList.unregisterAll(this);
		enabled = false;
		if(!fromEvent) {
			//Bukkit.getPluginManager().callEvent(new ScenarioStateChangeEvent(this, false));
		}
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}
