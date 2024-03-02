package me.loryyyy.pvparena.managers;

import me.loryyyy.pvparena.commands.ArenaCommand;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
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

    }
    @EventHandler
    public void onDamage(EntityDamageEvent e){

    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        if(p.getGameMode() != GameMode.CREATIVE) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if(!isArenaWand(item)) return;

        e.setCancelled(true);

        ArenaCommand.getSelectedRegions().putIfAbsent(p, new Region());
        Region region = ArenaCommand.getSelectedRegions().get(p);
        if(e.getBlock().getLocation().equals(region.getCorner1())) return;

        region.setCorner1(e.getBlock().getLocation());

        region.updateVisualEffect(p);

        p.sendMessage(ChatColor.GREEN + "Pos1 of the region was set to that block's location.");

    }

    private boolean isArenaWand(ItemStack item) {
        if(item.getType() != Material.IRON_AXE) return false;
        if(!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Arena Wand");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(p.getGameMode() != GameMode.CREATIVE) return;
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if(!isArenaWand(item)) return;
        e.setCancelled(true);

        ArenaCommand.getSelectedRegions().putIfAbsent(p, new Region());
        Region region = ArenaCommand.getSelectedRegions().get(p);
        if(e.getClickedBlock().getLocation().equals(region.getCorner2())) return;

        region.setCorner2(e.getClickedBlock().getLocation());

        region.updateVisualEffect(p);

        p.sendMessage(ChatColor.GREEN + "Pos2 of the region was set to that block's location.");
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        ArenaCommand.getSelectedRegions().remove(p);
        ArenaCheckTask.getInstance().getPlayersInArena().remove(p);
    }



}
