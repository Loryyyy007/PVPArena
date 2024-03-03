package me.loryyyy.pvparena.managers;

import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.commands.ArenaCommand;
import me.loryyyy.pvparena.utils.Arena;
import me.loryyyy.pvparena.utils.ConstantPaths;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Listeners implements Listener {

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent e){
        Player p = e.getPlayer();

        Region region = ArenaCommand.getSelectedRegions().getOrDefault(p, null);
        if(region == null) return;
        PlayerInventory inv = p.getInventory();
        ItemStack newItem = inv.getItem(e.getNewSlot());
        ItemStack oldItem = inv.getItem(e.getPreviousSlot());

        if(newItem != null && isArenaWand(newItem)){
            region.setRegionVisible(true);
        }else if(oldItem != null && isArenaWand(oldItem)){
            region.setRegionVisible(false);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e){
        FileConfiguration config = PVPArena.getInstance().getConfig();
        Entity entity = e.getEntity();
        Entity damager = e.getDamager();
        if(!(entity instanceof Player victim)) return;

        Player damagerPl = null;
        if(damager instanceof Player){
            damagerPl = (Player) damager;
        } else if (damager instanceof Projectile projectile) {
            if(!(projectile.getShooter() instanceof Player)) return;
            damagerPl = (Player) projectile.getShooter();
        }
        if(damagerPl == null) return;

        boolean isVictimInArena = ArenaCheckTask.getInstance().getPlayersInArena().containsKey(victim);
        boolean isDamagerInArena = ArenaCheckTask.getInstance().getPlayersInArena().containsKey(damagerPl);

        if(isVictimInArena && !isDamagerInArena && config.getBoolean(ConstantPaths.OUT_OF_ARENA_INTERFERENCE)){
            e.setCancelled(true);
            return;
        }

        if(victim.getHealth() < e.getFinalDamage()){
            if(isVictimInArena){
                ArenaCheckTask.getInstance().getPlayersInArena().get(victim).onDeath(victim, damagerPl);
            }else e.setCancelled(true);
            return;
        }
        if(!config.getBoolean(ConstantPaths.OUT_OF_ARENA_DAMAGE)) e.setCancelled(true);

    }

    public static boolean isArenaWand(ItemStack item) {
        if(item.getType() != Material.IRON_AXE) return false;
        if(!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Arena Wand");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if(!isArenaWand(item)) return;
        e.setCancelled(true);

        ArenaCommand.getSelectedRegions().putIfAbsent(p, new Region());
        Region region = ArenaCommand.getSelectedRegions().get(p);

        Location blockLoc = e.getClickedBlock().getLocation();

        String pos;
        if(action == Action.RIGHT_CLICK_BLOCK){
            if(region.getCorner2() != null && blockLoc.equals(region.getCorner2().getBlock().getLocation())) return;

            region.setCorner2(blockLoc);

            pos = "Pos2";
        }else{
            if(region.getCorner1() != null && blockLoc.equals(region.getCorner1().getBlock().getLocation())) return;

            region.setCorner1(blockLoc);

            pos = "Pos1";
        }
        region.updateVisualEffect(p);
        p.sendMessage(ChatColor.GREEN + pos + " of the region was set to: " + ChatColor.GOLD + UM.getInstance().locToString(blockLoc));

    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        Region region = ArenaCommand.getSelectedRegions().getOrDefault(p, null);
        if(region != null) region.endVisualEffect();
        ArenaCommand.getSelectedRegions().remove(p);
        ArenaCheckTask.getInstance().getPlayersInArena().remove(p);
    }



}
