package com.nisovin.magicspells.spells.passive;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
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
import org.bukkit.material.MaterialData;

import java.util.*;

// debug;blockType,blockType2;offsetRange;matchItem:count,matchItem2:count
// when range == -1, offset position y will add one, and range is 0.1d.
// debug sample: [skill index, block type, other for() function index]
public class RightClickBlockTypeInspectItemListener extends PassiveListener {

    List<Material> materials = new ArrayList<>();
    Map<Integer, List<PassiveSpell>> types = new HashMap<>();
    List<List<Boolean>> isPredefineItem = new ArrayList<>();
    List<List<ItemStack>> matchItemStacks = new ArrayList<>();
    List<List<Material>> matchMaterials = new ArrayList<>();
    List<List<Integer>> matchMaterialsCount = new ArrayList<>();
    List<Double> range = new ArrayList<>();
    List<Boolean> tempDebug = new ArrayList<>();

    @Override
    public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
        String[] all = var.split(";");
        if (all.length > 0) {
            tempDebug.add(Boolean.parseBoolean(all[0]));
        }
        if (all.length > 1) {
            int indexIfAdd = matchMaterials.size();
            String[] split = all[1].split(",");
            for (String s : split) {
                s = s.trim();
                MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
                if (m != null) {
                    materials.add(m.getMaterial());
                    List<PassiveSpell> list = types.computeIfAbsent(indexIfAdd, material -> new ArrayList<>());
                    list.add(spell);
                } else {
                    MagicSpells.error("Invalid type on rightclickblocktypeinspectitem trigger '" + var + "' on passive spell '" + spell.getInternalName() + '\'');
                }
            }
        }
        if (all.length > 2) {
            range.add(Double.parseDouble(all[2]));
        }
        if (all.length > 3) {
            String[] split = all[3].split(",");
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
            matchItemStacks.add(tempItemStackList);
            matchMaterials.add(tempMaterialList);
            matchMaterialsCount.add(tempCountList);
            isPredefineItem.add(tempIsPredefineList);
        }
        if (tempDebug.contains(true)) {
            List<String> temp = new ArrayList<>();
            int index = matchMaterials.size() - 1;
            for (int j = 0; j < matchMaterials.get(index).size(); j++) {
                if (isPredefineItem.get(index).get(j)) {
                    temp.add("PRE|" + matchItemStacks.get(index).get(j).getType().name() + "x" + matchMaterialsCount.get(index).get(j));
                } else {
                    temp.add(matchMaterials.get(index).get(j) + "x" + matchMaterialsCount.get(index).get(j));
                }
            }
            MagicSpells.error("Spell[RCBTIIL] Debug: " + (tempDebug.get(index) ? "1" : "0") + ", Range: " + range.get(index) + ", Items:" + String.join(" ", temp));
            List<String> out = new ArrayList<>();
            for (Map.Entry<Integer, List<PassiveSpell>> entry : types.entrySet()) {
                out.add(entry.getKey() + ":" + entry.getValue().get(0).getActivatedSpells().get(0).getSpell().getName());
            }
            MagicSpells.error("Spell[RCBTIIL] Debug2: " + out.get(out.size() - 1));
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
        for (int h = 0; h < range.size(); h++) {
            if (tempDebug.get(h)) {
                MagicSpells.error("[" + h + "] Start process ===================================================");
                MagicSpells.error("[" + h + "] player block: " + block.getType() + ", require block: " + materials.get(h));
            }
            if (materials.get(h).equals(block.getType())) {
                double sRange = range.get(h);
                List<Boolean> sIsPredefine = isPredefineItem.get(h);
                List<ItemStack> sPredefine = matchItemStacks.get(h);
                List<Material> sMaterials = matchMaterials.get(h);
                List<Integer> sCounts = matchMaterialsCount.get(h);
                if (tempDebug.get(h)) {
                    MagicSpells.error("[" + h + "]sList size: b" + sIsPredefine.size() + " | p" + sPredefine.size() + " | m" + sMaterials.size() + " | c" + sCounts.size());
                    List<String> debugMaterials1 = new ArrayList<>();
                    for (int i = 0; i < sMaterials.size(); i++) {
                        if (sIsPredefine.get(i)) {
                            debugMaterials1.add("PRE|" + sPredefine.get(i).getType().name() + "x" + sCounts.get(i));
                        } else {
                            debugMaterials1.add(sMaterials.get(i).name() + "x" + sCounts.get(i));
                        }
                    }
                    MagicSpells.error("[" + h + "]ready: r:" + sRange + " I:" + String.join(",", debugMaterials1));
                }
                Entity[] entities = location.getChunk().getEntities();
                List<Item> itemsMatchRange = new ArrayList<>();
                for (Entity entity : entities) {
                    if (entity instanceof Item) {
                        if (Math.floor(sRange) >= 0) {
                            if (sRange == 0) range.set(h, 0.1d);
                            if (tempDebug.get(h)) {
                                String name = ((Item)entity).getItemStack().getType().name();
                                MagicSpells.error("[" + h + "," + name + "]range:" + sRange + ",ix:" + Math.floor(entity.getLocation().getX()) + ",iy:" + Math.floor(entity.getLocation().getY()) + ",iz:" + Math.floor(entity.getLocation().getZ()) + ",nx:" + location.getX() + ",ny:" + location.getY() + ",nz:" + location.getZ());
                                MagicSpells.error("[" + h + "," + name + "]check x:" + (location.getX() + sRange >= Math.floor(entity.getLocation().getX())) + " " + (location.getX() - sRange <= Math.floor(entity.getLocation().getX())));
                                MagicSpells.error("[" + h + "," + name + "]check y:" + (location.getY() + sRange >= Math.floor(entity.getLocation().getY())) + " " + (location.getY() - sRange <= Math.floor(entity.getLocation().getY())));
                                MagicSpells.error("[" + h + "," + name + "]check z:" + (location.getZ() + sRange >= Math.floor(entity.getLocation().getZ())) + " " + (location.getZ() - sRange <= Math.floor(entity.getLocation().getZ())));
                            }
                            if (location.getX() + sRange >= Math.floor(entity.getLocation().getX()) && location.getX() - sRange <= Math.floor(entity.getLocation().getX()) &&
                                    location.getY() + sRange >= Math.floor(entity.getLocation().getY()) && location.getY() - sRange <= Math.floor(entity.getLocation().getY()) &&
                                    location.getZ() + sRange >= Math.floor(entity.getLocation().getZ()) && location.getZ() - sRange <= Math.floor(entity.getLocation().getZ())) {
                                itemsMatchRange.add((Item)entity);
                            }
                        } else if (Math.floor(sRange) == -1) {
                            if (location.getX() + 0.1d >= Math.floor(entity.getLocation().getX()) && location.getX() - 0.1d <= Math.floor(entity.getLocation().getX()) &&
                                    location.getY() + 1.1d >= Math.floor(entity.getLocation().getY()) && location.getY() + 0.9d <= Math.floor(entity.getLocation().getY()) &&
                                    location.getZ() + 0.1d >= Math.floor(entity.getLocation().getZ()) && location.getZ() - 0.1d <= Math.floor(entity.getLocation().getZ())) {
                                itemsMatchRange.add((Item)entity);
                            }
                        }
                    }
                }
                if (itemsMatchRange.size() > 0) {
                    if (tempDebug.get(h)) {
                        List<String> debugMaterials2 = new ArrayList<>();
                        for (int i = 0; i < itemsMatchRange.size(); i++) {
                            debugMaterials2.add(itemsMatchRange.get(i).getItemStack().getType().name() + "x" + itemsMatchRange.get(i).getItemStack().getAmount());
                        }
                        MagicSpells.error("[" + h + "]range filter: " + String.join(",", debugMaterials2));
                    }
                    int counter = 0;
                    List<Item> needRemove = new ArrayList<>();
                    List<Integer> needRemoveCount = new ArrayList<>();
                    List<Boolean> bIsPredefine = new ArrayList<>(sIsPredefine);
                    List<ItemStack> bPredefine = new ArrayList<>(sPredefine);
                    List<Material> bMaterials = new ArrayList<>(sMaterials);
                    List<Integer> bCounts = new ArrayList<>(sCounts);
                    for (Item item : itemsMatchRange) {
                        if (tempDebug.get(h)) {
                            MagicSpells.error("[" + h + "," + item.getItemStack().getType().name() + "]bList size: b" + bIsPredefine.size() + " | p" + bPredefine.size() + " | m" + bMaterials.size() + " | c" + bCounts.size());
                            MagicSpells.error("[" + h + "," + item.getItemStack().getType().name() + "]needRemoveList size: i" + needRemove.size() + " | c" + needRemoveCount.size());
                        }
                        for (int j = 0; j < bMaterials.size(); j++) {
                            if (tempDebug.get(h)) {
                                MagicSpells.error("[" + h + "," + item.getItemStack().getType().name() + "," + j + "]if predefine: " + bIsPredefine.get(j));
                            }
                            boolean matchItemWithoutCount = false;
                            if (bIsPredefine.get(j)) {
                                if (tempDebug.get(h)) {
                                    MagicSpells.error("[" + h + "," + item.getItemStack().getType().name() + "," + j  + "]predefine match: " + item.getItemStack().isSimilar(bPredefine.get(j)));
                                }
                                if (item.getItemStack().isSimilar(bPredefine.get(j))) {
                                    matchItemWithoutCount = true;
                                }
                            } else {
                                if (tempDebug.get(h)) {
                                    MagicSpells.error("[" + h + "," + item.getItemStack().getType().name() + "," + j  + "]normal match: " + item.getItemStack().getType().equals(bMaterials.get(j)));
                                }
                                if (item.getItemStack().getType().equals(bMaterials.get(j))) {
                                    matchItemWithoutCount = true;
                                }
                            }
                            if (matchItemWithoutCount) {
                                if (tempDebug.get(h)) {
                                    MagicSpells.error("[" + h + "," + item.getItemStack().getType().name() + "," + j  + "]count: ITEMx" + item.getItemStack().getAmount() + " vs REQUIREx" + bCounts.get(j));
                                }
                                if (item.getItemStack().getAmount() == bCounts.get(j)) {
                                    needRemove.add(item);
                                    needRemoveCount.add(-1);
                                    counter++;
                                    bIsPredefine.remove(j);
                                    bPredefine.remove(j);
                                    bMaterials.remove(j);
                                    bCounts.remove(j);
                                    break;
                                } else if (item.getItemStack().getAmount() > bCounts.get(j)) {
                                    needRemove.add(item);
                                    needRemoveCount.add(bCounts.get(j));
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
                    if (counter == sMaterials.size()) {
                        if (tempDebug.get(h)) {
                            List<String> debugMaterials3 = new ArrayList<>();
                            for (int i = 0; i < sMaterials.size(); i++) {
                                if (sIsPredefine.get(i)) {
                                    debugMaterials3.add("PRE|" + sPredefine.get(i).getType().name() + "x" + sCounts.get(i));
                                } else {
                                    debugMaterials3.add(sMaterials.get(i).name() + "x" + sCounts.get(i));
                                }
                            }
                            MagicSpells.error("[" + h + "]remove: " + String.join(",", debugMaterials3));
                        }
                        for (int i = 0; i < needRemove.size(); i++) {
                            if (needRemoveCount.get(i) == -1) {
                                needRemove.get(i).remove();
                            } else {
                                ItemStack item = needRemove.get(i).getItemStack();
                                item.setAmount(item.getAmount() - needRemoveCount.get(i));
                                needRemove.get(i).setItemStack(item);
                            }
                        }
                        if (tempDebug.get(h)) {
                            MagicSpells.error("[" + h + "] =================================== Run successfully!");
                        }
                        return types.get(h);
                    }
                }
            }
        }
        return null;
    }
}
