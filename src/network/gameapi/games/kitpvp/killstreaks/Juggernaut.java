package network.gameapi.games.kitpvp.killstreaks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import network.player.MessageHandler;
import network.player.account.AccountHandler;
import network.server.util.ItemCreator;

public class Juggernaut extends Killstreak {
	private static Juggernaut instance = null;
	
	public Juggernaut() {
		super(new ItemCreator(Material.DIAMOND_CHESTPLATE).setName("Juggernaut").getItemStack());
		instance = this;
	}
	
	public static Juggernaut getInstance() {
		if(instance == null) {
			new Juggernaut();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 40, 0));
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet resistance I for 30 seconds");
	}
}