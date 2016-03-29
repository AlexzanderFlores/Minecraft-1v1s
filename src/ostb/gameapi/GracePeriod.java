package ostb.gameapi;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import ostb.OSTB;
import ostb.ProPlugin;
import ostb.customevents.game.GracePeriodEndEvent;
import ostb.customevents.timed.OneSecondTaskEvent;
import ostb.player.TitleDisplayer;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;
import ostb.server.util.CountDownUtil;
import ostb.server.util.EffectUtil;
import ostb.server.util.EventUtil;

public class GracePeriod extends CountDownUtil implements Listener {
	private static GracePeriod instance = null;
	private static boolean isRunning = false;
	
	public GracePeriod(int seconds) {
		super(seconds);
		instance = this;
		isRunning = true;
		OSTB.getProPlugin().setAllowEntityDamage(false);
		OSTB.getProPlugin().setAllowEntityDamageByEntities(false);
		EventUtil.register(instance);
		new CommandBase("grace", 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				try {
					setCounter(Integer.valueOf(arguments[0]));
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(getCounter() <= 0) {
			isRunning = false;
			HandlerList.unregisterAll(instance);
			OSTB.getProPlugin().setAllowEntityDamage(true);
			OSTB.getProPlugin().setAllowEntityDamageByEntities(true);
			OSTB.getProPlugin().setAllowBowShooting(true);
			for(Player player : ProPlugin.getPlayers()) {
				new TitleDisplayer(player, "&cPVP Enabled").setFadeIn(5).setStay(30).setFadeOut(5).display();
			}
			EffectUtil.playSound(Sound.ENDERDRAGON_GROWL);
			Bukkit.getPluginManager().callEvent(new GracePeriodEndEvent());
		} else {
			if(getCounter() <= 3) {
				for(Player player : Bukkit.getOnlinePlayers()) {
					new TitleDisplayer(player, "&cPVP Enabled in", "&e0:0" + getCounter()).setFadeIn(5).setStay(15).setFadeOut(5).display();
				}
			}
		}
		decrementCounter();
	}
	
	public static boolean isRunning() {
		return isRunning;
	}
	
	public static String getGraceCounterString() {
		return instance.getCounterAsString();
	}
	
	public static int getGraceCounter() {
		return instance.getCounter();
	}
}
