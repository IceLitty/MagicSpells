package com.nisovin.magicspells.spells.passive;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | Args Key                                                                      | Args Default Value                |
//  | Args Description                                                              |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | debug                                                                         | 0 (false)                         |
//  | Turn on or off Debug Mode.                                                    |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | w wn world worldName                                                          | null                              |
//  | If not null, recipe will check player's world name is match, also can multi world like 'world1,world2'.           |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | x                                                                             | null                              |
//  | Specially player in a coordinate, axis x.                                     |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | y                                                                             | null                              |
//  | Specially player in a coordinate, axis y.                                     |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | z                                                                             | null                              |
//  | Specially player in a coordinate, axis z.                                     |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | x2                                                                            | null                              |
//  | Specially player in a coordinate range, axis x.                               |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | y2                                                                            | null                              |
//  | Specially player in a coordinate range, axis y.                               |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | z2                                                                            | null                              |
//  | Specially player in a coordinate range, axis z.                               |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | yaw                                                                           | null                              |
//  | Specially player in an angel, axis yaw.                                       |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | pitch                                                                         | null                              |
//  | Specially player in an angel, axis pitch.                                     |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | yaw2                                                                          | null                              |
//  | Specially player in an angel range, axis yaw.                                 |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | pitch2                                                                        | null                              |
//  | Specially player in an angel range, axis pitch.                               |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | i invert invertYaw                                                            | 0 (false)                         |
//  | Invert yaw judge result.                                                      |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | b bt block blocks blockType blockTypes                                        | null                              |
//  | Specially block under player.                                                 |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | ia air fly includeAir                                                         | 0 (false)                         |
//  | Is block under player or interval with Air block.                             |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+

public class WalkListener extends PassiveListener {

    private List<WalkListenerClass> recipes = new ArrayList<>();
    private String moduleLogPrefix = "[WL]";
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
        WalkListenerClass recipe = new WalkListenerClass();
        String[] args = var.split(";");
        for (String arg : args) {
            String[] kvList = arg.split("=");
            if (kvList.length != 2) { MagicSpells.error(String.format("%s In register %s has more or less than one equal key/value split char.", moduleLogPrefix, var)); continue; }
            switch (kvList[0].toLowerCase()) {
                case "debug": {
                    recipe.setDebug(kvList[1].equals("1"));
                    break;
                }
                case "w":
                case "wn":
                case "world":
                case "worldName": {
                    recipe.setWorldName(Arrays.asList(kvList[1].split(",")));
                    break;
                }
                case "x": {
                    try {
                        recipe.setX(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems X is not an integer number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "y": {
                    try {
                        recipe.setY(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Y is not an integer number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "z": {
                    try {
                        recipe.setZ(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Z is not an integer number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "x2": {
                    try {
                        int p2 = Integer.parseInt(kvList[1]);
                        if (recipe.getX() == null) {
                            recipe.setX(p2);
                        } else {
                            if (p2 < recipe.getX()) {
                                recipe.setX2(recipe.getX());
                                recipe.setX(p2);
                            } else {
                                recipe.setX2(p2);
                            }
                        }
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems X2 is not an integer number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "y2": {
                    try {
                        int p2 = Integer.parseInt(kvList[1]);
                        if (recipe.getY() == null) {
                            recipe.setY(p2);
                        } else {
                            if (p2 < recipe.getY()) {
                                recipe.setY2(recipe.getY());
                                recipe.setY(p2);
                            } else {
                                recipe.setY2(p2);
                            }
                        }
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Y2 is not an integer number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "z2": {
                    try {
                        int p2 = Integer.parseInt(kvList[1]);
                        if (recipe.getZ() == null) {
                            recipe.setZ(p2);
                        } else {
                            if (p2 < recipe.getZ()) {
                                recipe.setZ2(recipe.getZ());
                                recipe.setZ(p2);
                            } else {
                                recipe.setZ2(p2);
                            }
                        }
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Z2 is not an integer number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "yaw": {
                    try {
                        float yaw = Float.parseFloat(kvList[1]);
                        while (yaw < 0) {
                            yaw += 360.0f;
                        }
                        while (yaw > 360.0f) {
                            yaw -= 360.0f;
                        }
                        recipe.setYaw(yaw);
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Yaw is not a float number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "pitch": {
                    try {
                        recipe.setPitch(Float.parseFloat(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Pitch is not a float number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "yaw2": {
                    try {
                        float a2 = Float.parseFloat(kvList[1]);
                        while (a2 < 0) {
                            a2 += 360.0f;
                        }
                        while (a2 > 360.0f) {
                            a2 -= 360.0f;
                        }
                        if (recipe.getYaw() == null) {
                            recipe.setYaw(a2);
                        } else {
                            if (a2 < recipe.getYaw()) {
                                recipe.setYaw2(recipe.getYaw());
                                recipe.setYaw(a2);
                            } else {
                                recipe.setYaw2(a2);
                            }
                        }
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Yaw2 is not a float number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "pitch2": {
                    try {
                        float a2 = Float.parseFloat(kvList[1]);
                        if (recipe.getPitch() == null) {
                            recipe.setPitch(a2);
                        } else {
                            if (a2 < recipe.getPitch()) {
                                recipe.setPitch2(recipe.getPitch() + 0.0f);
                                recipe.setPitch(a2);
                            } else {
                                recipe.setPitch2(a2);
                            }
                        }
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register seems Pitch2 is not a float number.", moduleLogPrefix)); continue; }
                    break;
                }
                case "i":
                case "invert":
                case "invertYaw": {
                    recipe.setInvertYaw(kvList[1].equals("1"));
                    break;
                }
                case "b":
                case "bt":
                case "block":
                case "blocks":
                case "blockType":
                case "blockTypes": {
                    String[] split = kvList[1].split(",");
                    List<MagicMaterial> nowBlocks = new ArrayList<>();
                    for (String s : split) {
                        MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
                        if (m != null) {
                            nowBlocks.add(m);
                        } else { MagicSpells.error(String.format("%s In register seems blocks %s can't find.", moduleLogPrefix, s)); continue; }
                    }
                    recipe.setBlockTypes(nowBlocks);
                    break;
                }
                case "ia":
                case "air":
                case "fly":
                case "includeAir": {
                    recipe.setIncludeAir(kvList[1].equals("1"));
                    break;
                }
            }
            recipe.setSkills(Arrays.asList(spell));
        }
        recipes.add(recipe);
	}
	
	@OverridePriority
	@EventHandler
	public void onWalkOverBlockEvent(PlayerMoveEvent event) {
	    float yaw = event.getPlayer().getLocation().getYaw();
        while (yaw < 0) {
            yaw += 360.0f;
        }
        while (yaw > 360.0f) {
            yaw -= 360.0f;
        }
	    float pitch = event.getPlayer().getLocation().getPitch();
		List<PassiveSpell> list = getSpells(event.getPlayer(), new Location(event.getPlayer().getLocation().getWorld(), event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ(), yaw, pitch));
		if (list.size() > 0) {
			Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
			for (PassiveSpell spell : list) {
				if (!PassiveListener.isCancelStateOk(spell, event.isCancelled())) continue;
				if (!spellbook.hasSpell(spell, false)) continue;
				boolean casted = spell.activate(event.getPlayer(), event.getPlayer().getLocation().add(0.5, 0.5, 0.5));
				if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
				event.setCancelled(true);
			}
		}
	}
	
	private List<PassiveSpell> getSpells(Player player, Location position) {
	    List<PassiveSpell> sl = new ArrayList<>();
        for (WalkListenerClass recipe : recipes) {
            if (player.hasPermission("magicspells.cast." + recipe.getSkills().get(0).getPermissionName())) {
                if (recipe.getSkills().get(0).getCooldown(player) <= 0) {
                    if (recipe.isDebug()) {
                        MagicSpells.error(String.format("%s ==================================================== Run start! ===", moduleLogPrefix));
                        MagicSpells.error(String.format("Skill name: %s", recipe.getSkills().get(0).getInternalName()));
                    }
                    boolean worldFlag = false;
                    if (recipe.getWorldName().size() == 0) { worldFlag = true; }
                    else {
                        if (recipe.isDebug()) { MagicSpells.error(String.format("player world: %s, must in: %s", position.getWorld().getName(), String.join(",", recipe.getWorldName()))); }
                        if (recipe.getWorldName().contains(position.getWorld().getName())) {
                            worldFlag = true;
                        }
                    }
                    if (worldFlag) {
                        boolean posFlag = false;
                        if (recipe.getX() == null || recipe.getY() == null || recipe.getZ() == null) {
                            posFlag = true;
                        } else {
                            if (recipe.getX2() == null || recipe.getY2() == null || recipe.getZ2() == null) {
                                if (recipe.isDebug()) { MagicSpells.error(String.format("position judge: x[%d==%d], y[%d==%d], z[%d==%d]", position.getBlockX(), recipe.getX(), position.getBlockY(), recipe.getY(), position.getBlockZ(), recipe.getZ())); }
                                if (position.getBlockX() == recipe.getX() && position.getBlockY() == recipe.getY() && position.getBlockZ() == recipe.getZ()) {
                                    posFlag = true;
                                }
                            } else {
                                if (recipe.isDebug()) { MagicSpells.error(String.format("position judge: x[%d<=%d<=%d], y[%d<=%d<=%d], z[%d<=%d<=%d]", recipe.getX(), position.getBlockX(), recipe.getX2(), recipe.getY(), position.getBlockY(), recipe.getY2(), recipe.getZ(), position.getBlockZ(), recipe.getZ2())); }
                                if (betweenPosition(recipe.getX(), recipe.getX2(), position.getBlockX()) && betweenPosition(recipe.getY(), recipe.getY2(), position.getBlockY()) && betweenPosition(recipe.getZ(), recipe.getZ2(), position.getBlockZ())) {
                                    posFlag = true;
                                }
                            }
                        }
                        if (posFlag) {
                            boolean yawFlag = false;
                            if (recipe.getYaw() == null) { yawFlag = true; }
                            else {
                                if (recipe.getYaw2() == null) {
                                    if (recipe.isDebug()) { MagicSpells.error(String.format("angel judge: yaw[%f == %f]", recipe.getYaw(), position.getYaw())); }
                                    if (position.getYaw() == recipe.getYaw()) {
                                        yawFlag = true;
                                    }
                                } else {
                                    if (recipe.isDebug()) { MagicSpells.error(String.format("angel judge: yaw[%f <= %f <= %f]", recipe.getYaw(), position.getYaw(), recipe.getYaw2())); }
                                    if (betweenAngel(recipe.getYaw(), recipe.getYaw2(), position.getYaw())) {
                                        yawFlag = true;
                                    }
                                }
                                if (recipe.isInvertYaw()) {
                                    yawFlag = !yawFlag;
                                    if (recipe.isDebug()) { MagicSpells.error(String.format("angel judge: yaw flag invert: %s", yawFlag)); }
                                }
                            }
                            if (yawFlag) {
                                boolean pitchFlag = false;
                                if (recipe.getPitch() == null) { pitchFlag = true; }
                                else {
                                    if (recipe.getPitch2() == null) {
                                        if (recipe.isDebug()) { MagicSpells.error(String.format("angel judge: pitch[%f == %f]", recipe.getPitch(), position.getPitch())); }
                                        if (position.getPitch() == recipe.getPitch()) {
                                            pitchFlag = true;
                                        }
                                    } else {
                                        if (recipe.isDebug()) { MagicSpells.error(String.format("angel judge: pitch[%f <= %f <= %f]", recipe.getPitch(), position.getPitch(), recipe.getPitch2())); }
                                        if (betweenAngel(recipe.getPitch(), recipe.getPitch2(), position.getPitch())) {
                                            pitchFlag = true;
                                        }
                                    }
                                }
                                if (pitchFlag) {
                                    boolean materialFlag = false;
                                    if (recipe.getBlockTypes().size() == 0) { materialFlag = true; }
                                    else {
                                        if (recipe.isIncludeAir()) {
                                            int x = position.getBlockX();
                                            int y = position.getBlockY() - 1;
                                            int z = position.getBlockZ();
                                            while (true) {
                                                if (position.getWorld().getBlockAt(x, y, z).getType() == Material.AIR) {
                                                    y -= 1;
                                                } else {
                                                    if (blockTypeIsMatch(recipe.getBlockTypes(), position.getWorld().getBlockAt(x, y, z), recipe.isDebug())) {
                                                        materialFlag = true;
                                                    }
                                                    break;
                                                }
                                                if (y < 1) {
                                                    break;
                                                }
                                            }
                                        } else {
                                            if (blockTypeIsMatch(recipe.getBlockTypes(), position.getWorld().getBlockAt(position.getBlockX(), position.getBlockY() - 1, position.getBlockZ()), recipe.isDebug())) {
                                                materialFlag = true;
                                            }
                                        }
                                    }
                                    if (materialFlag) {
                                        sl.addAll(recipe.getSkills());
                                        if (recipe.isDebug()) MagicSpells.error(String.format("%s ============================================= Run successfully! ===", moduleLogPrefix));
                                    } else if (recipe.isDebug()) MagicSpells.error(String.format("%s =========== Run failed because block under player is not match! ===", moduleLogPrefix));
                                } else if (recipe.isDebug()) MagicSpells.error(String.format("%s ================= Run failed because player pitch is not match! ===", moduleLogPrefix));
                            } else if (recipe.isDebug()) MagicSpells.error(String.format("%s =================== Run failed because player yaw is not match! ===", moduleLogPrefix));
                        } else if (recipe.isDebug()) MagicSpells.error(String.format("%s ============== Run failed because player position is not match! ===", moduleLogPrefix));
                    } else if (recipe.isDebug()) MagicSpells.error(String.format("%s ========= Run failed because player doesn't in specified world! ===", moduleLogPrefix));
                }
            }
        }
        return sl;
	}

	private boolean blockTypeIsMatch(List<MagicMaterial> recipeBlockTypes, Block block, boolean debugMode) {
        for (MagicMaterial actionBlock : recipeBlockTypes) {
            if (debugMode) { MagicSpells.error(String.format("block judge: %s == %s", block.getState().getData(), actionBlock.getMaterialData())); }
            if (actionBlock.equals(block.getState().getData())) {
                return true;
            }
        }
        return false;
    }

	private boolean betweenPosition(int p, int p2, int p_now) {
        return p <= p_now && p_now <= p2;
    }

    private boolean betweenAngel(float a, float a2, float a_now) {
        return a <= a_now && a_now <= a2;
    }

    private class WalkListenerClass {
	    private boolean debug = false;
	    private List<String> worldName = new ArrayList<>();
	    private Integer x = 0;
	    private Integer y = 0;
	    private Integer z = 0;
	    private Integer x2;
	    private Integer y2;
	    private Integer z2;
	    private Float yaw;
	    private Float pitch;
        private Float yaw2;
	    private Float pitch2;
	    private boolean invertYaw = false;
        private List<MagicMaterial> blockTypes = new ArrayList<>();
        private boolean includeAir = false;
        private List<PassiveSpell> skills = new ArrayList<>();

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public List<String> getWorldName() {
            return worldName;
        }

        public void setWorldName(List<String> worldName) {
            this.worldName = worldName;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public Integer getZ() {
            return z;
        }

        public void setZ(Integer z) {
            this.z = z;
        }

        public Integer getX2() {
            return x2;
        }

        public void setX2(Integer x2) {
            this.x2 = x2;
        }

        public Integer getY2() {
            return y2;
        }

        public void setY2(Integer y2) {
            this.y2 = y2;
        }

        public Integer getZ2() {
            return z2;
        }

        public void setZ2(Integer z2) {
            this.z2 = z2;
        }

        public Float getYaw() {
            return yaw;
        }

        public void setYaw(Float yaw) {
            this.yaw = yaw;
        }

        public Float getPitch() {
            return pitch;
        }

        public void setPitch(Float pitch) {
            this.pitch = pitch;
        }

        public Float getYaw2() {
            return yaw2;
        }

        public void setYaw2(Float yaw2) {
            this.yaw2 = yaw2;
        }

        public Float getPitch2() {
            return pitch2;
        }

        public void setPitch2(Float pitch2) {
            this.pitch2 = pitch2;
        }

        public boolean isInvertYaw() {
            return invertYaw;
        }

        public void setInvertYaw(boolean invertYaw) {
            this.invertYaw = invertYaw;
        }

        public List<MagicMaterial> getBlockTypes() {
            return blockTypes;
        }

        public void setBlockTypes(List<MagicMaterial> blockTypes) {
            this.blockTypes = blockTypes;
        }

        public boolean isIncludeAir() {
            return includeAir;
        }

        public void setIncludeAir(boolean includeAir) {
            this.includeAir = includeAir;
        }

        public List<PassiveSpell> getSkills() {
            return skills;
        }

        public void setSkills(List<PassiveSpell> skills) {
            this.skills = skills;
        }
    }

}
