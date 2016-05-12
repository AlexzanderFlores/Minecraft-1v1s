package ostb.server.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import ostb.OSTB;
import ostb.player.account.AccountHandler.Ranks;
import ostb.server.CommandBase;

@SuppressWarnings("deprecation")
public class ImageMap {
	private static final int MAP_WIDTH = 128;
	private static final int MAP_HEIGHT = 128;
	private static boolean registeredCommand = false;
	private static List<ImageMap> imageMaps = null;
	private static Map<ItemFrame, Integer> itemFrameMaps = null;
	private ItemFrame itemFrame = null;
	private List<ItemFrame> itemFrames = null;
	private String path = null;
	private int width = 0;
	private int height = 0;
	
	public class CustomRender extends MapRenderer {
		private Image image = null;
		private boolean load = true;
		
		public CustomRender(BufferedImage image, int x1, int y1) {
			int x2 = MAP_WIDTH;
			int y2 = MAP_HEIGHT;
			if(x1 > image.getWidth() || y1 > image.getHeight()) {
				return;
			}
			if(x1 + x2 >= image.getWidth()) {
				x2 = image.getWidth() - x1;
			}
			if(y1 + y2 >= image.getHeight()) {
				y2 = image.getHeight() - y1;
			}
			this.image = image.getSubimage(x1, y1, x2, y2);
		}
		
		@Override
		public void render(MapView view, MapCanvas canvas, Player player) {
			if(image != null && load) {
				load = false;
				canvas.drawImage(0, 0, image);
			}
		}
	}
	
	public ImageMap(ItemFrame itemFrame, String path) {
		this(itemFrame, path, 5, 3);
	}
	
	public ImageMap(ItemFrame itemFrame, String path, int width, int height) {
		if(!registeredCommand) {
			registeredCommand = true;
			new CommandBase("reloadImageMaps", true) {
				@Override
				public boolean execute(CommandSender sender, String [] arguments) {
					for(ImageMap map : imageMaps) {
						map.execute();
					}
					return true;
				}
			}.setRequiredRank(Ranks.OWNER);
		}
		if(itemFrameMaps == null) {
			itemFrameMaps = new HashMap<ItemFrame, Integer>();
		}
		itemFrames = new ArrayList<ItemFrame>();
		this.itemFrame = itemFrame;
		this.path = path;
		this.width = width;
		this.height = height;
		execute();
		if(imageMaps == null) {
			imageMaps = new ArrayList<ImageMap>();
		}
		Iterator<ImageMap> iterator = imageMaps.iterator();
		while(iterator.hasNext()) {
			ImageMap map = iterator.next();
			if(map.getItemFrames().get(0).equals(itemFrame)) {
				iterator.remove();
			}
		}
		imageMaps.add(this);
	}
	
	public void execute() {
		Bukkit.getLogger().info("Loading image from \"" + path + "\"");
		File file = new File(path);
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch(IOException e) {
			e.printStackTrace();
		}
		Location location = itemFrame.getLocation();
		BlockFace face = itemFrame.getFacing();
		int x1 = location.getBlockX();
		int y1 = location.getBlockY();
		int z1 = location.getBlockZ();
		for(int a = 0; a < height; ++a, --y1) {
			for(int b = 0; b < width; ++b) {
				int x = b * MAP_WIDTH;
				int y = a * MAP_HEIGHT;
				ItemFrame frame = getItemFrame(itemFrame.getWorld(), x1, y1, z1);
				int id = itemFrameMaps.containsKey(frame) ? itemFrameMaps.get(frame) : -1;
				MapView mapView = null;
				if(id == -1) {
					if(OSTB.getMiniGame() == null) {
						mapView = OSTB.getInstance().getServer().createMap(itemFrame.getWorld());
					} else {
						do {
							mapView = OSTB.getInstance().getServer().createMap(itemFrame.getWorld());
						} while(mapView.getId() <= 36);
					}
					itemFrameMaps.put(frame, (int) mapView.getId());
					if(!itemFrames.contains(frame)) {
						itemFrames.add(frame);
					}
				} else {
					mapView = Bukkit.getMap((short) id);
				}
				for(MapRenderer renderer : mapView.getRenderers()) {
					mapView.removeRenderer(renderer);
				}
				mapView.addRenderer(new CustomRender(image, x, y));
				ItemStack map = new ItemStack(Material.MAP);
				map.setDurability(mapView.getId());
				frame.setItem(map);
				switch(face) {
					case NORTH:
						--x1;
						break;
					case SOUTH:
						++x1;
						break;
					case EAST:
						--z1;
						break;
					case WEST:
						++z1;
						break;
					default:
						return;
				}
			}
			switch(face) {
				case NORTH:
				case SOUTH:
					x1 = location.getBlockX();
					break;
				case EAST:
				case WEST:
					z1 = location.getBlockZ();
					break;
				default:
					return;
			}
		}
	}
	
	public List<ItemFrame> getItemFrames() {
		return itemFrames;
	}
	
	public static ItemFrame getItemFrame(World world, int x1, int y1, int z1) {
		for(Entity entity : world.getEntities()) {
			if(entity instanceof ItemFrame) {
				Location loc = entity.getLocation();
				if(loc.getBlockX() == x1 && loc.getBlockY() == y1 && loc.getBlockZ() == z1) {
					ItemFrame itemFrame = (ItemFrame) entity;
					return itemFrame;
				}
			}
		}
		return null;
	}
}
