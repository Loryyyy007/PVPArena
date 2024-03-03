package me.loryyyy.pvparena.utils;

import lombok.Getter;
import lombok.Setter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.files.Messages;
import me.loryyyy.pvparena.files.Setting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Arena extends Region {

    @Getter
    private static final Map<String, Arena> enabledArenas = new HashMap<>();
    private boolean enabled;
    private String name;

    public Arena(String name) {
        FileConfiguration config = Setting.getInstance().getConfig();

        setCorner1(config.getLocation(ConstantPaths.ARENA_SETTING + name + ConstantPaths.ARENA_CORNER1));
        setCorner2(config.getLocation(ConstantPaths.ARENA_SETTING + name + ConstantPaths.ARENA_CORNER2));
        this.enabled = config.getBoolean(ConstantPaths.ARENA_SETTING + name + ConstantPaths.ARENA_ENABLED);
        this.name = name;
    }

    public void onJoin(Player p){

        if(!PVPArena.getInstance().getConfig().getBoolean(ConstantPaths.JOIN_MESSAGE_ENABLED)) return;

        String message = Messages.getInstance().getMessage(ConstantPaths.ARENA_JOIN_MESSAGE);
        message = message.replace("<player>", p.getName()).replace("<arena>", this.getName());
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void onLeave(Player p){

        if(!PVPArena.getInstance().getConfig().getBoolean(ConstantPaths.LEAVE_MESSAGE_ENABLED)) return;

        String message = Messages.getInstance().getMessage(ConstantPaths.ARENA_LEAVE_MESSAGE);
        message = message.replace("<player>", p.getName()).replace("<arena>", this.getName());
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void onDeath(Player victim, Player killer){

        if(!PVPArena.getInstance().getConfig().getBoolean(ConstantPaths.DEATH_MESSAGE_ENABLED)) return;

        String message = Messages.getInstance().getMessage(ConstantPaths.ARENA_DEATH_MESSAGE);
        message = message.replace("<victim>", victim.getName()).replace("<killer>", killer.getName());
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

}
