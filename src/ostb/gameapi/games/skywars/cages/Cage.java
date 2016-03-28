package ostb.gameapi.games.skywars.cages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import ostb.ProPlugin;

@SuppressWarnings("deprecation")
public abstract class Cage {
	private static List<Cage> cages = null;
	private List<Block> blocks = null;
	private String playerName = null;
	private Material material = Material.GLASS;
	private byte data = 0;
	
	public Cage(Player player) {
		this.blocks = new ArrayList<Block>();
		this.playerName = player.getName();
		if(cages == null) {
			cages = new ArrayList<Cage>();
		}
		cages.add(this);
	}
	
	public static List<Cage> getCages() {
		return cages;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public void setMaterial(Material material, byte data) {
		this.material = material;
		this.data = data;
	}
	
	public void placeBlock(Location location) {
		placeBlock(location.getBlock());
	}
	
	public void placeBlock(Block block) {
		block.setType(material);
		block.setData(data);
		blocks.add(block);
	}
	
	public List<Block> getBlocks() {
		return blocks;
	}
	
	public void remove() {
		for(Block block : getBlocks()) {
			block.setType(Material.AIR);
			block.setData((byte) 0);
		}
		blocks.clear();
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public Player getPlayer() {
		return ProPlugin.getPlayer(playerName);
	}
	
	public abstract void place();
}
