package me.loryyyy.pvparena;

import lombok.Getter;
import me.loryyyy.pvparena.commands.ArenaCommand;
import me.loryyyy.pvparena.files.Messages;
import me.loryyyy.pvparena.files.Setting;
import me.loryyyy.pvparena.managers.ArenaCheckTask;
import me.loryyyy.pvparena.managers.Listeners;
import me.loryyyy.pvparena.utils.Arena;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PVPArena extends JavaPlugin {

    @Getter
    private static PVPArena instance;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        reloadConfig();

        File file = new File(getDataFolder() + "/saves");
        if(!file.mkdir()){
            getLogger().severe("Failed to create directory /saves.");
        }

        Setting.getInstance().setup();
        Messages.getInstance().setup();

        getCommand("arena").setExecutor(new ArenaCommand());

        getServer().getPluginManager().registerEvents(new Listeners(), this);

        ArenaCheckTask.getInstance().setTaskEnabled(getConfig().getBoolean("General.Enabled"));

        if(ArenaCheckTask.getInstance().isTaskEnabled()) {
            ArenaCheckTask.getInstance().start();
        }

        for(String arenaName : Setting.getInstance().getCreatedArenas()){
            Arena arena = new Arena(arenaName);
            if(arena.isEnabled()) Arena.getEnabledArenas().put(arenaName, arena);
        }

        getLogger().info("PVPArena plugin has loaded successfully!");

    }

    @Override
    public void onDisable() {

        ArenaCheckTask.getInstance().cancel();

    }

}
