package ostb.player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

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
		this.name = player.getName();
		setTitle(title);
		if(subTitle != null) {
			setSubTitle(subTitle);
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
		if(player != null) {
			CraftPlayer craftPlayer = (CraftPlayer) player;
			PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.TITLE, this.title);
			PacketPlayOutTitle subTitle = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, this.subTitle);
			PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
			craftPlayer.getHandle().playerConnection.sendPacket(title);
			craftPlayer.getHandle().playerConnection.sendPacket(subTitle);
			craftPlayer.getHandle().playerConnection.sendPacket(length);
			title = null;
			subTitle = null;
			name = null;
		}
	}
}
