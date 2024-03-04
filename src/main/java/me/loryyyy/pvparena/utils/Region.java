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

        if(Listeners.isArenaWand(p.getInventory().getItemInMainHand())) setRegionVisible(true);
        this.externalLocations = getExternalLocations();
        if(regionTask == null){
            this.regionTask = new BukkitRunnable(){
                @Override
                public void run() {
                    if(!Region.this.isRegionVisible()) return;
                    for(Location location : Region.this.externalLocations){
                        p.spawnParticle(Particle.VILLAGER_HAPPY, location, 1);
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

}
