package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoonPhaseCondition extends Condition {

	String phaseName = "";

	@Override
	public boolean setVar(String var) {
		phaseName = var.toLowerCase();
		return true;
	}
	
	@Override
	public boolean check(Player player) {
		return check(player, player.getLocation());
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		return check(player, target.getLocation());
	}
	
	@Override
	public boolean check(Player player, Location location) {
        Pattern pattern = Pattern.compile("[0-9]{1}");
        Matcher isNum = pattern.matcher(phaseName);
        Pattern pattern2 = Pattern.compile("[0-9]{1}-[0-9]{1}");
        Matcher isNum2 = pattern2.matcher(phaseName);
        if (isNum2.matches()) {
            int a = Integer.parseInt(phaseName.split("-")[0]);
            int b = Integer.parseInt(phaseName.split("-")[1]);
            long time = location.getWorld().getFullTime();
            int phase = (int)((time / 24000) % 8);
            return phase >= a && phase <= b;
        } else if (isNum.matches()) {
            int a = Integer.parseInt(phaseName);
            long time = location.getWorld().getFullTime();
            int phase = (int)((time / 24000) % 8);
            return a == phase;
        } else {
            long time = location.getWorld().getFullTime();
            int phase = (int)((time / 24000) % 8);
            if (phase == 0 && phaseName.equals("full")) return true;
            if ((phase == 1 || phase == 2 || phase == 3) && phaseName.equals("waning")) return true;
            if (phase == 4 && phaseName.equals("new")) return true;
            if ((phase == 5 || phase == 6 || phase == 7) && phaseName.equals("waxing")) return true;
            return false;
        }
	}

}
