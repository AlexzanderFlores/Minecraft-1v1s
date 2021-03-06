package network.anticheat.detections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import network.anticheat.AntiCheatBase;
import network.customevents.TimeEvent;
import network.customevents.player.PlayerLeaveEvent;
import network.server.DB;
import network.server.tasks.AsyncDelayedTask;
import network.server.util.EventUtil;

public class AutoStealFix extends AntiCheatBase {
    private Map<String, Integer> clicks = null;
    private List<String> reported = null;

    public AutoStealFix() {
        super("Auto Steal");
        clicks = new HashMap<String, Integer>();
        reported = new ArrayList<String>();
        EventUtil.register(this);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        long ticks = event.getTicks();
        if (ticks == 2 && isEnabled()) {
            clicks.clear();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isEnabled()) {
            ClickType type = event.getClick();
            if (event.getWhoClicked() instanceof Player && (type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT)) {
                Player player = (Player) event.getWhoClicked();
                int click = 0;
                if (clicks.containsKey(player.getName())) {
                    click = clicks.get(player.getName());
                }
                clicks.put(player.getName(), ++click);
                if (click >= 5) {
                    if (!reported.contains(player.getName())) {
                        reported.add(player.getName());
                        final UUID uuid = player.getUniqueId();
                        new AsyncDelayedTask(new Runnable() {
                            @Override
                            public void run() {
                                DB.NETWORK_AUTO_STEAL_TEST.insert("'" + uuid.toString() + "'");
                            }
                        });
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        if (isEnabled()) {
            reported.remove(event.getPlayer().getName());
        }
    }
}
