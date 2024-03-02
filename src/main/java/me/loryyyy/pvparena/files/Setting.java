package me.loryyyy.pvparena.files;

import lombok.Getter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Setting {

    @Getter
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
            throw new RuntimeException(e);
        }
    }

    public void reloadConfig() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public List<String> getCreatedArenas(){
        return UM.getInstance().getCommaList(getConfig(), "Created", false);
    }
    public boolean addArena(String arenaName, Region region){
        List<String> created = getCreatedArenas();
        boolean isNew = !created.contains(arenaName);

        if(isNew) UM.getInstance().addToCommaList(getConfig(), "Created", arenaName);

        getConfig().set("Arenas." + arenaName + ".Enabled", true);
        getConfig().set("Arenas." + arenaName + ".Corner 1", region.getCorner1());
        getConfig().set("Arenas." + arenaName + ".Corner 2", region.getCorner2());
        saveConfig();

        return isNew;
    }
    public boolean deleteArena(String arenaName){
        List<String> created = getCreatedArenas();
        boolean exists = created.contains(arenaName);

        if(!exists) return false;

        UM.getInstance().removeFromCommaList(getConfig(), "Created", arenaName);
        getConfig().set("Arenas." + arenaName, null);
        saveConfig();

        return true;
    }
    public void changeArenaName(String oldArenaName, String newArenaName){

        UM.getInstance().removeFromCommaList(getConfig(), "Created", oldArenaName);
        UM.getInstance().addToCommaList(getConfig(), "Created", newArenaName);

        ConfigurationSection oldSection = getConfig().getConfigurationSection("Arenas." + oldArenaName);
        getConfig().set("Arenas." + oldArenaName, null);
        getConfig().set("Arenas." + newArenaName, oldSection);
        saveConfig();

    }

    public boolean arenaExists(String arenaName){
        List<String> created = getCreatedArenas();
        return created.contains(arenaName);
    }

}
