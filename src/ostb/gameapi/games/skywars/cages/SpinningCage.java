package ostb.gameapi.games.skywars.cages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ostb.customevents.game.GameStartEvent;
import ostb.server.servers.hub.items.features.blocks.SpinBlockEntity;
import ostb.server.util.EventUtil;

public class SpinningCage extends Cage implements Listener {
	private SpinBlockEntity spinningBlock = null;
	private Material material = null;
	private int data = 0;
	
	public SpinningCage(Player player, Material material, int data) {
		super(player);
		this.material = material;
		this.data = data;
		place();
		EventUtil.register(this);
	}
	
	@Override
	public void place() {
		Player player = getPlayer();
		if(player != null) {
			Location location = player.getLocation();
			placeBlock(location.clone().add(0, -1, 0));
			for(int [] a : new int [] [] {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}) {
				int x = a[0];
				int z = a[1];
				for(int y = 0; y < 3; ++y) {
					placeBlock(location.clone().add(x, y, z));
				}
			}
			player.teleport(getBlocks().get(0).getLocation().clone().add(0.5, 1, 0.5));
		}
		spinningBlock = new SpinBlockEntity(material, data, getBlocks().get(0).getLocation().clone().add(0, 2, 0));
		spinningBlock.setRadius(3.0d);
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		spinningBlock.remove();
		spinningBlock = null;
		material = null;
		data = 0;
		GameStartEvent.getHandlerList().unregister(this);
	}
}
