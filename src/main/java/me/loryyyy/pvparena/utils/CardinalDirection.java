package me.loryyyy.pvparena.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum CardinalDirection {
    SOUTH, SOUTH_WEST, WEST, NORTH_WEST, NORTH, NORTH_EAST,
    EAST, SOUTH_EAST, UP, DOWN;

    private static final double ANGLE_RANGE = 360.0 / (values().length-2);

    public static CardinalDirection getDirection(Player p) {

        Location loc = p.getLocation();
        double yaw = loc.getYaw();
        double pitch = loc.getPitch();

        if(pitch < -45) return UP;
        else if (pitch > 45) return DOWN;

        while (yaw < 0) {
            yaw += 360;
        }
        while (yaw >= 360) {
            yaw -= 360;
        }

        int index = (int) Math.floor((yaw + (ANGLE_RANGE / 2)) / ANGLE_RANGE);

        return values()[index%(values().length-2)];

    }
}