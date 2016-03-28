package ostb.staff;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import ostb.ProPlugin;
import ostb.player.MessageHandler;
import ostb.player.account.AccountHandler;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.ChatClickHandler;
import ostb.server.DB;
import ostb.server.util.EventUtil;
import ostb.staff.ban.BanHandler;
import ostb.staff.ban.BanHandler.Violations;
import ostb.staff.ban.UnBanHandler;
import ostb.staff.mute.MuteHandler;
import ostb.staff.mute.UnMuteHandler;

public class Punishment implements Listener {
	public enum ChatViolations {
		DISRESPECT,
		RACISM,
		DEATH_COMMENTS,
		INAPPROPRIATE_COMMENTS,
		SPAMMING,
		SOCIAL_MEDIA_ADVERTISEMENT,
		SERVER_ADVERTISEMENT,
		DDOS_THREATS,
	}
	
	public class PunishmentExecuteReuslts {
		private boolean valid = false;
		private UUID uuid = null;
		
		public PunishmentExecuteReuslts(boolean valid, UUID uuid) {
			this.valid = valid;
			this.uuid = uuid;
		}
		
		public boolean isValid() {
			return this.valid;
		}
		
		public UUID getUUID() {
			return this.uuid;
		}
	}
	
	public static final String appeal = "https://promcgames.com/forum/view_forum/?fid=12";
	private String name = null;
	
	public Punishment() {
		new BanHandler();
		new UnBanHandler();
		new MuteHandler();
		new UnMuteHandler();
		new SpamPrevention();
		new ReportHandler();
		new ViolationPrevention();
		new CommandLogger();
	}
	
	public Punishment(String name) {
		this.name = name;
		EventUtil.register(this);
	}
	
	protected String getName() {
		return this.name;
	}
	
	protected String getReason(Ranks rank, String [] arguments, String reason, PunishmentExecuteReuslts result) {
		return getReason(rank, arguments, reason, result, false);
	}
	
	protected String getReason(Ranks rank, String [] arguments, String reason, PunishmentExecuteReuslts result, boolean reversingPunishment) {
		Ranks playerRank = AccountHandler.getRank(result.getUUID());
		String proof = "";
		if(!(reversingPunishment || arguments.length == 1 || arguments.length == 2 || !reason.equals(Violations.HACKING.toString()))) {
			proof = " " + ChatColor.DARK_GREEN + arguments[2];
		}
		String message = ChatColor.WHITE + playerRank.getPrefix() + arguments[0] + ChatColor.WHITE + " was " + getName();
		if(reason != null && !reason.equals("")) {
			message += ": " + ChatColor.RED + reason;
		}
		if(proof != null && !proof.equals("")) {
			message += proof;
		}
		return message;
	}
	
	protected PunishmentExecuteReuslts executePunishment(CommandSender sender, String [] arguments, boolean reversingPunishment) {
		UUID uuid = null;
		Player player = ProPlugin.getPlayer(arguments[0]);
		if(player == null) {
			uuid = AccountHandler.getUUID(arguments[0]);
		} else {
			uuid = player.getUniqueId();
		}
		if(uuid == null) {
			MessageHandler.sendMessage(sender, "&cNo player data found for " + arguments[0]);
		} else {
			if(Bukkit.getPlayer(uuid) == null && DB.PLAYERS_LOCATIONS.isUUIDSet(uuid)) {
				MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is online on another server");
				String text = DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "location");
				if(sender instanceof Player) {
					Player staff = (Player) sender;
					String server = text.split(ChatColor.RED.toString())[1];
					ChatClickHandler.sendMessageToRunCommand(staff, " &c&lCLICK TO JOIN", "Click to teleport to " + server, "/join " + server, text);
				} else {
					MessageHandler.sendMessage(sender, text);
				}
			} else {
				return new PunishmentExecuteReuslts(true, uuid);
			}
		}
		return new PunishmentExecuteReuslts(false, null);
	}
}
