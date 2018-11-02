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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

//
//  Note:       Spell args name are ignore case.
//
//  Example:    clickblocktypeinspectitem delPermCmd=manudelp %a <permission>
//  Result:     Init delete permission command format to use permission decide spells. It's only need register once, and can no more args with it.
//
//  Example:    clickblocktypeinspectitem id=0;pm=alchemy.all;b=CAULDRON:1,CAULDRON:2,CAULDRON:3;r=0;i=RAW_FISH:10
//  Result:     Id is 0. When the player has the permission of alchemy.all, in the cauldron with any water,
//              the range is extended by 0, throwing 10 raw fish, and the right cauldron will determine the success of the recipe,
//              after which the permission will be deleted.
//
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | Args Key                                                                      | Args Default Value                |
//  | Args Description                                                              |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | id                                                                            | null                              |
//  | The recipe unique id.                                                         |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | dp delpermcmd                                                                 | null                              |
//  | If you use perm arg, need create an empty skill use this arg to define a command format to delete perm.           |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | ac action                                                                     | 0                                 |
//  | Action shows player use left or right hand to touch block. 0 is right, 1 is left, 2 is any.                       |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | de debug debugmode                                                            | false                             |
//  | Turn on or off Debug Mode.                                                    |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | pm perm permission                                                            | alchemy.all                       |
//  | Player need have this perm to run this recipe, and will remove perm node when recipe success.                     |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | world worldname                                                               | null                              |
//  | If not null or '*', recipe will check player's world name is match, also can multi world like 'world1,world2'.    |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | b ab block blocks actionblock actionblocks                                    | null                              |
//  | You can use 'CAULDRON' to check all cauldron, or 'CAULDRON:0' to check empty cauldron.                            |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | m bm bmm mm mode modifymode blockmodifymode                                   | 0                                 |
//  | If 1 then block meta will decrease, 3 will decrease and break(some block can use this), 2 will increase.          |
//  | Support: CAULDRON SNOW(layer) ENDER_PORTAL_FRAME ANVIL PUMPKIN JACK_O_LANTERN                                     |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | mc modifychance                                                               | 0.0                               |
//  | Block modify chance, can more than 1.0.                                       |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | rm restrict restrictmode                                                      | false                             |
//  | If it is true, there must be no other items in the range to determine the correct recipe.                         |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | r ir range itemrange                                                          | 1.0                               |
//  | Item determination range. 1 is block self.                                    |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | no nomode nomatchmode                                                         | false                             |
//  | The item is not determined and is used to intercept results that are not matched to any recipe.                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | noc nomodeconsume nomatchmodeconsume                                          | false                             |
//  | Similar to NoMatchMode but will consume all items in the range.               |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | i pi item items pitem pitems preitem preitems predefineitem predefineitems    | null                              |
//  | Define the items for the recipe.                                              |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | f fu fire fireunder                                                           | false                             |
//  | Define if the heat source block (fire/lava/magma) is needed below the interaction block.                          |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | ff fireblock fireblockchance                                                  | 0.25                              |
//  | If the heat source is a fire block, its chances of extinguishing.             |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | fl lava lavablock lavablockchance                                             | 0.05                              |
//  | If the heat source is a lava square, its chances of becoming cobblestone.     |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | fm magma magmablock magmablockchance                                          | 0.00                              |
//  | If the heat source is an magma square, his chances of becoming a netherbrick. |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | exp expval expvalue experience experiencevalue                                | -1                                |
//  | If greater than -1, an experience ball is generated when the delay is specified.                                  |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | expc expcount                                                                 | 3                                 |
//  | Specify the number of experience balls.                                       |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | exps expstart expts exptimerstart                                             | 40                                |
//  | After this delay, the experience ball is generated.                           |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//  | expe expend expte exptimerend                                                 | 60                                |
//  | The delay is generated when the experience ball ends.                         |                                   |
//  +-------------------------------------------------------------------------------+-----------------------------------+
//

public class RightClickBlockTypeInspectItemListener extends PassiveListener {

    private List<RightClickBlockTypeInspectItemListenerClass> recipes = new ArrayList<>();
    private String deletePermissionCommand = "";
    private String moduleLogPrefix = "[RCBTIIL]";

    @Override
    public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
        RightClickBlockTypeInspectItemListenerClass recipe = new RightClickBlockTypeInspectItemListenerClass();
        String[] args = var.split(";");
        boolean continueBeacuseUseDelPermCmdArg = false;
        for (String arg : args) {
            String[] kvList = arg.split("=");
            if (kvList.length != 2) { MagicSpells.error(String.format("%s In register %s has more or less than one equal key/value split char.", moduleLogPrefix, var)); continue; }
            int recipeId = -1;
            switch (kvList[0].toLowerCase()) {
                case "dp":
                case "delpermcmd": {
                    deletePermissionCommand = kvList[1];
                    continueBeacuseUseDelPermCmdArg = true;
                    break;
                }
                case "id": {
                    try {
                        recipeId = Integer.parseInt(kvList[1]);
                        if (recipeId < 0) { MagicSpells.error(String.format("%s In register %s seems id is smaller than zero.", moduleLogPrefix, var)); continue; }
                        recipe.setId(recipeId);
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register %s seems id is not an integer.", moduleLogPrefix, var)); continue; }
                    break;
                }
                case "ac":
                case "action": {
                    recipe.setAction(kvList[1].equals("0") ? RightClickBlockTypeInspectItemListenerEnumAction.Action0_RightClickBlock :
                            (kvList[1].equals("1") ? RightClickBlockTypeInspectItemListenerEnumAction.Action1_LeftClickBlock :
                                    RightClickBlockTypeInspectItemListenerEnumAction.Action2_RightOrLeftClickBlock));
                    break;
                }
                case "de":
                case "debug":
                case "debugmode": {
                    recipe.setDebugFlag(kvList[1].equals("1"));
                    break;
                }
                case "pm":
                case "perm":
                case "permission": {
                    recipe.setPermissionString(kvList[1]);
                    break;
                }
                case "w":
                case "world":
                case "worldname": {
                    recipe.setWorldName(Arrays.asList(kvList[1].split(",")));
                    break;
                }
                case "b":
                case "ab":
                case "block":
                case "blocks":
                case "actionblock":
                case "actionblocks": {
                    String[] split = kvList[1].split(",");
                    List<MagicMaterial> nowBlocks = new ArrayList<>();
                    for (String s : split) {
                        MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
                        if (m != null) {
                            nowBlocks.add(m);
                        } else { MagicSpells.error(String.format("%s In register #%d seems action blocks %s can't find.", moduleLogPrefix, recipeId, s)); continue; }
                    }
                    recipe.setActionBlocks(nowBlocks);
                    break;
                }
                case "m":
                case "bm":
                case "bmm":
                case "mm":
                case "mode":
                case "modifymode":
                case "blockmodifymode": {
                    recipe.setBlockModifyMode(kvList[1].equals("3") ? RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode3_ReduceAndDestroyMeta :
                            (kvList[1].equals("2") ? RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode2_IncreaseMeta :
                                    (kvList[1].equals("1") ? RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode1_ReduceMeta :
                                            RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode0_NoModify)));
                    break;
                }
                case "mc":
                case "modifychance": {
                    try {
                        recipe.setBlockModifyChance(Float.parseFloat(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems block modify chance is not a float number.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "rm":
                case "restrict":
                case "restrictmode": {
                    recipe.setItemRangeRestrictMode(kvList[1].equals("1"));
                    break;
                }
                case "r":
                case "ir":
                case "range":
                case "itemrange": {
                    try {
                        recipe.setItemRange(Float.parseFloat(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems item range is not a float number.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "no":
                case "nomode":
                case "nomatchmode": {
                    recipe.setNoItemsMatchMode(kvList[1].equals("1"));
                    break;
                }
                case "noc":
                case "nomodeconsume":
                case "nomatchmodeconsume": {
                    recipe.setNoItemsMatchButConsumeMode(kvList[1].equals("1"));
                    break;
                }
                case "i":
                case "pi":
                case "item":
                case "items":
                case "pitem":
                case "pitems":
                case "preitem":
                case "preitems":
                case "predefineitem":
                case "predefineitems": {
                    String[] split = kvList[1].split(",");
                    if (kvList[1].length() > 0 && split.length > 0) {
                        List<Boolean> tempPredefineItems = new ArrayList<>();
                        List<ItemStack> tempMatchMagicItems = new ArrayList<>();
                        List<Material> tempMatchNormalItems = new ArrayList<>();
                        List<Integer> tempMatchNormalItemsCount = new ArrayList<>();
                        for (String item : split) {
                            String[] sp = item.split(":");
                            int amount = 1;
                            try {
                                if (sp.length > 1 && Integer.parseInt(sp[1]) >= 0) {
                                    amount = Integer.parseInt(sp[1]);
                                }
                            } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems item amount is not an integer: %s, spell still loaded.", moduleLogPrefix, recipeId, item)); continue; }
                            ItemStack isa = Util.getItemStackFromString(sp[0]);
                            if (isa == null) {
                                String itemName = sp[0];
                                boolean forceItemStack = false;
                                if (itemName.length() > 4 && itemName.toLowerCase().substring(0, 4).equals("stack|")) {
                                    itemName = itemName.substring(4, itemName.length() - 4);
                                    forceItemStack = true;
                                }
                                MagicMaterial m = MagicSpells.getItemNameResolver().resolveItem(itemName);
                                if (m == null) { m = MagicSpells.getItemNameResolver().resolveBlock(itemName); }
                                if (m == null) {
                                    MagicSpells.error(String.format("%s In register #%d seems item id is not a correctly id: %s, spell still loaded.", moduleLogPrefix, recipeId, item)); continue;
                                } else {
                                    if (forceItemStack) {
                                        tempPredefineItems.add(true);
                                        tempMatchMagicItems.add(m.toItemStack(amount));
                                        tempMatchNormalItems.add(null);
                                        tempMatchNormalItemsCount.add(null);
                                    } else {
                                        tempPredefineItems.add(false);
                                        tempMatchMagicItems.add(null);
                                        tempMatchNormalItems.add(m.getMaterial());
                                        tempMatchNormalItemsCount.add(amount);
                                    }
                                }
                            } else {
                                isa.setAmount(amount);
                                tempPredefineItems.add(true);
                                tempMatchMagicItems.add(isa);
                                tempMatchNormalItems.add(null);
                                tempMatchNormalItemsCount.add(null);
                            }
                        }
                        recipe.setPredefineItems(tempPredefineItems);
                        recipe.setMatchMagicItems(tempMatchMagicItems);
                        recipe.setMatchNormalItems(tempMatchNormalItems);
                        recipe.setMatchNormalItemsCount(tempMatchNormalItemsCount);
                    }
                    break;
                }
                case "f":
                case "fu":
                case "fire":
                case "fireunder": {
                    recipe.setFireBlockUnder(kvList[1].equals("1"));
                    break;
                }
                case "ff":
                case "fireblock":
                case "fireblockchance": {
                    try {
                        recipe.setFireBlockExtinguishChance_fire(Float.parseFloat(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems fire block chance is not a float.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "fl":
                case "lava":
                case "lavablock":
                case "lavablockchance": {
                    try {
                        recipe.setFireBlockExtinguishChance_lava(Float.parseFloat(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems lava block chance is not a float.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "fm":
                case "magma":
                case "magmablock":
                case "magmablockchance": {
                    try {
                        recipe.setFireBlockExtinguishChance_magma(Float.parseFloat(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems magma block chance is not a float.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "exp":
                case "expval":
                case "expvalue":
                case "experience":
                case "experiencevalue": {
                    try {
                        recipe.setGiveExpValue(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems exp give value is not an integer.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "expc":
                case "expcount": {
                    try {
                        recipe.setGiveExpCount(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems exp split amount is not an integer.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "exps":
                case "expstart":
                case "expts":
                case "exptimerstart": {
                    try {
                        recipe.setGiveExpTimerStart(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems exp timer start setting is not an integer.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
                case "expe":
                case "expend":
                case "expte":
                case "exptimerend": {
                    try {
                        recipe.setGiveExpTimerEnd(Integer.parseInt(kvList[1]));
                    } catch (Exception e) { MagicSpells.error(String.format("%s In register #%d seems exp timer end setting is not an integer.", moduleLogPrefix, recipeId)); continue; }
                    break;
                }
            }
            if (recipeId == -1) continue;
            List<PassiveSpell> spellList = Arrays.asList(spell);
            recipe.setSkills(spellList);
        }
        if (!continueBeacuseUseDelPermCmdArg) {
            if (recipe.isValid()) {
                recipes.add(recipe);
            } else {
                MagicSpells.error(String.format("%s #%d is invalied, unloaded. Turn on the debug flag to get more info.", moduleLogPrefix, recipe.getId()));
            }
            Collections.sort(recipes);
        }
    }

    @OverridePriority
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        RightClickBlockTypeInspectItemListenerEnumAction action = event.getAction() == Action.LEFT_CLICK_BLOCK ? RightClickBlockTypeInspectItemListenerEnumAction.Action1_LeftClickBlock :
                RightClickBlockTypeInspectItemListenerEnumAction.Action0_RightClickBlock;
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

    private List<PassiveSpell> getSpells(Block block, Location location, RightClickBlockTypeInspectItemListenerEnumAction action, Player player) {
        for (RightClickBlockTypeInspectItemListenerClass recipe : recipes) {
            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] === Start process ===============================================", recipe.getId()));
            boolean actionOk = false;
            if (recipe.getAction() == RightClickBlockTypeInspectItemListenerEnumAction.Action2_RightOrLeftClickBlock) actionOk = true;
            else if (recipe.getAction() == action) actionOk = true;
            if (actionOk) {
                boolean permOk = false;
                if (!recipe.isNeedPermission()) permOk = true; else if (player.hasPermission(recipe.getPermissionString())) permOk = true;
                if (permOk) {
                    boolean worldOk = false;
                    if (recipe.getWorldName().size() > 0) {
                        String worldName = location.getWorld().getName();
                        if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] World name matching: %s ? %s", recipe.getId(), recipe.getWorldName(), worldName));
                        for (String world : recipe.getWorldName()) {
                            if ("*".equals(world)) {
                                worldOk = true;
                                break;
                            } else if (world.equals(worldName)) {
                                worldOk = true;
                                break;
                            }
                        }
                    } else worldOk = true;
                    if (worldOk) {
                        boolean blockTypeOk = false;
                        for (MagicMaterial actionBlock : recipe.getActionBlocks()) {
                            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Player block: %s:%d, require block: %s:%d", recipe.getId(), block.getType(), block.getState().getData().getData(), actionBlock.getMaterial().name(), actionBlock.getMaterialData().getData()));
                            if (actionBlock.equals(block.getState().getData())) {
                                blockTypeOk = true;
                                boolean fireUnderOk = false;
                                Block blockUnder = block.getWorld().getBlockAt(block.getLocation().add(0, -1, 0));
                                if (recipe.isFireBlockUnder()) {
                                    if (blockUnder.getType() == Material.FIRE || blockUnder.getType() == Material.LAVA || blockUnder.getType() == Material.STATIONARY_LAVA || blockUnder.getType() == Material.MAGMA) {
                                        fireUnderOk = true;
                                    }
                                } else {
                                    if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Fire block under is not required, skip.", recipe.getId()));
                                    fireUnderOk = true;
                                }
                                if (fireUnderOk) {
                                    boolean itemsMatch = false;
                                    List<Entity> entitiesNeedRemove = new ArrayList<>();
                                    if (recipe.isNoItemsMatchMode()) {
                                        itemsMatch = true;
                                    } else if (recipe.isNoItemsMatchButConsumeMode()) {
                                        List<Entity> entities = new ArrayList<>(location.getWorld().getNearbyEntities(location, recipe.getItemRange(), recipe.getItemRange(), recipe.getItemRange()));
                                        for (Entity entity : entities) {
                                            entity.remove();
                                        }
                                        itemsMatch = true;
                                    } else {
                                        List<Entity> entities = new ArrayList<>(location.getWorld().getNearbyEntities(location, recipe.getItemRange(), recipe.getItemRange(), recipe.getItemRange()));
                                        if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] x %d, y %d, z %d, r %f, e %d", recipe.getId(), block.getX(), block.getY(), block.getZ(), recipe.getItemRange(), entities.size()));
                                        List<Boolean> tempPlaceholder = new ArrayList<>(recipe.getPredefineItems());
                                        if (recipe.getPredefineItems().size() > 0 && entities.size() > 0) {
                                            for (int i = recipe.getPredefineItems().size() - 1; i >= 0; i--) {
                                                for (int j = entities.size() - 1; j >= 0; j--) {
                                                    if (entities.get(j) instanceof Item) {
                                                        Item tempItem = (Item) entities.get(j);
                                                        if (recipe.getPredefineItems().get(i)) {
                                                            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Predefine item matching: %dx%s ? %dx%s", recipe.getId(), recipe.getMatchMagicItems().get(i).getAmount(), recipe.getMatchMagicItems().get(i).getType(), tempItem.getItemStack().getAmount(), tempItem.getItemStack().getType()));
                                                            if (tempItem.getItemStack().isSimilar(recipe.getMatchMagicItems().get(i)) && tempItem.getItemStack().getAmount() >= recipe.getMatchMagicItems().get(i).getAmount()) {
                                                                entitiesNeedRemove.add(entities.get(j));
                                                                entities.remove(j);
                                                                tempPlaceholder.remove(i);
                                                            }
                                                        } else {
                                                            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Vanilla item matching: %dx%s ? %dx%s", recipe.getId(), recipe.getMatchNormalItemsCount().get(i), recipe.getMatchNormalItems().get(i), tempItem.getItemStack().getAmount(), tempItem.getItemStack().getType()));
                                                            if (tempItem.getItemStack().getType().equals(recipe.getMatchNormalItems().get(i)) && tempItem.getItemStack().getAmount() >= recipe.getMatchNormalItemsCount().get(i)) {
                                                                entitiesNeedRemove.add(entities.get(j));
                                                                entities.remove(j);
                                                                tempPlaceholder.remove(i);
                                                            }
                                                        }
                                                    } else entities.remove(entities.get(j));
                                                }
                                            }
                                        }
                                        if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Item matching list size: %d entities, %d need remove, %d placeholders.", recipe.getId(), entities.size(), entitiesNeedRemove.size(), tempPlaceholder.size()));
                                        if (tempPlaceholder.size() == 0) {
                                            if (recipe.isItemRangeRestrictMode()) {
                                                if (entities.size() == 0) {
                                                    itemsMatch = true;
                                                }
                                            } else {
                                                itemsMatch = true;
                                            }
                                        }
                                    }
                                    if (itemsMatch) {
                                        if (entitiesNeedRemove.size() > 0) {
                                            for (int i = entitiesNeedRemove.size() - 1; i >= 0; i--) {
                                                entitiesNeedRemove.get(i).remove();
                                            }
                                        }
                                        if (recipe.isNeedPermission()) {
                                            MagicSpells.plugin.getServer().dispatchCommand(MagicSpells.plugin.getServer().getConsoleSender(), deletePermissionCommand.replace("%a", player.getName()).replace("<permission>", recipe.getPermissionString()));
                                            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Permission %s removed.", recipe.getId(), recipe.getPermissionString()));
                                        }
                                        boolean fireUnderCheat = false;
                                        if (recipe.isFireBlockUnder()) {
                                            int rdm = new Random().nextInt(100);
                                            boolean alive = true;
                                            String str = MagicSpells.plugin.getMagicConfig().getString("general.str-icy-fire-extinguished", "");
                                            if (blockUnder.getType() == Material.FIRE) {
                                                if (rdm < recipe.getFireBlockExtinguishChance_fire() * 100) {
                                                    blockUnder.breakNaturally();
                                                    if (!str.isEmpty()) player.sendMessage(str);
                                                    alive = false;
                                                }
                                            } else if (blockUnder.getType() == Material.LAVA || blockUnder.getType() == Material.STATIONARY_LAVA) {
                                                if (rdm < recipe.getFireBlockExtinguishChance_lava() * 100) {
                                                    blockUnder.breakNaturally();
                                                    blockUnder.setType(Material.COBBLESTONE);
                                                    if (!str.isEmpty()) player.sendMessage(str);
                                                    alive = false;
                                                }
                                            } else if (blockUnder.getType() == Material.MAGMA) {
                                                if (rdm < recipe.getFireBlockExtinguishChance_magma() * 100) {
                                                    blockUnder.breakNaturally();
                                                    blockUnder.setType(Material.NETHER_BRICK);
                                                    if (!str.isEmpty()) player.sendMessage(str);
                                                    alive = false;
                                                }
                                            } else fireUnderCheat = true;
                                            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Fire random: %d/100 | %s", recipe.getId(), rdm, alive ? "alive" : "extinguished"));
                                        }
                                        if (!fireUnderCheat) {
                                            if (recipe.getBlockModifyMode() != RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode0_NoModify) {
                                                int originalMeta = block.getState().getData().getData();
                                                float chance = recipe.getBlockModifyChance();
                                                int multi = (int) Math.floor(chance);
                                                chance -= multi;
                                                if (new Random().nextInt(100) < chance * 100) {
                                                    multi++;
                                                }
                                                for (int i = 0; i < multi; i++) {
                                                    if (block.getState().getType().equals(Material.CAULDRON)) {
                                                        if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode1_ReduceMeta) {
                                                            if (block.getState().getData().getData() > 0) {
                                                                block.setData((byte) (block.getState().getData().getData() - 1));
                                                            }
                                                        } else if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode2_IncreaseMeta) {
                                                            if (block.getState().getData().getData() < 3) {
                                                                block.setData((byte) (block.getState().getData().getData() + 1));
                                                            }
                                                        }
                                                    } else if (block.getState().getType().equals(Material.SNOW)) {
                                                        if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode1_ReduceMeta || recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode3_ReduceAndDestroyMeta) {
                                                            if (block.getState().getData().getData() > 1) {
                                                                block.setData((byte) (block.getState().getData().getData() - 1));
                                                            } else if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode3_ReduceAndDestroyMeta) {
                                                                block.setType(Material.AIR);
                                                            }
                                                        } else if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode2_IncreaseMeta) {
                                                            if (block.getState().getData().getData() < 8) {
                                                                block.setData((byte) (block.getState().getData().getData() + 1));
                                                            }
                                                        }
                                                    } else if (block.getState().getType().equals(Material.ENDER_PORTAL_FRAME)) {
                                                        if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode1_ReduceMeta) {
                                                            switch (block.getState().getData().getData()) {
                                                                case 4:
                                                                    block.setData((byte) 0);
                                                                    break;
                                                                case 5:
                                                                    block.setData((byte) 1);
                                                                    break;
                                                                case 6:
                                                                    block.setData((byte) 2);
                                                                    break;
                                                                case 7:
                                                                    block.setData((byte) 3);
                                                                    break;
                                                            }
                                                        } else if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode2_IncreaseMeta) {
                                                            switch (block.getState().getData().getData()) {
                                                                case 0:
                                                                    block.setData((byte) 4);
                                                                    break;
                                                                case 1:
                                                                    block.setData((byte) 5);
                                                                    break;
                                                                case 2:
                                                                    block.setData((byte) 6);
                                                                    break;
                                                                case 3:
                                                                    block.setData((byte) 7);
                                                                    break;
                                                            }
                                                        }
                                                    } else if (block.getState().getType().equals(Material.ANVIL)) {
                                                        if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode1_ReduceMeta || recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode3_ReduceAndDestroyMeta) {
                                                            switch (block.getState().getData().getData()) {
                                                                case 0:
                                                                    block.setData((byte) 4);
                                                                    break;
                                                                case 1:
                                                                    block.setData((byte) 5);
                                                                    break;
                                                                case 2:
                                                                    block.setData((byte) 6);
                                                                    break;
                                                                case 3:
                                                                    block.setData((byte) 7);
                                                                    break;
                                                                case 4:
                                                                    block.setData((byte) 8);
                                                                    break;
                                                                case 5:
                                                                    block.setData((byte) 9);
                                                                    break;
                                                                case 6:
                                                                    block.setData((byte) 10);
                                                                    break;
                                                                case 7:
                                                                    block.setData((byte) 11);
                                                                    break;
                                                            }
                                                            if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode3_ReduceAndDestroyMeta) {
                                                                switch (block.getState().getData().getData()) {
                                                                    case 8:
                                                                    case 9:
                                                                    case 10:
                                                                    case 11:
                                                                        block.setType(Material.AIR);
                                                                        break;
                                                                }
                                                            }
                                                        } else if (recipe.getBlockModifyMode() == RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode2_IncreaseMeta) {
                                                            switch (block.getState().getData().getData()) {
                                                                case 11:
                                                                    block.setData((byte) 7);
                                                                    break;
                                                                case 10:
                                                                    block.setData((byte) 6);
                                                                    break;
                                                                case 9:
                                                                    block.setData((byte) 5);
                                                                    break;
                                                                case 8:
                                                                    block.setData((byte) 4);
                                                                    break;
                                                                case 7:
                                                                    block.setData((byte) 3);
                                                                    break;
                                                                case 6:
                                                                    block.setData((byte) 2);
                                                                    break;
                                                                case 5:
                                                                    block.setData((byte) 1);
                                                                    break;
                                                                case 4:
                                                                    block.setData((byte) 0);
                                                                    break;
                                                            }
                                                        }
                                                    } else if (block.getState().getType().equals(Material.PUMPKIN)) {
                                                        block.setType(Material.JACK_O_LANTERN);
                                                    } else if (block.getState().getType().equals(Material.JACK_O_LANTERN)) {
                                                        block.setType(Material.PUMPKIN);
                                                    }
                                                }
                                                if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Block meta changed from %d to %d.", recipe.getId(), originalMeta, block.getState().getData().getData()));
                                            }
                                            if (recipe.getGiveExpValue() >= 0) {
                                                int delayMulti = (recipe.getGiveExpTimerEnd() - recipe.getGiveExpTimerStart()) / recipe.getGiveExpCount();
                                                Location locadd = block.getLocation();
                                                locadd.setY(locadd.getBlockY() + 1);
                                                locadd.setX(locadd.getBlockX() + 0.5);
                                                locadd.setZ(locadd.getBlockZ() + 0.5);
                                                for (int i = 0; i < recipe.getGiveExpCount(); i++) {
                                                    if (i == recipe.getGiveExpCount() - 1) {
                                                        MagicSpells.plugin.getServer().getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, () -> {
                                                            ExperienceOrb exporb = (ExperienceOrb) block.getWorld().spawnEntity(locadd, EntityType.EXPERIENCE_ORB);
                                                            exporb.setExperience(recipe.getGiveExpValue());
                                                        }, recipe.getGiveExpTimerStart() + (i * delayMulti));
                                                    } else {
                                                        MagicSpells.plugin.getServer().getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, () -> {
                                                            ExperienceOrb exporb = (ExperienceOrb) block.getWorld().spawnEntity(locadd, EntityType.EXPERIENCE_ORB);
                                                            exporb.setExperience(0);
                                                        }, recipe.getGiveExpTimerStart() + (i * delayMulti));
                                                    }
                                                }
                                                if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Summon %d orbs in %d ticks, all of them are %d exp.", recipe.getId(), recipe.getGiveExpCount(), recipe.getGiveExpTimerEnd() - recipe.getGiveExpTimerStart(), recipe.getGiveExpValue()));
                                            }
                                            if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] =========================================== Run successfully! ===", recipe.getId()));
                                            return recipe.getSkills();
                                        } else if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] Run failed item consumed because who remove fire block first! ===", recipe.getId()));
                                    } else if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] ===================== Run failed because items are not match! ===", recipe.getId()));
                                } else if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] ========== Run failed because need any fire block under that! ===", recipe.getId()));
                                break;
                            }
                        } if (!blockTypeOk) if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] ========= Run failed because clicked block type is not match! ===", recipe.getId()));
                    } else if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] ================= Run failed because world name is not match! ===", recipe.getId()));
                } else if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] ============= Run failed because permission player not have! ===", recipe.getId()));
            } else if (recipe.isDebugFlag()) MagicSpells.error(String.format("[%d] ===================== Run failed because action is not equal! ===", recipe.getId()));
        }
        return null;
    }

    private class RightClickBlockTypeInspectItemListenerClass implements Comparable<RightClickBlockTypeInspectItemListenerClass> {
        private int id = -1;
        private RightClickBlockTypeInspectItemListenerEnumAction action = RightClickBlockTypeInspectItemListenerEnumAction.Action0_RightClickBlock;
        private boolean debugFlag = false;
        private boolean needPermission = false;
        private String permissionString = "alchemy.all";
        private List<String> worldName = new ArrayList<>();
        private List<MagicMaterial> actionBlocks = new ArrayList<>();
        private RightClickBlockTypeInspectItemListenerEnumBlockModifyMode blockModifyMode = RightClickBlockTypeInspectItemListenerEnumBlockModifyMode.Mode0_NoModify;
        private float blockModifyChance = 0.0f;
        private float itemRange = 1.0f;
        private boolean itemRangeRestrictMode = false;
        private boolean noItemsMatchMode = false;
        private boolean noItemsMatchButConsumeMode = false;
        private List<Boolean> predefineItems = new ArrayList<>();
        private List<ItemStack> matchMagicItems = new ArrayList<>();
        private List<Material> matchNormalItems = new ArrayList<>();
        private List<Integer> matchNormalItemsCount = new ArrayList<>();
        private boolean fireBlockUnder = false;
        private float fireBlockExtinguishChance_fire = 0.25f;
        private float fireBlockExtinguishChance_lava = 0.05f;
        private float fireBlockExtinguishChance_magma = 0.00f;
        private int giveExpValue = -1;
        private int giveExpCount = 3;
        private int giveExpTimerStart = 40;
        private int giveExpTimerEnd = 60;
        private List<PassiveSpell> skills = new ArrayList<>();

        @Override
        public int compareTo(RightClickBlockTypeInspectItemListenerClass c) {
            if (this.id < c.getId()) {
                return -1;
            } else if (this.id > c.getId()) {
                return 1;
            }
            return 0;
        }

        public boolean isValid() {
            boolean valid = true;
            if (id < 0) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s A recipe has invalid id, so remove it.", moduleLogPrefix));
            }
            if (actionBlocks.size() == 0) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The interaction blocks array of recipe #%d is empty, so unload it.", moduleLogPrefix, id));
            }
            if (blockModifyChance < 0) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The block modify chance is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (itemRange < 0 || itemRange > 32) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The item decide range is negative or more than 32 in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (predefineItems.size() == 0 && debugFlag) {
                MagicSpells.error(String.format("%s The cost items array of recipe #%d is empty, if this is an empty recipe for settings, you can ignored this message.", moduleLogPrefix, id));
            }
            if (matchMagicItems.size() != matchNormalItems.size() || matchMagicItems.size() != predefineItems.size() || matchNormalItems.size() != predefineItems.size()) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The item decide range is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (fireBlockExtinguishChance_fire < 0 || fireBlockExtinguishChance_fire > 1) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The fire block extinguish chance is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (fireBlockExtinguishChance_lava < 0 || fireBlockExtinguishChance_lava > 1) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The lava block extinguish chance is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (fireBlockExtinguishChance_magma < 0 || fireBlockExtinguishChance_magma > 1) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The magma block extinguish chance is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (giveExpValue < -1) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The exp summon value is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (giveExpCount <= 0) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The exp split amount is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            if (giveExpTimerStart <= 0 || giveExpTimerEnd <= giveExpTimerStart) {
                valid = false;
                if (debugFlag) MagicSpells.error(String.format("%s The exp give time setting is invalid in recipe #%d, so unload it.", moduleLogPrefix, id));
            }
            return valid;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public RightClickBlockTypeInspectItemListenerEnumAction getAction() {
            return action;
        }

        public void setAction(RightClickBlockTypeInspectItemListenerEnumAction action) {
            this.action = action;
        }

        public boolean isDebugFlag() {
            return debugFlag;
        }

        public void setDebugFlag(boolean debugFlag) {
            this.debugFlag = debugFlag;
        }

        public boolean isNeedPermission() {
            return needPermission;
        }

        public String getPermissionString() {
            return permissionString;
        }

        public void setPermissionString(String permissionString) {
            this.permissionString = permissionString;
            needPermission = true;
        }

        public List<String> getWorldName() {
            return worldName;
        }

        public void setWorldName(List<String> worldName) {
            this.worldName = worldName;
        }

        public List<MagicMaterial> getActionBlocks() {
            return actionBlocks;
        }

        public void setActionBlocks(List<MagicMaterial> actionBlocks) {
            this.actionBlocks = actionBlocks;
        }

        public RightClickBlockTypeInspectItemListenerEnumBlockModifyMode getBlockModifyMode() {
            return blockModifyMode;
        }

        public void setBlockModifyMode(RightClickBlockTypeInspectItemListenerEnumBlockModifyMode blockModifyMode) {
            this.blockModifyMode = blockModifyMode;
        }

        public float getBlockModifyChance() {
            return blockModifyChance;
        }

        public void setBlockModifyChance(float blockModifyChance) {
            this.blockModifyChance = blockModifyChance;
        }

        public float getItemRange() {
            return itemRange;
        }

        public void setItemRange(float itemRange) {
            this.itemRange = itemRange;
        }

        public boolean isItemRangeRestrictMode() {
            return itemRangeRestrictMode;
        }

        public void setItemRangeRestrictMode(boolean itemRangeRestrictMode) {
            this.itemRangeRestrictMode = itemRangeRestrictMode;
        }

        public boolean isNoItemsMatchMode() {
            return noItemsMatchMode;
        }

        public void setNoItemsMatchMode(boolean noItemsMatchMode) {
            this.noItemsMatchMode = noItemsMatchMode;
        }

        public boolean isNoItemsMatchButConsumeMode() {
            return noItemsMatchButConsumeMode;
        }

        public void setNoItemsMatchButConsumeMode(boolean noItemsMatchButConsumeMode) {
            this.noItemsMatchButConsumeMode = noItemsMatchButConsumeMode;
        }

        public List<Boolean> getPredefineItems() {
            return predefineItems;
        }

        public void setPredefineItems(List<Boolean> predefineItems) {
            this.predefineItems = predefineItems;
        }

        public List<ItemStack> getMatchMagicItems() {
            return matchMagicItems;
        }

        public void setMatchMagicItems(List<ItemStack> matchMagicItems) {
            this.matchMagicItems = matchMagicItems;
        }

        public List<Material> getMatchNormalItems() {
            return matchNormalItems;
        }

        public void setMatchNormalItems(List<Material> matchNormalItems) {
            this.matchNormalItems = matchNormalItems;
        }

        public List<Integer> getMatchNormalItemsCount() {
            return matchNormalItemsCount;
        }

        public void setMatchNormalItemsCount(List<Integer> matchNormalItemsCount) {
            this.matchNormalItemsCount = matchNormalItemsCount;
        }

        public boolean isFireBlockUnder() {
            return fireBlockUnder;
        }

        public void setFireBlockUnder(boolean fireBlockUnder) {
            this.fireBlockUnder = fireBlockUnder;
        }

        public float getFireBlockExtinguishChance_fire() {
            return fireBlockExtinguishChance_fire;
        }

        public void setFireBlockExtinguishChance_fire(float fireBlockExtinguishChance_fire) {
            this.fireBlockExtinguishChance_fire = fireBlockExtinguishChance_fire;
        }

        public float getFireBlockExtinguishChance_lava() {
            return fireBlockExtinguishChance_lava;
        }

        public void setFireBlockExtinguishChance_lava(float fireBlockExtinguishChance_lava) {
            this.fireBlockExtinguishChance_lava = fireBlockExtinguishChance_lava;
        }

        public float getFireBlockExtinguishChance_magma() {
            return fireBlockExtinguishChance_magma;
        }

        public void setFireBlockExtinguishChance_magma(float fireBlockExtinguishChance_magma) {
            this.fireBlockExtinguishChance_magma = fireBlockExtinguishChance_magma;
        }

        public int getGiveExpValue() {
            return giveExpValue;
        }

        public void setGiveExpValue(int giveExpValue) {
            this.giveExpValue = giveExpValue;
        }

        public int getGiveExpCount() {
            return giveExpCount;
        }

        public void setGiveExpCount(int giveExpCount) {
            this.giveExpCount = giveExpCount;
        }

        public int getGiveExpTimerStart() {
            return giveExpTimerStart;
        }

        public void setGiveExpTimerStart(int giveExpTimerStart) {
            this.giveExpTimerStart = giveExpTimerStart;
        }

        public int getGiveExpTimerEnd() {
            return giveExpTimerEnd;
        }

        public void setGiveExpTimerEnd(int giveExpTimerEnd) {
            this.giveExpTimerEnd = giveExpTimerEnd;
        }

        public List<PassiveSpell> getSkills() {
            return skills;
        }

        public void setSkills(List<PassiveSpell> skills) {
            this.skills = skills;
        }
    }

    private enum RightClickBlockTypeInspectItemListenerEnumAction {
        Action0_RightClickBlock,
        Action1_LeftClickBlock,
        Action2_RightOrLeftClickBlock,
    }

    private enum RightClickBlockTypeInspectItemListenerEnumBlockModifyMode {
        Mode0_NoModify,
        Mode1_ReduceMeta,
        Mode2_IncreaseMeta,
        Mode3_ReduceAndDestroyMeta,
    }
}
