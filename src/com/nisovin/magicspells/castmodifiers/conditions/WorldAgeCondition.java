package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.castmodifiers.Condition;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WorldAgeCondition extends Condition {

	String mode = "day";
	String v = "=";
	int number = 0;
    String v2 = "";
    int number2 = 0;

	@Override
	public boolean setVar(String var) {
		char[] varc = var.toLowerCase().toCharArray();
		if (varc.length > 3) {
		    if (("" + varc[0] + varc[1] + varc[2]).equalsIgnoreCase("day")) {
		        mode = "day";
		        char[] newArray = new char[varc.length - 3];
                System.arraycopy(varc, 3, newArray, 0, varc.length - 3);
                varc = newArray;
            } else if (("" + varc[0] + varc[1] + varc[2] + varc[3]).equalsIgnoreCase("tick")) {
                mode = "tick";
                char[] newArray = new char[varc.length - 4];
                System.arraycopy(varc, 4, newArray, 0, varc.length - 4);
                varc = newArray;
            }
        }
        if (varc.length > 0) {
            if (("" + varc[0]).equalsIgnoreCase(">") ||
                    ("" + varc[0]).equalsIgnoreCase("<") ||
                    ("" + varc[0]).equalsIgnoreCase("=") ||
                    ("" + varc[0]).equalsIgnoreCase("/") ||
                    ("" + varc[0]).equalsIgnoreCase("*") ||
                    ("" + varc[0]).equalsIgnoreCase("%")) {
                v = varc[0] + "";
                char[] newArray = new char[varc.length - 1];
                System.arraycopy(varc, 1, newArray, 0, varc.length - 1);
                varc = newArray;
            }
        }
        if (varc.length > 0) {
            StringBuilder digit = new StringBuilder();
            int index = 0;
            for (char c : varc) {
                if (Character.isDigit(c)) {
                    digit.append(c);
                    index++;
                } else {
                    break;
                }
            }
            number = Integer.parseInt(digit.toString());
            char[] newArray = new char[varc.length - index];
            System.arraycopy(varc, index, newArray, 0, varc.length - index);
            varc = newArray;
        }
        if (varc.length > 0) {
            if (("" + varc[0]).equalsIgnoreCase(">") ||
                    ("" + varc[0]).equalsIgnoreCase("<") ||
                    ("" + varc[0]).equalsIgnoreCase("=")) {
                v2 = varc[0] + "";
                char[] newArray = new char[varc.length - 1];
                System.arraycopy(varc, 1, newArray, 0, varc.length - 1);
                varc = newArray;
            }
        }
        if (varc.length > 0) {
            StringBuilder digit = new StringBuilder();
            for (char c : varc) {
                if (Character.isDigit(c)) {
                    digit.append(c);
                } else {
                    break;
                }
            }
            number2 = Integer.parseInt(digit.toString());
        }
		return true;
	}
	
	@Override
	public boolean check(Player player) {
        long tick = player.getWorld().getFullTime();
        if (!mode.equalsIgnoreCase("tick")) {
            tick = tick / 24000;
        }
        switch (v) {
            case ">":
            {
                return tick > number;
            }
            case "<":
            {
                return tick < number;
            }
            case "=":
            {
                return tick == number;
            }
            case "%":
            {
                tick = tick % number;
                break;
            }
            case "/":
            {
                tick = tick / number;
                break;
            }
            case "*":
            {
                tick = tick * number;
                break;
            }
        }
        if (!v2.isEmpty()) {
            switch (v2) {
                case ">":
                {
                    return tick > number2;
                }
                case "<":
                {
                    return tick < number2;
                }
                case "=":
                {
                    return tick == number2;
                }
            }
        }
        return false;
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
        return check(player);
	}
	
	@Override
	public boolean check(Player player, Location location) {
        return check(player);
	}

}
