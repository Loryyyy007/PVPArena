package me.loryyyy.pvparena.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Getter
@Setter
public class Region {

    private Location corner1;
    private Location corner2;
    private final ArrayList<Block> blocks = new ArrayList<>();

    public Region(Location corner1, Location corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }
    public Region() {

    }

    public boolean contains(Location loc) {

        UM um = UM.getInstance();
        boolean xIn = um.isIn(Math.min(corner1.getX(), corner2.getX()), Math.max(corner1.getX(), corner2.getX()), loc.getX());
        boolean yIn = um.isIn(Math.min(corner1.getY(), corner2.getY()), Math.max(corner1.getY(), corner2.getY()), loc.getY());
        boolean zIn = um.isIn(Math.min(corner1.getZ(), corner2.getZ()), Math.max(corner1.getZ(), corner2.getZ()), loc.getZ());

        return xIn && yIn && zIn;

    }

    public ArrayList<Block> getBlocks() {

        if (!blocks.isEmpty()) return blocks;

        for (double x = Math.min(corner1.getBlockX(), corner2.getBlockX()); x <= Math.max(corner1.getBlockX(), corner2.getBlockX()); x++) {
            for (double y = Math.min(corner1.getBlockY(), corner2.getBlockY()); y <= Math.max(corner1.getBlockY(), corner2.getBlockY()); y++) {
                for (double z = Math.min(corner1.getBlockZ(), corner2.getBlockZ()); z <= Math.max(corner1.getBlockZ(), corner2.getBlockZ()); z++) {

                    Block block = new Location(corner1.getWorld(), x, y, z).getBlock();
                    blocks.add(block);

                }
            }
        }
        return blocks;

    }

    public void clear() {
        for (Block block : getBlocks())
            block.setType(Material.AIR);
    }

    public Location getCentre() {

        double x = (corner1.getX() + corner2.getX()) / 2;
        double y = (corner1.getY() + corner2.getY()) / 2;
        double z = (corner1.getZ() + corner2.getZ()) / 2;

        return new Location(corner1.getWorld(), x, y, z);

    }

    public double getHeight() {
        return Math.abs(corner1.getY() - corner2.getY());
    }

    public float getBottomY() {
        return Math.min(corner1.getBlockY(), corner2.getBlockY());
    }


    public void updateVisualEffect(Player p){

    }
    public void hideToPlayer(Player p){

    }

}
