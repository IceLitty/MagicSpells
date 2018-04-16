package com.nisovin.magicspells.spells.passive;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

//
// Format:  id;Right/LeftAction;debug;permissionNode;blockType1,blockType2:blockType2DataValue;modifyBlockMode;offsetRange;matchItem:count,matchItem2:count
// Example: 0;0;false;;CAULDRON:1,CAULDRON:2,CAULDRON:3;1;0;REDSTONE:5
// Result:  If the player puts 5 redstones into any water levels cauldron and right-clicks on it,
//          the 5 redstones will be removed and the skills in the skill list will be executed, and consume a level water of cauldron.
// Example: 99999;0;false;;CAULDRON:1,CAULDRON:2,CAULDRON:3;1;-3;AIR:0
// Result:  When other skills do not match, execution to this skill will remove all items and execute skills.
//
// when range equal -1, offset position y will add one, and range is 0.1d.
// when range equal -2, disregard all item match conditions and allow execution of skills, it is recommended to set id as last position.
// when range equal -3, the effect is similar to -2, but all items will be consumed.
//
// debug sample: [skill index, block type, other for() function index]
//
// Modify Block Mode List:
//                         0 : none for any block
//                  CAULDRON :
//                         1 : reduce data value
//                         2 : increase data value
//               SNOW(LAYER) :
//                         1 : reduce data value
//                         2 : increase data value
//        ENDER_PORTAL_FRAME :
//                         1 : remove eye
//                         2 : add eye
//                     ANVIL :
//                         1 : anvil is broken
//                         2 : anvil is broken (will destroy)
//                         3 : anvil is repair
//                PUMPKIN or :
//            JACK_O_LANTERN :
//          any num except 0 : they will swap each other
//

public class RightClickBlockTypeInspectItemListener extends PassiveListener {

    Map<Integer, List<PassiveSpell>> types = new HashMap<>();
    List<Integer> indexer = new ArrayList<>();
    Map<Integer, Integer> actions = new HashMap<>();
    Map<Integer, Boolean> tempDebug = new HashMap<>();
    Map<Integer, String> permissions = new HashMap<>();
    Map<Integer, List<MagicMaterial>> materials = new HashMap<>();
    Map<Integer, Integer> modifyBlock = new HashMap<>();
    Map<Integer, Double> range = new HashMap<>();
    Map<Integer, List<Boolean>> isPredefineItem = new HashMap<>();
    Map<Integer, List<ItemStack>> matchItemStacks = new HashMap<>();
    Map<Integer, List<Material>> matchMaterials = new HashMap<>();
    Map<Integer, List<Integer>> matchMaterialsCount = new HashMap<>();

    @Override
    public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
        String[] all = var.split(";");
        int indexIfAdd = types.size();
        if (all.length > 0) {
            indexIfAdd = Integer.parseInt(all[0]);
        }
        if (all.length > 1) {
            actions.put(indexIfAdd, Integer.parseInt(all[1]));
        }
        if (all.length > 2) {
            tempDebug.put(indexIfAdd, Boolean.parseBoolean(all[2]));
        }
        if (all.length > 3) {
            permissions.put(indexIfAdd, all[3]);
        }
        if (all.length > 4) {
            String[] split = all[4].split(",");
            List<MagicMaterial> nowBlocks = new ArrayList<>();
            for (String s : split) {
                s = s.trim();
                MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
                if (m != null) {
                    nowBlocks.add(m);
                    List<PassiveSpell> list = new ArrayList<>();
                    list.add(spell);
                    types.put(indexIfAdd, list);
                } else {
                    MagicSpells.error("Invalid type on rightclickblocktypeinspectitem trigger '" + var + "' on passive spell '" + spell.getInternalName() + '\'');
                }
            }
            materials.put(indexIfAdd, nowBlocks);
        }
        if (all.length > 5) {
            modifyBlock.put(indexIfAdd, Integer.parseInt(all[5]));
        }
        if (all.length > 6) {
            range.put(indexIfAdd, Double.parseDouble(all[6]));
        }
        if (all.length > 7) {
            String[] split = all[7].split(",");
            List<ItemStack> tempItemStackList = new ArrayList<>();
            List<Material> tempMaterialList = new ArrayList<>();
            List<Integer> tempCountList = new ArrayList<>();
            List<Boolean> tempIsPredefineList = new ArrayList<>();
            for (String s : split) {
                boolean tempIsPredefine = false;
                String[] temp = s.split(":");
                if (temp[0].contains("PRE|")) {
                    String preName = temp[0].replace("PRE|", "");
                    ItemStack isa = Util.getItemStackFromString(preName);
                    if (temp.length > 1 && Integer.parseInt(temp[1]) >= 0) {
                        tempCountList.add(Integer.parseInt(temp[1]));
                        isa.setAmount(Integer.parseInt(temp[1]));
                    } else {
                        tempCountList.add(1);
                        isa.setAmount(1);
                    }
                    tempItemStackList.add(isa);
                    tempMaterialList.add(null);
                    tempIsPredefine = true;
                } else {
                    s = s.trim();
                    MagicMaterial m = MagicSpells.getItemNameResolver().resolveItem(s);
                    if (m == null) { m = MagicSpells.getItemNameResolver().resolveBlock(s); }
                    if (m != null) {
                        tempItemStackList.add(null);
                        tempMaterialList.add(m.getMaterial());
                        if (temp.length > 1) tempCountList.add(Integer.parseInt(temp[1])); else tempCountList.add(1);
                    } else {
                        MagicSpells.error("Invalid type on rightclickblocktypeinspectitem trigger '" + var + "' on passive spell '" + spell.getInternalName() + '\'');
                    }
                }
                tempIsPredefineList.add(tempIsPredefine);
            }
            matchItemStacks.put(indexIfAdd, tempItemStackList);
            matchMaterials.put(indexIfAdd, tempMaterialList);
            matchMaterialsCount.put(indexIfAdd, tempCountList);
            isPredefineItem.put(indexIfAdd, tempIsPredefineList);
            indexer.add(indexIfAdd);
        }
        Collections.sort(indexer);
        if (tempDebug.values().contains(true)) {
            List<String> temp = new ArrayList<>();
            for (int j = 0; j < matchMaterials.get(indexIfAdd).size(); j++) {
                if (isPredefineItem.get(indexIfAdd).get(j)) {
                    temp.add("PRE|" + matchItemStacks.get(indexIfAdd).get(j).getType().name() + "x" + matchMaterialsCount.get(indexIfAdd).get(j));
                } else {
                    temp.add(matchMaterials.get(indexIfAdd).get(j) + "x" + matchMaterialsCount.get(indexIfAdd).get(j));
                }
            }
            MagicSpells.error("RCBTIIL[" + String.format("%1$5s", indexIfAdd + "") + "] Debug: " + (tempDebug.get(indexIfAdd) ? "1" : "0") + ", Range: " + range.get(indexIfAdd) + ", Items:" + String.join(" ", temp));
            List<Subspell> spellsTemp = types.get(indexIfAdd).get(0).getActivatedSpells();
            List<String> spells = new ArrayList<>();
            for (Subspell spellSingle : spellsTemp) {
                spells.add(spellSingle.getSpell().getName());
            }
            MagicSpells.error("RCBTIIL[" + String.format("%1$5s", indexIfAdd + "") + "] RunSpell: " + String.join(",", spells));
        }
    }

    @OverridePriority
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        int action = event.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : 0;
        // BUG: Left button cannot trigger method!
        List<PassiveSpell> list = getSpells(event.getClickedBlock(), event.getClickedBlock().getLocation(), action, event.getPlayer());
        if (list != null) {
            Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
            for (PassiveSpell spell : list) {
                if (!PassiveListener.isCancelStateOk(spell, event.isCancelled())) continue;
                if (!spellbook.hasSpell(spell, false)) continue;
                boolean casted = spell.activate(event.getPlayer(), event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5));
                if (!PassiveListener.cancelDefaultAction(spell, casted)) continue;
                event.setCancelled(true);
            }
        }
    }

    private List<PassiveSpell> getSpells(Block block, Location location, Integer action, Player player) {
        for (int h = 0; h < indexer.size(); h++) {
            int indexTemp = indexer.get(h);
            boolean debugMode = tempDebug.get(indexTemp);
            if (debugMode) {
                MagicSpells.error("[" + indexTemp + "] === Start process ===============================================");
            }
            boolean perm = false;
            if ("".equals(permissions.get(indexTemp))) {
                perm = true;
            } else if (player.hasPermission(permissions.get(indexTemp))) {
                perm = true;
            }
            if (debugMode) {
                MagicSpells.error("[" + indexTemp + "] perm state: " + perm + " " + permissions.get(indexTemp));
            }
            if (perm) {
                if (action.equals(actions.get(indexTemp))) {
                    List<MagicMaterial> magicMaterialList = materials.get(indexTemp);
                    for (MagicMaterial m : magicMaterialList) {
                        if (debugMode) {
                            MagicSpells.error("[" + indexTemp + "] player block: " + block.getType() + ":" + block.getState().getData().getData() + ", require block: " + m.getMaterial().name() + ":" + m.getMaterialData().getData());
                        }
                        if (m.equals(block.getState().getData())) {
                            double sRange = range.get(indexTemp);
                            List<Boolean> sIsPredefine = isPredefineItem.get(indexTemp);
                            List<ItemStack> sPredefine = matchItemStacks.get(indexTemp);
                            List<Material> sMaterials = matchMaterials.get(indexTemp);
                            List<Integer> sCounts = matchMaterialsCount.get(indexTemp);
                            if (debugMode) {
                                MagicSpells.error("[" + indexTemp + "] sList size: b" + sIsPredefine.size() + " | p" + sPredefine.size() + " | m" + sMaterials.size() + " | c" + sCounts.size());
                                List<String> debugMaterials1 = new ArrayList<>();
                                for (int i = 0; i < sMaterials.size(); i++) {
                                    if (sIsPredefine.get(i)) {
                                        debugMaterials1.add("PRE|" + sPredefine.get(i).getType().name() + "x" + sCounts.get(i));
                                    } else {
                                        debugMaterials1.add(sMaterials.get(i).name() + "x" + sCounts.get(i));
                                    }
                                }
                                MagicSpells.error("[" + indexTemp + "] ready: r:" + sRange + " I:" + String.join(",", debugMaterials1));
                            }
                            Entity[] entities = location.getChunk().getEntities();
                            List<Item> itemsMatchRange = new ArrayList<>();
                            List<Integer> itemsMatchRangeAddonMsg = new ArrayList<>();
                            for (Entity entity : entities) {
                                if (entity instanceof Item) {
                                    if (Math.floor(sRange) >= 0 || Math.floor(sRange) == -3) {
                                        double rangeTemp = sRange;
                                        if (rangeTemp == 0) rangeTemp = 0.1d;
                                        if (rangeTemp == -3) rangeTemp = 0.1d;
                                        if (debugMode) {
                                            String name = ((Item)entity).getItemStack().getType().name();
                                            MagicSpells.error("[" + indexTemp + "] [" + name + "] range:" + rangeTemp + ",ix:" + Math.floor(entity.getLocation().getX()) + ",iy:" + Math.floor(entity.getLocation().getY()) + ",iz:" + Math.floor(entity.getLocation().getZ()) + ",nx:" + location.getX() + ",ny:" + location.getY() + ",nz:" + location.getZ());
                                            MagicSpells.error("[" + indexTemp + "] [" + name + "] check x:" + (location.getX() + rangeTemp >= Math.floor(entity.getLocation().getX())) + " " + (location.getX() - rangeTemp <= Math.floor(entity.getLocation().getX())));
                                            MagicSpells.error("[" + indexTemp + "] [" + name + "] check y:" + (location.getY() + rangeTemp >= Math.floor(entity.getLocation().getY())) + " " + (location.getY() - rangeTemp <= Math.floor(entity.getLocation().getY())));
                                            MagicSpells.error("[" + indexTemp + "] [" + name + "] check z:" + (location.getZ() + rangeTemp >= Math.floor(entity.getLocation().getZ())) + " " + (location.getZ() - rangeTemp <= Math.floor(entity.getLocation().getZ())));
                                        }
                                        if (location.getX() + rangeTemp >= Math.floor(entity.getLocation().getX()) && location.getX() - rangeTemp <= Math.floor(entity.getLocation().getX()) &&
                                                location.getY() + rangeTemp >= Math.floor(entity.getLocation().getY()) && location.getY() - rangeTemp <= Math.floor(entity.getLocation().getY()) &&
                                                location.getZ() + rangeTemp >= Math.floor(entity.getLocation().getZ()) && location.getZ() - rangeTemp <= Math.floor(entity.getLocation().getZ())) {
                                            itemsMatchRange.add((Item)entity);
                                            if (Math.floor(sRange) == -3) {
                                                itemsMatchRangeAddonMsg.add(-3);
                                            } else {
                                                itemsMatchRangeAddonMsg.add(0);
                                            }
                                        }
                                    } else if (Math.floor(sRange) == -1) {
                                        if (location.getX() + 0.1d >= Math.floor(entity.getLocation().getX()) && location.getX() - 0.1d <= Math.floor(entity.getLocation().getX()) &&
                                                location.getY() + 1.1d >= Math.floor(entity.getLocation().getY()) && location.getY() + 0.9d <= Math.floor(entity.getLocation().getY()) &&
                                                location.getZ() + 0.1d >= Math.floor(entity.getLocation().getZ()) && location.getZ() - 0.1d <= Math.floor(entity.getLocation().getZ())) {
                                            itemsMatchRange.add((Item)entity);
                                            if (Math.floor(sRange) == -3) {
                                                itemsMatchRangeAddonMsg.add(-3);
                                            } else {
                                                itemsMatchRangeAddonMsg.add(0);
                                            }
                                        }
                                    } else if (Math.floor(sRange) == -2) {
                                        itemsMatchRange.add(null);
                                        itemsMatchRangeAddonMsg.add((int)Math.floor(sRange));
                                    }
                                }
                            }
                            if (itemsMatchRange.size() > 0) {
                                if (debugMode) {
                                    List<String> debugMaterials2 = new ArrayList<>();
                                    for (int i = 0; i < itemsMatchRange.size(); i++) {
                                        if (itemsMatchRange.get(i) == null) {
                                            debugMaterials2.add("NULLx0");
                                        } else {
                                            debugMaterials2.add(itemsMatchRange.get(i).getItemStack().getType().name() + "x" + itemsMatchRange.get(i).getItemStack().getAmount());
                                        }
                                    }
                                    MagicSpells.error("[" + indexTemp + "] range filter: " + String.join(",", debugMaterials2));
                                }
                                int counter = 0;
                                List<Material> matchedItems = new ArrayList<>();
                                List<Item> needRemove = new ArrayList<>();
                                List<Integer> needRemoveCount = new ArrayList<>();
                                List<Integer> isRangeNegative = new ArrayList<>();
                                List<Boolean> bIsPredefine = new ArrayList<>(sIsPredefine);
                                List<ItemStack> bPredefine = new ArrayList<>(sPredefine);
                                List<Material> bMaterials = new ArrayList<>(sMaterials);
                                List<Integer> bCounts = new ArrayList<>(sCounts);
                                for (int i = 0; i < itemsMatchRange.size(); i++) {
                                    Item item = itemsMatchRange.get(i);
                                    if (itemsMatchRangeAddonMsg.get(i) != 0) {
                                        counter++;
                                        needRemove.add(null);
                                        needRemoveCount.add(0);
                                        if (debugMode) {
                                            MagicSpells.error("[" + indexTemp + "] addon msg: " + itemsMatchRangeAddonMsg.get(i));
                                        }
                                        if (itemsMatchRangeAddonMsg.get(i) == -2) {
                                            isRangeNegative.add(-2);
                                        } else if (itemsMatchRangeAddonMsg.get(i) == -3) {
                                            isRangeNegative.add(-3);
                                        } else {
                                            isRangeNegative.add(null);
                                        }
                                    } else {
                                        if (debugMode) {
                                            MagicSpells.error("[" + indexTemp + "] [" + item.getItemStack().getType().name() + "] bList size: b" + bIsPredefine.size() + " | p" + bPredefine.size() + " | m" + bMaterials.size() + " | c" + bCounts.size());
                                            MagicSpells.error("[" + indexTemp + "] [" + item.getItemStack().getType().name() + "] needRemoveList size: i" + needRemove.size() + " | c" + needRemoveCount.size());
                                        }
                                        for (int j = 0; j < bMaterials.size(); j++) {
                                            if (debugMode) {
                                                MagicSpells.error("[" + indexTemp + "] [" + item.getItemStack().getType().name() + "] [" + j + "] if predefine: " + bIsPredefine.get(j));
                                            }
                                            boolean matchItemWithoutCount = false;
                                            if (bIsPredefine.get(j)) {
                                                if (debugMode) {
                                                    MagicSpells.error("[" + indexTemp + "] [" + item.getItemStack().getType().name() + "] [" + j  + "] predefine match: " + item.getItemStack().isSimilar(bPredefine.get(j)));
                                                }
                                                if (item.getItemStack().isSimilar(bPredefine.get(j))) {
                                                    matchItemWithoutCount = true;
                                                }
                                            } else {
                                                if (debugMode) {
                                                    MagicSpells.error("[" + indexTemp + "] [" + item.getItemStack().getType().name() + "] [" + j  + "] normal match: " + item.getItemStack().getType().equals(bMaterials.get(j)));
                                                }
                                                if (item.getItemStack().getType().equals(bMaterials.get(j))) {
                                                    matchItemWithoutCount = true;
                                                }
                                            }
                                            if (matchItemWithoutCount) {
                                                if (debugMode) {
                                                    MagicSpells.error("[" + indexTemp + "] [" + item.getItemStack().getType().name() + "] [" + j  + "] count: ITEMx" + item.getItemStack().getAmount() + " vs REQUIREx" + bCounts.get(j));
                                                }
                                                if (item.getItemStack().getAmount() == bCounts.get(j)) {
                                                    if (!matchedItems.contains(item.getItemStack().getType())) {
                                                        needRemove.add(item);
                                                        needRemoveCount.add(-1);
                                                        isRangeNegative.add(null);
                                                        counter++;
                                                        bIsPredefine.remove(j);
                                                        bPredefine.remove(j);
                                                        bMaterials.remove(j);
                                                        bCounts.remove(j);
                                                    }
                                                    matchedItems.add(item.getItemStack().getType());
                                                    break;
                                                } else if (item.getItemStack().getAmount() > bCounts.get(j)) {
                                                    if (!matchedItems.contains(item.getItemStack().getType())) {
                                                        needRemove.add(item);
                                                        needRemoveCount.add(bCounts.get(j));
                                                        isRangeNegative.add(null);
                                                        counter++;
                                                        bIsPredefine.remove(j);
                                                        bPredefine.remove(j);
                                                        bMaterials.remove(j);
                                                        bCounts.remove(j);
                                                    }
                                                    matchedItems.add(item.getItemStack().getType());
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (debugMode) {
                                    List<String> matchItemList = new ArrayList<>();
                                    for (Material mm : matchedItems) {
                                        matchItemList.add(mm.name());
                                    }
                                    MagicSpells.error("[" + indexTemp + "] match item data counter: " + counter + " size: " + sMaterials.size());
                                    MagicSpells.error("[" + indexTemp + "] match: " + String.join(" ", matchItemList));
                                    MagicSpells.error("[" + indexTemp + "] range +0 size: " + needRemove.size());
                                    MagicSpells.error("[" + indexTemp + "] range -0 size: " + isRangeNegative.size());
                                }
                                if (counter == sMaterials.size() || range.get(indexTemp) == -2 || range.get(indexTemp) == -3) {
                                    if (debugMode) {
                                        List<String> debugMaterials3 = new ArrayList<>();
                                        for (int i = 0; i < sMaterials.size(); i++) {
                                            if (sIsPredefine.get(i)) {
                                                debugMaterials3.add("PRE|" + sPredefine.get(i).getType().name() + "x" + sCounts.get(i));
                                            } else {
                                                debugMaterials3.add(sMaterials.get(i).name() + "x" + sCounts.get(i));
                                            }
                                        }
                                        MagicSpells.error("[" + indexTemp + "] remove: " + String.join(",", debugMaterials3));
                                    }
                                    for (int i = 0; i < needRemove.size(); i++) {
                                        if (isRangeNegative.get(i) == null) {
                                            if (needRemoveCount.get(i) == -1) {
                                                needRemove.get(i).remove();
                                            } else {
                                                ItemStack item = needRemove.get(i).getItemStack();
                                                item.setAmount(item.getAmount() - needRemoveCount.get(i));
                                                needRemove.get(i).setItemStack(item);
                                            }
                                        } else {
                                            if (isRangeNegative.get(i) == -3) {
                                                for (Item item : itemsMatchRange) {
                                                    item.remove();
                                                }
                                            }
                                        }
                                    }
                                    if (modifyBlock.get(indexTemp) != 0) {
                                        int mode = modifyBlock.get(indexTemp);
                                        if (block.getState().getType().equals(Material.CAULDRON)) {
                                            if (debugMode) { MagicSpells.error("[" + indexTemp + "] block type is " + block.getState().getType().name()); }
                                            if (mode == 1) {
                                                if (block.getState().getData().getData() > 0) {
                                                    if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is " + block.getState().getData().getData()); }
                                                    block.setData((byte)(block.getState().getData().getData() - 1));
                                                }
                                            } else if (mode == 2) {
                                                if (block.getState().getData().getData() < 3) {
                                                    if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is " + block.getState().getData().getData()); }
                                                    block.setData((byte)(block.getState().getData().getData() + 1));
                                                }
                                            }
                                        } else if (block.getState().getType().equals(Material.SNOW)) {
                                            if (debugMode) { MagicSpells.error("[" + indexTemp + "] block type is " + block.getState().getType().name()); }
                                            if (mode == 1) {
                                                if (block.getState().getData().getData() > 1) {
                                                    if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is " + block.getState().getData().getData()); }
                                                    block.setData((byte)(block.getState().getData().getData() - 1));
                                                } else {
                                                    block.setType(Material.AIR);
                                                }
                                            } else if (mode == 2) {
                                                if (block.getState().getData().getData() < 8) {
                                                    if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is " + block.getState().getData().getData()); }
                                                    block.setData((byte)(block.getState().getData().getData() + 1));
                                                }
                                            }
                                        } else if (block.getState().getType().equals(Material.ENDER_PORTAL_FRAME)) {
                                            if (debugMode) { MagicSpells.error("[" + indexTemp + "] block type is " + block.getState().getType().name()); }
                                            if (mode == 1) {
                                                switch (block.getState().getData().getData()) {
                                                    case 4:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 4"); }
                                                        block.setData((byte)0);
                                                        break;
                                                    case 5:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 5"); }
                                                        block.setData((byte)1);
                                                        break;
                                                    case 6:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 6"); }
                                                        block.setData((byte)2);
                                                        break;
                                                    case 7:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 7"); }
                                                        block.setData((byte)3);
                                                        break;
                                                }
                                            } else if (mode == 2) {
                                                switch (block.getState().getData().getData()) {
                                                    case 0:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 0"); }
                                                        block.setData((byte)4);
                                                        break;
                                                    case 1:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 1"); }
                                                        block.setData((byte)5);
                                                        break;
                                                    case 2:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 2"); }
                                                        block.setData((byte)6);
                                                        break;
                                                    case 3:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 3"); }
                                                        block.setData((byte)7);
                                                        break;
                                                }
                                            }
                                        } else if (block.getState().getType().equals(Material.ANVIL)) {
                                            if (debugMode) { MagicSpells.error("[" + indexTemp + "] block type is " + block.getState().getType().name()); }
                                            if (mode == 1 || mode == 2) {
                                                switch (block.getState().getData().getData()) {
                                                    case 0:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 0"); }
                                                        block.setData((byte)4);
                                                        break;
                                                    case 1:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 1"); }
                                                        block.setData((byte)5);
                                                        break;
                                                    case 2:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 2"); }
                                                        block.setData((byte)6);
                                                        break;
                                                    case 3:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 3"); }
                                                        block.setData((byte)7);
                                                        break;
                                                    case 4:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 4"); }
                                                        block.setData((byte)8);
                                                        break;
                                                    case 5:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 5"); }
                                                        block.setData((byte)9);
                                                        break;
                                                    case 6:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 6"); }
                                                        block.setData((byte)10);
                                                        break;
                                                    case 7:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 7"); }
                                                        block.setData((byte)11);
                                                        break;
                                                }
                                                if (mode == 2) {
                                                    switch (block.getState().getData().getData()) {
                                                        case 8:
                                                        case 9:
                                                        case 10:
                                                        case 11:
                                                            block.setType(Material.AIR);
                                                            break;
                                                    }
                                                }
                                            } else if (mode == 3) {
                                                switch (block.getState().getData().getData()) {
                                                    case 11:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 11"); }
                                                        block.setData((byte)7);
                                                        break;
                                                    case 10:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 10"); }
                                                        block.setData((byte)6);
                                                        break;
                                                    case 9:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 9"); }
                                                        block.setData((byte)5);
                                                        break;
                                                    case 8:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 8"); }
                                                        block.setData((byte)4);
                                                        break;
                                                    case 7:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 7"); }
                                                        block.setData((byte)3);
                                                        break;
                                                    case 6:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 6"); }
                                                        block.setData((byte)2);
                                                        break;
                                                    case 5:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 5"); }
                                                        block.setData((byte)1);
                                                        break;
                                                    case 4:
                                                        if (debugMode) { MagicSpells.error("[" + indexTemp + "] block data is 4"); }
                                                        block.setData((byte)0);
                                                        break;
                                                }
                                            }
                                        } else if (block.getState().getType().equals(Material.PUMPKIN)) {
                                            if (debugMode) { MagicSpells.error("[" + indexTemp + "] block type is " + block.getState().getType().name()); }
                                            block.setType(Material.JACK_O_LANTERN);
                                        } else if (block.getState().getType().equals(Material.JACK_O_LANTERN)) {
                                            if (debugMode) { MagicSpells.error("[" + indexTemp + "] block type is " + block.getState().getType().name()); }
                                            block.setType(Material.PUMPKIN);
                                        }
                                    }
                                    if (!"".equals(permissions.get(indexTemp))) {
//                                        PermissionAttachment pma = player.addAttachment(MagicSpells.plugin);
//                                        pma.unsetPermission(permissions.get(indexTemp));
//                                        player.removeAttachment(pma);
                                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "manudelp " + player.getName() + " " + permissions.get(indexTemp));
                                        if (debugMode) {
                                            MagicSpells.error("[" + indexTemp + "] perm " + permissions.get(indexTemp) + " removed!");
                                        }
                                    }
                                    if (debugMode) {
                                        List<Subspell> spellsTemp1 = types.get(indexTemp).get(0).getActivatedSpells();
                                        List<String> spells1 = new ArrayList<>();
                                        for (Subspell spellSingle : spellsTemp1) {
                                            spells1.add(spellSingle.getSpell().getName());
                                        }
                                        MagicSpells.error("[" + indexTemp + "] will run skill: " + String.join(" ", spells1));
                                        MagicSpells.error("[" + indexTemp + "] =========================================== Run successfully! ===");
                                    }
                                    return types.get(indexTemp);
                                }
                            }
                        }
                    }
                }
            }
            if (debugMode) {
                MagicSpells.error("[" + indexTemp + "] ================================================ Run failed ! ===");
            }
        }
        return null;
    }
}
