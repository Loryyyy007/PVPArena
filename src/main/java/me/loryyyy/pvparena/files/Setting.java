package me.loryyyy.pvparena.files;

import lombok.Getter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.utils.Arena;
import me.loryyyy.pvparena.utils.ConstantPaths;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
        return UM.getInstance().getCommaList(getConfig(), ConstantPaths.CREATED_ARENAS, false);
    }
    public boolean addArena(String arenaName, Region region){
        List<String> created = getCreatedArenas();
        boolean isNew = !created.contains(arenaName);

        if(isNew) UM.getInstance().addToCommaList(getConfig(), ConstantPaths.CREATED_ARENAS, arenaName);

        getConfig().set(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED, true);
        getConfig().set(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_CORNER1, region.getCorner1());
        getConfig().set(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_CORNER2, region.getCorner2());
        saveConfig();

        Arena arena = new Arena(arenaName);
        Arena.getEnabledArenas().put(arenaName, arena);

        return isNew;
    }
    public boolean deleteArena(String arenaName){
        List<String> created = getCreatedArenas();
        boolean exists = created.contains(arenaName);

        if(!exists) return false;

        UM.getInstance().removeFromCommaList(getConfig(), ConstantPaths.CREATED_ARENAS, arenaName);
        getConfig().set(ConstantPaths.ARENA_SETTING + arenaName, null);
        saveConfig();

        Arena.getEnabledArenas().remove(arenaName);

        return true;
    }
    public void changeArenaName(String oldArenaName, String newArenaName){

        UM.getInstance().removeFromCommaList(getConfig(), ConstantPaths.CREATED_ARENAS, oldArenaName);
        UM.getInstance().addToCommaList(getConfig(), ConstantPaths.CREATED_ARENAS, newArenaName);

        ConfigurationSection oldSection = getConfig().getConfigurationSection(ConstantPaths.ARENA_SETTING + oldArenaName);
        getConfig().set(ConstantPaths.ARENA_SETTING + oldArenaName, null);
        getConfig().set(ConstantPaths.ARENA_SETTING + newArenaName, oldSection);
        saveConfig();

        Arena arena = new Arena(newArenaName);
        Arena.getEnabledArenas().remove(oldArenaName);
        Arena.getEnabledArenas().put(newArenaName, arena);

    }
    public void disableArena(String arenaName){
        getConfig().set(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED, false);
        saveConfig();
        Arena.getEnabledArenas().remove(arenaName);
    }

    public void enableArena(String arenaName){
        getConfig().set(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED, true);
        saveConfig();
        Arena arena = new Arena(arenaName);
        Arena.getEnabledArenas().put(arenaName, arena);
    }

    public boolean arenaExists(String arenaName){
        List<String> created = getCreatedArenas();
        return created.contains(arenaName);
    }

    public void sendInfoOfArena(Player p, String arenaName){
        Location corner1 = getConfig().getLocation(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_CORNER1);
        Location corner2 = getConfig().getLocation(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_CORNER2);
        boolean enabled = getConfig().getBoolean(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED);

        if(corner1 == null || corner2 == null) return;

        p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + arenaName);
        p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(corner1));
        p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(corner2));
        p.sendMessage(ChatColor.AQUA + "Enabled: " + ChatColor.GOLD + enabled);
    }

}
