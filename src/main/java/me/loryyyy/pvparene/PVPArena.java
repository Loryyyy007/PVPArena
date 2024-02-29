package me.loryyyy.pvparene;

import lombok.Getter;
import me.loryyyy.pvparene.commands.ArenaCommand;
import me.loryyyy.pvparene.files.Messages;
import me.loryyyy.pvparene.files.Setting;
import me.loryyyy.pvparene.managers.ArenaCheckTask;
import me.loryyyy.pvparene.managers.Listeners;
import org.bukkit.plugin.java.JavaPlugin;

public final class PVPArena extends JavaPlugin {

    @Getter
    private static PVPArena instance;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        reloadConfig();

        Setting.getInstance().setup();
        Messages.getInstance().setup();

        getCommand("arena").setExecutor(new ArenaCommand());

        getServer().getPluginManager().registerEvents(new Listeners(), this);

        if(getConfig().getBoolean("General.Enabled")) {
            ArenaCheckTask.getInstance().start();
        }

    }

    @Override
    public void onDisable() {

        ArenaCheckTask.getInstance().cancel();

    }

}
