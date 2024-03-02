package me.loryyyy.pvparena.utils;

import lombok.Getter;
import lombok.Setter;
import me.loryyyy.pvparena.files.Setting;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
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

        setCorner1(config.getLocation("Arenas." + name + ".Corner 1"));
        setCorner2(config.getLocation("Arenas." + name + ".Corner 2"));
        this.enabled = config.getBoolean("Arenas." + name + ".Enabled");
        this.name = name; //to change on changeName
    }

}
