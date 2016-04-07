package ostb.gameapi.games.hardcoreelimination;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import npc.ostb.NPCEntity;
import ostb.player.account.AccountHandler.Ranks;

public class Modifier {
	private static List<Modifier> modifiers = null;
	private String name = null;
	private List<String> votedBy = null;
	private int votes = 0;
	
	public Modifier(String name, Location location) {
		this.name = name;
		votedBy = new ArrayList<String>();
		new NPCEntity(EntityType.ZOMBIE, "&e&n" + name, location) {
			@Override
			public void onInteract(final Player player) {
				
			}
		};
		if(modifiers == null) {
			modifiers = new ArrayList<Modifier>();
		}
		modifiers.add(this);
	}
	
	public static List<Modifier> getModifiers() {
		return modifiers;
	}
	
	public String getName() {
		return name;
	}
	
	public int getVotes() {
		return votes;
	}
	
	public void addVotes(Player player) {
		for(Modifier modifier : modifiers) {
			modifier.removeVotes(player);
		}
		votes += Ranks.getVotes(player);
		votedBy.add(player.getName());
	}
	
	public void removeVotes(Player player) {
		if(votedBy.contains(player.getName())) {
			votes -= Ranks.getVotes(player);
			votedBy.remove(player.getName());
		}
	}
}
