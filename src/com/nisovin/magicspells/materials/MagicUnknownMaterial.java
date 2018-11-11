package com.nisovin.magicspells.materials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Objects;

public class MagicUnknownMaterial extends MagicMaterial {

	Material type;
	short data;
	
	public MagicUnknownMaterial(String type, short data) {
		this.type = Material.getMaterial(type);
		this.data = data;
	}
	
	@Override
	public Material getMaterial() {
		return Material.getMaterial(this.type.toString());
	}
	
	@Override
	public MaterialData getMaterialData() {
		if (this.data == (byte)this.data) return new MaterialData(this.type, (byte)this.data);
		return new MaterialData(this.type);
	}
	
	@Override
	public void setBlock(Block block, boolean applyPhysics) {
		if (this.data < 16) block.setType(this.type, applyPhysics);
	}
	
	@Override
	public FallingBlock spawnFallingBlock(Location location) {
		return location.getWorld().spawnFallingBlock(location, getMaterial(), getMaterialData().getData());
	}
	
	@Override
	public ItemStack toItemStack(int quantity) {
		return new ItemStack(this.type, quantity, this.data);
	}
	
	@Override
	public boolean equals(MaterialData matData) {
		return matData.getItemType() == this.type && matData.getData() == this.data;
	}
	
	@Override
	public boolean equals(ItemStack itemStack) {
		return itemStack.getType() == this.type && itemStack.getDurability() == this.data;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.type,
			":",
			this.data
		);
	}
	
}
