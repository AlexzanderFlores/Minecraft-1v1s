package ostb.player;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector.PacketTitle;
import org.spigotmc.ProtocolInjector.PacketTitle.Action;

import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import ostb.ProPlugin;
import ostb.server.util.StringUtil;
import ostb.server.util.TextConverter;

public class TitleDisplayer {
    private String name = null;
    private IChatBaseComponent title = null;
    private IChatBaseComponent subTitle = null;
    private int fadeIn = 20;
    private int stay = 20;
    private int fadeOut = 20;

    public TitleDisplayer(Player player, String title) {
        this(player, title, null);
    }

    public TitleDisplayer(Player player, String title, String subTitle) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        if (craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
            this.name = player.getName();
            setTitle(title);
            if (subTitle != null) {
                setSubTitle(subTitle);
            }
        }
    }

    public TitleDisplayer setTitle(String title) {
        this.title = ChatSerializer.a(TextConverter.convert(StringUtil.color(title)));
        return this;
    }

    public TitleDisplayer setSubTitle(String subTitle) {
        this.subTitle = ChatSerializer.a(TextConverter.convert(StringUtil.color(subTitle)));
        return this;
    }

    public TitleDisplayer setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public TitleDisplayer setStay(int stay) {
        this.stay = stay;
        return this;
    }

    public TitleDisplayer setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public void display() {
        Player player = ProPlugin.getPlayer(name);
        if (player != null) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            craftPlayer.getHandle().playerConnection.sendPacket(new PacketTitle(Action.TIMES, fadeIn, stay, fadeOut));
            if (title != null) {
                craftPlayer.getHandle().playerConnection.sendPacket(new PacketTitle(Action.TITLE, title));
                title = null;
            }
            if (subTitle != null) {
                craftPlayer.getHandle().playerConnection.sendPacket(new PacketTitle(Action.SUBTITLE, subTitle));
                subTitle = null;
            }
            name = null;
        }
    }
}
