package com.nisovin.magicspells.materials;

import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Objects;

public class MagicUnknownAnyDataMaterial extends MagicUnknownMaterial {

	public MagicUnknownAnyDataMaterial(String type) {
		super(type, (short)0);
	}
	
	@Override
	public boolean equals(MaterialData matData) {
		return matData.getItemType() == super.type;
	}
	
	@Override
	public boolean equals(ItemStack itemStack) {
		return itemStack.getType() == super.type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
				super.type,
			":*"
		);
	}

}
