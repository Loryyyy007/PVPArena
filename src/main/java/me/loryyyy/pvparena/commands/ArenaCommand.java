package me.loryyyy.pvparena.commands;

import lombok.Getter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.files.Messages;
import me.loryyyy.pvparena.files.Setting;
import me.loryyyy.pvparena.managers.ArenaCheckTask;
import me.loryyyy.pvparena.utils.Arena;
import me.loryyyy.pvparena.utils.ConstantPaths;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArenaCommand implements TabExecutor {

    @Getter
    private static final Map<Player, Region> selectedRegions = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player p) {

            final int L = args.length;
            Setting setting = Setting.getInstance();
            FileConfiguration setConfig = setting.getConfig();
            FileConfiguration config = PVPArena.getInstance().getConfig();

            if (L == 0) {

                Map<Player, Arena> playersInArena = ArenaCheckTask.getInstance().getPlayersInArena();
                if (playersInArena.containsKey(p)) {
                    p.sendMessage(ChatColor.GREEN + "You are currently in " + ChatColor.GOLD + playersInArena.get(p).getName() + " arena.");
                } else {
                    p.sendMessage(ChatColor.RED + "You are not in an arena.");
                }

            } else if (L == 1) {

                switch (args[0].toLowerCase()) {
                    case "wand" -> {
                        ItemStack wand = UM.getInstance().createItem(Material.IRON_AXE, ChatColor.GOLD + "Arena Wand", Arrays.asList(" ", ChatColor.YELLOW + "Left-click a block to set pos1", ChatColor.YELLOW + "Right-click a block to set pos2"), 1);
                        wand.addEnchantment(Enchantment.DURABILITY, 1);
                        p.getInventory().addItem(wand);
                        p.sendMessage(ChatColor.GOLD + "Arena Wand " + ChatColor.GREEN + "has been added to your inventory.");
                    }
                    case "usage" -> {
                        showUsage(p);
                    }
                    case "reload" -> {
                        PVPArena.getInstance().onDisable();
                        PVPArena.getInstance().onEnable();
                        p.sendMessage(ChatColor.GREEN + "The plugin was reloaded.");
                        //TODO actually reload
                    }
                    case "confreload" -> {
                        Setting.getInstance().reloadConfig();
                        Messages.getInstance().reloadConfig();
                        PVPArena.getInstance().reloadConfig();
                        p.sendMessage(ChatColor.GREEN + "All config files where reloaded.");
                    }
                    case "info" -> {
                        List<String> created = setting.getCreatedArenas();
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                        p.sendMessage(ChatColor.AQUA + "Total number of arenas: " + ChatColor.GOLD + created.size());
                        p.sendMessage(" ");
                        for (String arena : created) {
                            setting.sendInfoOfArena(p, arena);
                            p.sendMessage(" ");
                        }
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                    }
                    case "disable" -> {
                        if (!ArenaCheckTask.getInstance().isTaskEnabled()) {
                            p.sendMessage(ChatColor.GOLD + "The arena check task is already disabled.");
                            return true;
                        }
                        ArenaCheckTask.getInstance().setTaskEnabled(false);
                        config.set(ConstantPaths.TASK_ENABLED, false);
                        PVPArena.getInstance().saveConfig();

                        ArenaCheckTask.getInstance().cancel();
                        ArenaCheckTask.getInstance().getPlayersInArena().clear();
                        p.sendMessage(ChatColor.GREEN + "Arena check task was " + ChatColor.GOLD + "disabled.");
                    }
                    case "enable" -> {
                        if (ArenaCheckTask.getInstance().isTaskEnabled()) {
                            p.sendMessage(ChatColor.GOLD + "The arena check task is already enabled.");
                            return true;
                        }
                        ArenaCheckTask.getInstance().setTaskEnabled(true);
                        config.set(ConstantPaths.TASK_ENABLED, true);
                        PVPArena.getInstance().saveConfig();

                        ArenaCheckTask.getInstance().start();
                        p.sendMessage(ChatColor.GREEN + "Arena check task was " + ChatColor.GOLD + "enabled.");
                    }
                    case "pos1", "pos2" -> {
                        selectedRegions.putIfAbsent(p, new Region());
                        Region region = selectedRegions.get(p);

                        if (args[0].equalsIgnoreCase("pos1"))
                            region.setCorner1(p.getLocation().getBlock().getLocation());
                        else region.setCorner2(p.getLocation().getBlock().getLocation());

                        region.updateVisualEffect(p);

                        p.sendMessage(ChatColor.GREEN + UM.getInstance().capitalize(args[0].toLowerCase()) + " of the region was set to your location.");
                    }
                    default -> {
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                    }
                }

            } else if (L == 2) {

                String arenaName = args[1];

                switch (args[0].toLowerCase()) {
                    case "save" -> {
                        if (!selectedRegions.containsKey(p)) {
                            p.sendMessage(ChatColor.GOLD + "Before saving the arena you need to select a region with the Arena Wand.");
                            return true;
                        }
                        Region region = selectedRegions.get(p);
                        if (region.getCorner1() == null || region.getCorner2() == null) {
                            p.sendMessage(ChatColor.GOLD + "Before saving the arena you need to set both corners of the region with the Arena Wand.");
                            return true;
                        }
                        boolean isNew = setting.addArena(arenaName, region);

                        p.sendMessage(ChatColor.YELLOW + "-------------------------------------");
                        if (isNew) {
                            p.sendMessage(ChatColor.GREEN + "A new arena has been created with name " + ChatColor.DARK_AQUA + arenaName + ".");
                        } else {
                            p.sendMessage(ChatColor.DARK_AQUA + arenaName + ChatColor.GREEN + " arena's region was updated.");
                        }
                        p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(region.getCorner1()));
                        p.sendMessage(ChatColor.AQUA + "Corner 2: " + ChatColor.GOLD + UM.getInstance().locToString(region.getCorner2()));
                        p.sendMessage(ChatColor.AQUA + "Enabled: " + ChatColor.GOLD + "true");
                        p.sendMessage(ChatColor.YELLOW + "-------------------------------------");

                    }
                    case "delete" -> {

                        boolean exists = setting.deleteArena(arenaName);

                        if (exists) {
                            p.sendMessage(ChatColor.GREEN + "You deleted arena: " + ChatColor.GOLD + arenaName);
                        } else {
                            p.sendMessage(ChatColor.GOLD + "No arena with this name was found.");
                        }

                    }
                    case "info" -> {

                        if (!setting.arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }

                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");

                        setting.sendInfoOfArena(p, arenaName);

                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                    }
                    case "disable" -> {
                        if (!setting.arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if (!setConfig.getBoolean(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED)) {
                            p.sendMessage(ChatColor.GOLD + "This arena is already disabled.");
                            return true;
                        }
                        setting.disableArena(arenaName);

                        p.sendMessage(ChatColor.GREEN + "Arena " + arenaName + " was " + ChatColor.GOLD + "disabled.");
                    }
                    case "enable" -> {
                        if (!setting.arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if (setConfig.getBoolean(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED)) {
                            p.sendMessage(ChatColor.GOLD + "This arena is already enabled.");
                            return true;
                        }
                        setting.enableArena(arenaName);

                        p.sendMessage(ChatColor.GREEN + "Arena " + arenaName + " was " + ChatColor.GOLD + "enabled.");
                    }
                    default -> {
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                    }
                }

            } else if (L == 3) {

                String oldArenaName = args[1];
                String newArenaName = args[2];

                switch (args[0].toLowerCase()) {
                    case "changename" -> {

                        List<String> created = setting.getCreatedArenas();
                        if (!created.contains(oldArenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if (created.contains(newArenaName)) {
                            p.sendMessage(ChatColor.RED + "An arena with this name already exists.");
                            return true;
                        }
                        setting.changeArenaName(oldArenaName, newArenaName);

                        p.sendMessage(ChatColor.GOLD + oldArenaName + " arena" + ChatColor.GREEN + " was renamed to " + ChatColor.GOLD + newArenaName);
                    }
                    default -> {
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                    }
                }

            } else showUsage(p);


        } else {
            PVPArena.getInstance().getLogger().warning("This command can be executed only by players.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        ArrayList<String> l = new ArrayList<>();

        if (sender instanceof Player p) {
            final int L = args.length;

            if (L == 1) {

                l.add("usage");
                l.add("wand");
                l.add("save");
                l.add("changeName");
                l.add("delete");
                l.add("enable");
                l.add("disable");
                l.add("info");
                l.add("pos1");
                l.add("pos2");
                l.add("confReload");
                l.add("reload");

            } else if (L == 2) {

                final String A1 = args[0].toLowerCase();

                List<String> createdArenas = Setting.getInstance().getCreatedArenas();

                switch (A1) {
                    case "changename", "delete", "info" -> l.addAll(createdArenas);
                    case "enable" -> {
                        for (String arena : createdArenas) {
                            if (!Setting.getInstance().getConfig().getBoolean(ConstantPaths.ARENA_SETTING + arena + ConstantPaths.ARENA_ENABLED))
                                l.add(arena);
                        }
                    }
                    case "disable" -> {
                        for (String arena : createdArenas) {
                            if (Setting.getInstance().getConfig().getBoolean(ConstantPaths.ARENA_SETTING + arena + ConstantPaths.ARENA_ENABLED))
                                l.add(arena);
                        }
                    }
                }

            }

        }

        return UM.getInstance().filterList(l, args);
    }

    private void showUsage(Player p) {
        UM um = UM.getInstance();

        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Arena command usages:");

        um.sendUsageOfCommand("/arena usage", "shows this list.", p);
        um.sendUsageOfCommand("/arena", "shows the arena you are currently in.", p);
        um.sendUsageOfCommand("/arena wand", "gives the tool to select regions for the arenas.", p);
        um.sendUsageOfCommand("/arena save <arenaName>", "saves an arena as the selected region.", p);
        um.sendUsageOfCommand("/arena changeName <oldArenaName> <newArenaName>", "changes the name of an arena.", p);
        um.sendUsageOfCommand("/arena delete <arenaName>", "deletes an arena.", p);
        um.sendUsageOfCommand("/arena enable", "enables the enter/exit arena check.", p);
        um.sendUsageOfCommand("/arena disable", "disables the enter/exit arena check.", p);
        um.sendUsageOfCommand("/arena enable <arenaName>", "enables a certain arena.", p);
        um.sendUsageOfCommand("/arena disable <arenaName>", "disables a certain arena.", p);
        um.sendUsageOfCommand("/arena info", "shows some info of all arenas.", p);
        um.sendUsageOfCommand("/arena info <arenaName>", "shows some info of a certain arena.", p);
        um.sendUsageOfCommand("/arena pos1", "sets pos1 to your current location.", p);
        um.sendUsageOfCommand("/arena pos2", "sets pos2 to your current location.", p);
        um.sendUsageOfCommand("/arena confReload", "reloads all configs.", p);
        um.sendUsageOfCommand("/arena reload", "reloads the plugin.", p);

        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
    }
}
