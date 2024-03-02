package me.loryyyy.pvparena.managers;

import me.loryyyy.pvparena.commands.ArenaCommand;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class Listeners implements Listener {

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e){
        Entity entity = e.getEntity();
        Entity damager = e.getDamager();
        if(!(entity instanceof Player victim)) return;

        Player damagerPl = null;
        if(damager instanceof Player){
            damagerPl = (Player) damager;
        } else if (damager instanceof Arrow arrow) {
            if(!(arrow.getShooter() instanceof Player)) return;
            damagerPl = (Player) arrow.getShooter();
        }
        if(damagerPl == null) return;

        if(ArenaCheckTask.getInstance().getPlayersInArena().containsKey(victim)){
            if(ArenaCheckTask.getInstance().getPlayersInArena().containsKey(damagerPl)) e.setCancelled(true);
            return;
        }
        if(victim.getHealth() < e.getFinalDamage()) e.setCancelled(true);

    }
    @EventHandler
    public void onDamage(EntityDamageEvent e){

    }

    private boolean isArenaWand(ItemStack item) {
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

        if(action == Action.RIGHT_CLICK_BLOCK){
            if(e.getClickedBlock().getLocation().equals(region.getCorner2())) return;

            region.setCorner2(e.getClickedBlock().getLocation());

            region.updateVisualEffect(p);

            p.sendMessage(ChatColor.GREEN + "Pos2 of the region was set to that block's location.");
        }else{
            if(e.getClickedBlock().getLocation().equals(region.getCorner1())) return;

            region.setCorner1(e.getClickedBlock().getLocation());

            region.updateVisualEffect(p);

            p.sendMessage(ChatColor.GREEN + "Pos1 of the region was set to that block's location.");
        }

    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        ArenaCommand.getSelectedRegions().remove(p);
        ArenaCheckTask.getInstance().getPlayersInArena().remove(p);
    }



}
