package me.loryyyy.pvparena.files;

import lombok.Getter;
import me.loryyyy.pvparena.PVPArena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Messages {

    @Getter
    private static final Messages instance = new Messages();

    private Messages() {
    }

    private File file;
    private FileConfiguration configuration;

    public void setup() {
        file = new File(PVPArena.getInstance().getDataFolder(), "Messages.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                PVPArena.getInstance().getLogger().severe("Failed to create the Messages.yml file.");
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
            PVPArena.getInstance().getLogger().severe("Failed to save the Messages.yml file.");
        }
    }

    public void reloadConfig() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public String getMessage(String message){
        List<String> list = (List<String>) getConfig().getList(message);
        if(list == null){
            return getConfig().getString(message);
        }
        if(list.isEmpty()) return "";
        if(list.size() == 1) return list.get(0);

        Random r = new Random();
        int n = r.nextInt(list.size());
        return list.get(n);
    }

}
