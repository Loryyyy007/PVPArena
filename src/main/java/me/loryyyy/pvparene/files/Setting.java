package me.loryyyy.pvparene.files;

import me.loryyyy.pvparene.PVPArena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Setting {

    private static final Setting instance = new Setting();

    private Setting() {
    }

    private File file;
    private FileConfiguration configuration;

    public void setup() {
        file = new File(PVPArena.getInstance().getDataFolder() + "/saves", "Setting.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                PVPArena.getInstance().getLogger().severe("Failed to create the Setting.yml file.");
                throw new RuntimeException(e);
            }
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        saveConfig();
    }

    public void saveConfig() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            PVPArena.getInstance().getLogger().severe("Failed to save the Setting.yml file.");
        }
    }

    public void reloadConfig() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public static Setting getInstance() {
        return instance;
    }

}
