package com.nisovin.magicspells.materials;

import java.util.Random;


public interface ItemNameResolver {

	Random rand = new Random();

	@Deprecated
	ItemTypeAndData resolve(String string);
	
	MagicMaterial resolveItem(String string);
	
	MagicMaterial resolveBlock(String string);
	
	class ItemTypeAndData {

		public String id_str = "stone";
		public short data = 0;
		
	}
	
}
