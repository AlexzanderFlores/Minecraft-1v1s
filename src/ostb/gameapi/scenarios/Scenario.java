package ostb.gameapi.scenarios;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import ostb.server.util.EventUtil;
import ostb.server.util.ItemCreator;

public abstract class Scenario implements Listener {
    private String name = null;
    private ItemStack item = null;
    private boolean enabled = false;
    private String info = null;

    public Scenario(String name, Material material) {
        this(name, new ItemStack(material));
    }

    public Scenario(String name, ItemStack item) {
        this.name = name;
        this.item = new ItemCreator(item).setName("&b" + name).getItemStack();
    }

    public String getName() {
        return this.name;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void enable(boolean fromEvent) {
        disable(fromEvent);
        EventUtil.register(this);
        enabled = true;
        if(!fromEvent) {
            Bukkit.getPluginManager().callEvent(new ScenarioStateChangeEvent(this, true));
        }
    }

    public void disable(boolean fromEvent) {
        HandlerList.unregisterAll(this);
        enabled = false;
        if(!fromEvent) {
            Bukkit.getPluginManager().callEvent(new ScenarioStateChangeEvent(this, false));
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
