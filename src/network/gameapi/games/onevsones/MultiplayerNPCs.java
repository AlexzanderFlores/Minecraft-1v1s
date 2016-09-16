package network.gameapi.games.onevsones;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import network.player.MessageHandler;
import npc.NPCEntity;

public class MultiplayerNPCs {
	public MultiplayerNPCs() {
		World world = Bukkit.getWorlds().get(0);
		Location target = new Location(world, 0.5, 7, -30.5);
		new NPCEntity(EntityType.ZOMBIE, "&e&n1v1 Queues", new Location(world, 13.5, 8, -22.5), target) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "&cUse hotbar item for now, NPC coming soon");
			}
		};
		new NPCEntity(EntityType.ZOMBIE, "&e&n2v2 Queues", new Location(world, 15.5, 8, -25.5), target) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "&cComing soon");
			}
		};
		new NPCEntity(EntityType.ZOMBIE, "&e&n3v3 Queues", new Location(world, 16.5, 8, -29.5), target) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "&cComing soon");
			}
		};
		new NPCEntity(EntityType.ZOMBIE, "&e&n4v4 Queues", new Location(world, 15.5, 8, -33.5), target) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "&cComing soon");
			}
		};
		new NPCEntity(EntityType.ZOMBIE, "&e&n5v5 Queues", new Location(world, 13.5, 8, -36.5), target) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "&cComing soon");
			}
		};
	}
}
