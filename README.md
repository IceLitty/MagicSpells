# All modifies from ThrComputerGeek2 project:
1) Add `%t`(target) names to `ActionBarTextEffect` and `TitleEffect`.
2) In BlockBreakEffect, use `world.spawnParticle()` function to replace `world.playEffect()` function.

   And support block meta value like wheat 0-7, format:
   ```yaml
   Glyph:
        position: target
        effect: blockbreak
        id: 59
        meta: 7```
3) Add new passive listener: `clickblocktypeinspectitem`.

   Use format please view this: https://github.com/IceLitty/MagicSpells/blob/master/src/com/nisovin/magicspells/spells/passive/RightClickBlockTypeInspectItemListener.java#L22-L107
   
   Like this:
   ```yaml
    # We need define a command how to remove a player's permission.
    alchemy_empty_deperm:
      spell-class: ".PassiveSpell"
      triggers:
        - clickblocktypeinspectitem delPermCmd=manudelp %a <permission>
      spells:
        - alchemy_empty_failed_item   # any spell you can, not run but magicspells need at least once in passive spell.
    # We want to add a recipe use 10 raw fishes to get an exp bottle use stone.
    alchemy_recipe_stone:
      spell-class: ".PassiveSpell"
      triggers:
        - clickblocktypeinspectitem id=00100;ac=2;pm=alchemy.all;f=1;b=CAULDRON:1,CAULDRON:2,CAULDRON:3;i=RAW_FISH:10;m=1;mc=0.33
      delay: 60
      chance: 100
      cooldown: 0
      spells:
        - alchemy_conjure_stone
        - alchemy_add_perm
    # Need a conjure spell to conjure items to block location.
    alchemy_conjure_stone:
      spell-class: ".instant.ConjureSpell"
      add-to-inventory: false
      items:
        - stone 1 100%
    # We also need a fail spell to receive if player try an unrecorded recipe.
    alchemy_empty_failed:
      spell-class: ".PassiveSpell"
      triggers:
        - clickblocktypeinspectitem id=99999;pm=alchemy.all;b=CAULDRON:1,CAULDRON:2,CAULDRON:3;i=air:0;m=1;mc=0;noc=1
      delay: 40
      chance: 100
      cooldown: 0
      spells:
        - alchemy_empty_failed_item     # like give player an coal or empty.
        - alchemy_add_perm              # also need add perm back, because if recipe is not match, spell will not called.
    # We need add permission back to let player use alchemy system again.
    alchemy_add_perm:
      spell-class: ".ExternalCommandSpell"
      execute-on-console-instead: true
      command-to-execute:
        - manuaddp %a alchemy.all
   ```
   
   You can add more and more recipes and only need once setting deperm & addperm spell.
   
   And recipe success spells can add a DummySpell to play particle on block.
   
   Id is special for each receipe, can't repeat, because receipe is compare by id order!
   
   More arguments and description are in upper link.
   
