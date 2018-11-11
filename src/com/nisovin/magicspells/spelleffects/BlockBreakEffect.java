package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Particle;
import org.bukkit.material.MaterialData;

import com.nisovin.magicspells.DebugHandler;

/**
 * BlockBreakEffect<br>
 * <table border=1>
 *     <tr>
 *         <th>
 *             Config Field
 *         </th>
 *         <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>id</code>
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class BlockBreakEffect extends SpellEffect {

	String id = "stone";
	int meta = 0;
	
	@Override
	public void loadFromString(String string) {
		if (string != null && !string.isEmpty()) {
			id = string;
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		id = config.getString("id", id);
        meta = config.getInt("meta", meta);
	}
	
	@Override
	public Runnable playEffectLocation(Location location) {
        location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.add(0, 0.5, 0), 100, new MaterialData(Material.getMaterial(id), (byte)meta));
		return null;
	}
	
}
