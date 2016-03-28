package ostb.server.tasks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import ostb.OSTB;

public class AsyncDelayedTask implements Listener {
	public AsyncDelayedTask(Runnable runnable) {
		this(runnable, 1);
	}
	
	public AsyncDelayedTask(Runnable runnable, long delay) {
		OSTB instance = OSTB.getInstance();
		if(instance.isEnabled()) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(instance, runnable, delay);
		} else {
			runnable.run();
		}
	}
}
