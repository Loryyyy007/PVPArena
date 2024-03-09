package me.loryyyy.pvparena.utils;

import lombok.Getter;
import lombok.Setter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.managers.Listeners;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Region {

    private Location corner1;
    private Location corner2;
    private final ArrayList<Block> blocks = new ArrayList<>();

    private BukkitTask regionTask = null;
    private List<Location> externalLocations = null;
    private boolean regionVisible = false;
    private Particle particle = null;

    public boolean contains(Location loc) {

        UM um = UM.getInstance();
        boolean xIn = um.isIn(Math.min(corner1.getX(), corner2.getX()), Math.max(corner1.getX(), corner2.getX()), loc.getX());
        boolean yIn = um.isIn(Math.min(corner1.getY(), corner2.getY()), Math.max(corner1.getY(), corner2.getY()), loc.getY());
        boolean zIn = um.isIn(Math.min(corner1.getZ(), corner2.getZ()), Math.max(corner1.getZ(), corner2.getZ()), loc.getZ());

        return xIn && yIn && zIn;

    }

    private void makeCornersPrecise() {

        if (corner1 == null || corner2 == null) return;

        corner1 = corner1.getBlock().getLocation();
        corner2 = corner2.getBlock().getLocation();

        double x1Inc = 0;
        double x2Inc = 0;
        double y1Inc = 0;
        double y2Inc = 0;
        double z1Inc = 0;
        double z2Inc = 0;

        if (corner1.getX() > corner2.getX()) {
            x1Inc = 0.95;
        } else x2Inc = 0.95;
        if (corner1.getY() > corner2.getY()) {
            y1Inc = 0.95;
        } else y2Inc = 0.95;
        if (corner1.getZ() > corner2.getZ()) {
            z1Inc = 0.95;
        } else z2Inc = 0.95;

        corner1.add(x1Inc, y1Inc, z1Inc);
        corner2.add(x2Inc, y2Inc, z2Inc);

    }

    public void centerCorners(){
        corner1 = corner1.getBlock().getLocation().add(0.5, -0.5, 0.5);
        corner2 = corner2.getBlock().getLocation().add(0.5, -0.5, 0.5);
    }

    private List<Location> getExternalLocations(){
        List<Location> borderLocations = new ArrayList<>();

        World world = corner1.getWorld();
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        for (double x = minX; x <= maxX+0.05; x++) {
            for (double z = minZ; z <= maxZ+0.05; z++) {
                borderLocations.add(new Location(world, x, minY, z));
                borderLocations.add(new Location(world, x, maxY, z));
            }
        }
        for (double y = minY; y <= maxY+0.05; y++) {
            for (double z = minZ; z <= maxZ+0.05; z++) {
                borderLocations.add(new Location(world, minX, y, z));
                borderLocations.add(new Location(world, maxX, y, z));
            }
        }
        for (double x = minX; x <= maxX+0.05; x++) {
            for (double y = minY; y <= maxY+0.05; y++) {
                borderLocations.add(new Location(world, x, y, minZ));
                borderLocations.add(new Location(world, x, y, maxZ));
            }
        }

        return borderLocations;
    }

    public void updateVisualEffect(Player p) {
        FileConfiguration config = PVPArena.getInstance().getConfig();
        if(corner1 == null || corner2 == null){
            return;
        }
        if(config.getBoolean(ConstantPaths.PRECISE_CORNERS)) makeCornersPrecise();
        else centerCorners();

        if(!config.getBoolean(ConstantPaths.REGION_VISIBLE)) return;

        if(Listeners.isArenaWand(p.getInventory().getItemInMainHand())) setRegionVisible(true);
        this.externalLocations = getExternalLocations();
        try {
            this.particle = Particle.valueOf(config.getString(ConstantPaths.REGION_PARTICLE).toUpperCase());
        }catch (IllegalArgumentException | NullPointerException ex){
            this.particle = Particle.VILLAGER_HAPPY;
        }
        if(regionTask == null){

            this.regionTask = new BukkitRunnable(){
                @Override
                public void run() {
                    if(!Region.this.isRegionVisible()) return;
                    if(Region.this.externalLocations.size() > config.getInt(ConstantPaths.REGION_PARTICLE_AMOUNT)) return;

                    for(Location location : Region.this.externalLocations){
                        p.spawnParticle(Region.this.particle, location, 1);
                    }
                }
            }.runTaskTimer(PVPArena.getInstance(), 0, config.getInt(ConstantPaths.REGION_VISIBILITY_PERIOD));
        }
    }

    public void endVisualEffect() {
        if(this.regionTask == null) return;
        this.regionTask.cancel();
        this.regionTask = null;
    }

    public void move(CardinalDirection face, int amount){

        int xInc = 0;
        int yInc = 0;
        int zInc = 0;

        String name = face.name();

        if(face == CardinalDirection.UP){
            yInc = amount;
        } else if (face == CardinalDirection.DOWN) {
            yInc = -amount;
        }else {
            if (name.contains("NORTH")) {
                zInc = -amount;
            }
            if (name.contains("SOUTH")) {
                zInc = amount;
            }
            if (name.contains("EAST")) {
                xInc = amount;
            }
            if (name.contains("WEST")) {
                xInc = -amount;
            }
        }

        corner1.add(xInc, yInc, zInc);
        corner2.add(xInc, yInc, zInc);

    }
    public void changeSize(CardinalDirection face, int amount, boolean expand){

        int xInc = 0;
        int yInc = 0;
        int zInc = 0;

        String name = face.name();

        if(face == CardinalDirection.UP){
            yInc = amount;
        } else if (face == CardinalDirection.DOWN) {
            yInc = -amount;
        }else {
            if (name.contains("NORTH")) {
                zInc = -amount;
            }
            if (name.contains("SOUTH")) {
                zInc = amount;
            }
            if (name.contains("EAST")) {
                xInc = amount;
            }
            if (name.contains("WEST")) {
                xInc = -amount;
            }
        }

        int deltaX = (int) (corner1.getX()-corner2.getX());
        int deltaY = (int) (corner1.getY()-corner2.getY());
        int deltaZ = (int) (corner1.getZ()-corner2.getZ());
        UM um = UM.getInstance();

        Location c1 = expand ? corner1 : corner2;
        Location c2 = expand ? corner2 : corner1;

        c1.add(um.concord(deltaX, xInc) ? xInc : 0, um.concord(deltaY, yInc) ? yInc : 0, um.concord(deltaZ, zInc) ? zInc : 0);
        c2.add(!um.concord(deltaX, xInc) ? xInc : 0, !um.concord(deltaY, yInc) ? yInc : 0, !um.concord(deltaZ, zInc) ? zInc : 0);


    }



}
