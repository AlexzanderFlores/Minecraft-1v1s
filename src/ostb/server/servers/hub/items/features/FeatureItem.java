package ostb.server.servers.hub.items.features;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ostb.gameapi.shops.crates.HardcoreEliminationCrate;
import ostb.gameapi.shops.crates.SkyWarsCrate;
import ostb.server.servers.hub.crate.Beacon;
import ostb.server.servers.hub.crate.KeyFragments;
import ostb.server.servers.hub.items.Features.Rarity;
import ostb.server.servers.hub.items.features.Armor.PlayerArmor;
import ostb.server.servers.hub.items.features.blocks.SpinningBlocks.SpinBlock;
import ostb.server.servers.hub.items.features.particles.ArrowTrails.ArrowTrailParticleTypes;
import ostb.server.servers.hub.items.features.particles.HaloParticles.HaloParticleTypes;
import ostb.server.servers.hub.items.features.wineffects.WinEffects.WinEffect;

public class FeatureItem {
	private static List<FeatureItem> items = null;
	private String name = null;
	private ItemStack itemStack = null;
	private Rarity rarity = null;
	
	public FeatureItem(String name, ItemStack itemStack, Rarity rarity) {
		this.name = name;
		this.itemStack = itemStack;
		this.rarity = rarity;
		if(items == null) {
			items = new ArrayList<FeatureItem>();
		}
		items.add(this);
	}
	
	public static List<FeatureItem> getItems() {
		return items;
	}
	
	public static FeatureItem getItem(ItemStack itemStack) {
		for(FeatureItem featureItem : getItems()) {
			if(featureItem.getItemStack().equals(itemStack)) {
				return featureItem;
			}
		}
		return null;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ItemStack getItemStack() {
		return this.itemStack;
	}
	
	public Rarity getRarity() {
		return this.rarity;
	}
	
	public void give(Player player) {
		if(getName().equals(Beacon.getKeyFragmentName())) {
			KeyFragments.give(player, 1);
			return;
		}
		if(getName().equals(Beacon.getVotingKeyx3())) {
			Beacon.giveKey(player.getUniqueId(), 3, "voting");
			return;
		}
		if(getName().equals(Beacon.getSkyWarsCrateKey())) {
			SkyWarsCrate.giveKey(player.getUniqueId(), 1);
			return;
		}
		if(getName().equals(Beacon.getHardcoreEliminationCrateKey())) {
			HardcoreEliminationCrate.giveKey(player.getUniqueId(), 1);
			return;
		}
		for(HaloParticleTypes halo : HaloParticleTypes.values()) {
			if(halo.getName().equals(getName())) {
				halo.give(player);
				return;
			}
		}
		for(PlayerArmor armor : PlayerArmor.values()) {
			if(armor.getName().equals(getName())) {
				armor.give(player);
				return;
			}
		}
		for(ArrowTrailParticleTypes trail : ArrowTrailParticleTypes.values()) {
			if(trail.getName().equals(getName())) {
				trail.give(player);
				return;
			}
		}
		for(WinEffect effect : WinEffect.values()) {
			if(effect.getName().equals(getName())) {
				effect.give(player);
				return;
			}
		}
		for(SpinBlock block : SpinBlock.values()) {
			if(block.getName().equals(getName())) {
				block.give(player);
				return;
			}
		}
	}
}
