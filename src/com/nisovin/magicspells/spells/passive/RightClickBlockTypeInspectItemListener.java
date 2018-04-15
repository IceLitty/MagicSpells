package com.nisovin.magicspells.spells.passive;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

// id;debug;blockType;offsetRange;matchItem:count,matchItem2:count
// when range == -1, offset position y will add one, and range is 0.1d.
// when range == -2, disregard all item match conditions and allow execution of skills, it is recommended to set id as last position.
// debug sample: [skill index, block type, other for() function index]
public class RightClickBlockTypeInspectItemListener extends PassiveListener {

    Map<Integer, List<PassiveSpell>> types = new HashMap<>();
    List<Integer> indexer = new ArrayList<>();
    Map<Integer, Boolean> tempDebug = new HashMap<>();
    Map<Integer, Material> materials = new HashMap<>();
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
            tempDebug.put(indexIfAdd, Boolean.parseBoolean(all[1]));
        }
        if (all.length > 2) {
            String[] split = all[2].split(",");
            for (String s : split) {
                s = s.trim();
                MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
                if (m != null) {
                    materials.put(indexIfAdd, m.getMaterial());
                    List<PassiveSpell> list = new ArrayList<>();
                    list.add(spell);
                    types.put(indexIfAdd, list);

                } else {
                    MagicSpells.error("Invalid type on rightclickblocktypeinspectitem trigger '" + var + "' on passive spell '" + spell.getInternalName() + '\'');
                }
            }
        }
        if (all.length > 3) {
            range.put(indexIfAdd, Double.parseDouble(all[3]));
        }
        if (all.length > 4) {
            String[] split = all[4].split(",");
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        List<PassiveSpell> list = getSpells(event.getClickedBlock(), event.getClickedBlock().getLocation());
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

    private List<PassiveSpell> getSpells(Block block, Location location) {
        for (int h = 0; h < indexer.size(); h++) {
            int indexTemp = indexer.get(h);
            if (tempDebug.get(indexTemp)) {
                MagicSpells.error("[" + indexTemp + "] = Start process ==================================================");
                MagicSpells.error("[" + indexTemp + "] player block: " + block.getType() + ", require block: " + materials.get(indexTemp));
            }
            if (materials.get(indexTemp).equals(block.getType())) {
                double sRange = range.get(indexTemp);
                List<Boolean> sIsPredefine = isPredefineItem.get(indexTemp);
                List<ItemStack> sPredefine = matchItemStacks.get(indexTemp);
                List<Material> sMaterials = matchMaterials.get(indexTemp);
                List<Integer> sCounts = matchMaterialsCount.get(indexTemp);
                if (tempDebug.get(indexTemp)) {
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
                for (Entity entity : entities) {
                    if (entity instanceof Item) {
                        if (Math.floor(sRange) >= 0) {
                            double rangeTemp = sRange;
                            if (rangeTemp == 0) rangeTemp = 0.1d;
                            if (tempDebug.get(indexTemp)) {
                                String name = ((Item)entity).getItemStack().getType().name();
                                MagicSpells.error("[" + indexTemp + "," + name + "] range:" + rangeTemp + ",ix:" + Math.floor(entity.getLocation().getX()) + ",iy:" + Math.floor(entity.getLocation().getY()) + ",iz:" + Math.floor(entity.getLocation().getZ()) + ",nx:" + location.getX() + ",ny:" + location.getY() + ",nz:" + location.getZ());
                                MagicSpells.error("[" + indexTemp + "," + name + "] check x:" + (location.getX() + rangeTemp >= Math.floor(entity.getLocation().getX())) + " " + (location.getX() - rangeTemp <= Math.floor(entity.getLocation().getX())));
                                MagicSpells.error("[" + indexTemp + "," + name + "] check y:" + (location.getY() + rangeTemp >= Math.floor(entity.getLocation().getY())) + " " + (location.getY() - rangeTemp <= Math.floor(entity.getLocation().getY())));
                                MagicSpells.error("[" + indexTemp + "," + name + "] check z:" + (location.getZ() + rangeTemp >= Math.floor(entity.getLocation().getZ())) + " " + (location.getZ() - rangeTemp <= Math.floor(entity.getLocation().getZ())));
                            }
                            if (location.getX() + rangeTemp >= Math.floor(entity.getLocation().getX()) && location.getX() - rangeTemp <= Math.floor(entity.getLocation().getX()) &&
                                    location.getY() + rangeTemp >= Math.floor(entity.getLocation().getY()) && location.getY() - rangeTemp <= Math.floor(entity.getLocation().getY()) &&
                                    location.getZ() + rangeTemp >= Math.floor(entity.getLocation().getZ()) && location.getZ() - rangeTemp <= Math.floor(entity.getLocation().getZ())) {
                                itemsMatchRange.add((Item)entity);
                            }
                        } else if (Math.floor(sRange) == -1) {
                            if (location.getX() + 0.1d >= Math.floor(entity.getLocation().getX()) && location.getX() - 0.1d <= Math.floor(entity.getLocation().getX()) &&
                                    location.getY() + 1.1d >= Math.floor(entity.getLocation().getY()) && location.getY() + 0.9d <= Math.floor(entity.getLocation().getY()) &&
                                    location.getZ() + 0.1d >= Math.floor(entity.getLocation().getZ()) && location.getZ() - 0.1d <= Math.floor(entity.getLocation().getZ())) {
                                itemsMatchRange.add((Item)entity);
                            }
                        } else if (Math.floor(sRange) == -2) {
                            itemsMatchRange.add(null);
                        }
                    }
                }
                if (itemsMatchRange.size() > 0) {
                    if (tempDebug.get(indexTemp)) {
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
                    List<Item> needRemove = new ArrayList<>();
                    List<Integer> needRemoveCount = new ArrayList<>();
                    List<Boolean> isRangeNegative2 = new ArrayList<>();
                    for (Item item : itemsMatchRange) {
                        if (item == null) {
                            counter++;
                            needRemove.add(null);
                            needRemoveCount.add(0);
                            isRangeNegative2.add(true);
                        } else {
                            List<Boolean> bIsPredefine = new ArrayList<>(sIsPredefine);
                            List<ItemStack> bPredefine = new ArrayList<>(sPredefine);
                            List<Material> bMaterials = new ArrayList<>(sMaterials);
                            List<Integer> bCounts = new ArrayList<>(sCounts);
                            if (tempDebug.get(indexTemp)) {
                                MagicSpells.error("[" + indexTemp + "," + item.getItemStack().getType().name() + "] bList size: b" + bIsPredefine.size() + " | p" + bPredefine.size() + " | m" + bMaterials.size() + " | c" + bCounts.size());
                                MagicSpells.error("[" + indexTemp + "," + item.getItemStack().getType().name() + "] needRemoveList size: i" + needRemove.size() + " | c" + needRemoveCount.size());
                            }
                            for (int j = 0; j < bMaterials.size(); j++) {
                                if (tempDebug.get(indexTemp)) {
                                    MagicSpells.error("[" + indexTemp + "," + item.getItemStack().getType().name() + "," + j + "] if predefine: " + bIsPredefine.get(j));
                                }
                                boolean matchItemWithoutCount = false;
                                if (bIsPredefine.get(j)) {
                                    if (tempDebug.get(indexTemp)) {
                                        MagicSpells.error("[" + indexTemp + "," + item.getItemStack().getType().name() + "," + j  + "] predefine match: " + item.getItemStack().isSimilar(bPredefine.get(j)));
                                    }
                                    if (item.getItemStack().isSimilar(bPredefine.get(j))) {
                                        matchItemWithoutCount = true;
                                    }
                                } else {
                                    if (tempDebug.get(indexTemp)) {
                                        MagicSpells.error("[" + indexTemp + "," + item.getItemStack().getType().name() + "," + j  + "] normal match: " + item.getItemStack().getType().equals(bMaterials.get(j)));
                                    }
                                    if (item.getItemStack().getType().equals(bMaterials.get(j))) {
                                        matchItemWithoutCount = true;
                                    }
                                }
                                if (matchItemWithoutCount) {
                                    if (tempDebug.get(indexTemp)) {
                                        MagicSpells.error("[" + indexTemp + "," + item.getItemStack().getType().name() + "," + j  + "] count: ITEMx" + item.getItemStack().getAmount() + " vs REQUIREx" + bCounts.get(j));
                                    }
                                    if (item.getItemStack().getAmount() == bCounts.get(j)) {
                                        needRemove.add(item);
                                        needRemoveCount.add(-1);
                                        isRangeNegative2.add(false);
                                        counter++;
                                        bIsPredefine.remove(j);
                                        bPredefine.remove(j);
                                        bMaterials.remove(j);
                                        bCounts.remove(j);
                                        break;
                                    } else if (item.getItemStack().getAmount() > bCounts.get(j)) {
                                        needRemove.add(item);
                                        needRemoveCount.add(bCounts.get(j));
                                        isRangeNegative2.add(false);
                                        counter++;
                                        bIsPredefine.remove(j);
                                        bPredefine.remove(j);
                                        bMaterials.remove(j);
                                        bCounts.remove(j);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (tempDebug.get(indexTemp)) {
                        MagicSpells.error("[" + indexTemp + "] match item data counter: " + counter + " size:" + sMaterials.size());
                    }
                    if (counter == sMaterials.size()) {
                        if (tempDebug.get(indexTemp)) {
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
                            if (!isRangeNegative2.get(i)) {
                                if (needRemoveCount.get(i) == -1) {
                                    needRemove.get(i).remove();
                                } else {
                                    ItemStack item = needRemove.get(i).getItemStack();
                                    item.setAmount(item.getAmount() - needRemoveCount.get(i));
                                    needRemove.get(i).setItemStack(item);
                                }
                            }
                        }
                        if (tempDebug.get(indexTemp)) {
                            MagicSpells.error("[" + indexTemp + "] ============================================== Run successfully! =");
                        }
                        return types.get(indexTemp);
                    }
                }
            }
        }
        return null;
    }
}
