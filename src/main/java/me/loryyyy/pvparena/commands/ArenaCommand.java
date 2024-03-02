package me.loryyyy.pvparena.commands;

import lombok.Getter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.files.Setting;
import me.loryyyy.pvparena.managers.ArenaCheckTask;
import me.loryyyy.pvparena.utils.Arena;
import me.loryyyy.pvparena.utils.Region;
import me.loryyyy.pvparena.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

            if (L == 0) {

                Map<Player, Arena> playersInArena = ArenaCheckTask.getInstance().getPlayersInArena();
                if (playersInArena.containsKey(p)) {
                    p.sendMessage(ChatColor.GREEN + "You are currently in " + ChatColor.GOLD + playersInArena.get(p).getName() + " arena.");
                } else {
                    p.sendMessage(ChatColor.RED + "You are not in an arena.");
                }

            } else if (L == 1) {

                switch (args[0].toLowerCase()) {
                    case "wand":
                        ItemStack wand = UM.getInstance().createItem(Material.IRON_AXE, ChatColor.GOLD + "Arena Wand", Arrays.asList(" ", ChatColor.YELLOW + "Break a block to set pos1", ChatColor.YELLOW + "Right-click a block to set pos2"), 1);
                        wand.addEnchantment(Enchantment.DURABILITY, 1);
                        p.getInventory().addItem(wand);
                        p.sendMessage(ChatColor.GOLD + "Arena Wand " + ChatColor.GREEN + "has been added to your inventory.");
                        break;
                    case "usage":
                        showUsage(p);
                        break;
                    case "info":
                        List<String> created = Setting.getInstance().getCreatedArenas();
                        FileConfiguration config = Setting.getInstance().getConfig();
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                        p.sendMessage(ChatColor.AQUA + "Total number of arenas: " + ChatColor.GOLD + created.size());
                        p.sendMessage(" ");
                        for (String arena : created) {
                            Location corner1 = config.getLocation("Arenas." + arena + ".Corner 1");
                            Location corner2 = config.getLocation("Arenas." + arena + ".Corner 2");
                            boolean enabled = config.getBoolean("Arenas." + arena + ".Enabled");
                            if (corner1 == null || corner2 == null) continue;

                            p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + arena);
                            p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(corner1));
                            p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(corner2));
                            p.sendMessage(ChatColor.AQUA + "Enabled: " + ChatColor.GOLD + enabled);
                            p.sendMessage(" ");
                        }
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                        break;
                    case "disable":
                        if(!ArenaCheckTask.getInstance().isTaskEnabled()){
                            p.sendMessage(ChatColor.GOLD + "The arena check task is already disabled.");
                            return true;
                        }
                        ArenaCheckTask.getInstance().setTaskEnabled(false);
                        PVPArena.getInstance().getConfig().set("General.Enabled", false);
                        PVPArena.getInstance().saveConfig();

                        ArenaCheckTask.getInstance().cancel();
                        ArenaCheckTask.getInstance().getPlayersInArena().clear();
                        p.sendMessage(ChatColor.GREEN + "Arena check task was " + ChatColor.GOLD + "disabled.");
                        break;
                    case "enable":
                        if(ArenaCheckTask.getInstance().isTaskEnabled()){
                            p.sendMessage(ChatColor.GOLD + "The arena check task is already enabled.");
                            return true;
                        }
                        ArenaCheckTask.getInstance().setTaskEnabled(true);
                        PVPArena.getInstance().getConfig().set("General.Enabled", true);
                        PVPArena.getInstance().saveConfig();

                        ArenaCheckTask.getInstance().start();
                        p.sendMessage(ChatColor.GREEN + "Arena check task was " + ChatColor.GOLD + "enabled.");
                        break;
                    case "pos1":
                    case "pos2":
                        selectedRegions.putIfAbsent(p, new Region());
                        Region region = selectedRegions.get(p);

                        if(args[0].equalsIgnoreCase("pos1"))
                            region.setCorner1(p.getLocation().getBlock().getLocation());
                        else region.setCorner2(p.getLocation().getBlock().getLocation());

                        region.updateVisualEffect(p);

                        p.sendMessage(ChatColor.GREEN + UM.getInstance().capitalize(args[0].toLowerCase()) + " of the region was set to your location.");
                    default:
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                        break;
                }

            } else if (L == 2) {

                String arenaName = args[1];
                FileConfiguration config = Setting.getInstance().getConfig();

                switch (args[0].toLowerCase()) {
                    case "save":
                        if (!selectedRegions.containsKey(p)) {
                            p.sendMessage(ChatColor.GOLD + "Before saving the arena you need to select a region with the Arena Wand.");
                            return true;
                        }
                        Region region = selectedRegions.get(p);
                        if (region.getCorner1() == null || region.getCorner2() == null) {
                            p.sendMessage(ChatColor.GOLD + "Before saving the arena you need to set both corners of the region with the Arena Wand.");
                            return true;
                        }
                        boolean isNew = Setting.getInstance().addArena(arenaName, region);

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

                        break;
                    case "delete":

                        boolean exists = Setting.getInstance().deleteArena(arenaName);

                        if (exists) {
                            p.sendMessage(ChatColor.GREEN + "You deleted arena: " + ChatColor.GOLD + arenaName);
                        } else {
                            p.sendMessage(ChatColor.GOLD + "No arena with this name was found.");
                        }

                        break;
                    case "info":

                        if (!Setting.getInstance().arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }

                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");

                        Location corner1 = config.getLocation("Arenas." + arenaName + ".Corner 1");
                        Location corner2 = config.getLocation("Arenas." + arenaName + ".Corner 2");
                        boolean enabled = config.getBoolean("Arenas." + arenaName + ".Enabled");

                        p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + arenaName);
                        p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(corner1));
                        p.sendMessage(ChatColor.AQUA + "Corner 1: " + ChatColor.GOLD + UM.getInstance().locToString(corner2));
                        p.sendMessage(ChatColor.AQUA + "Enabled: " + ChatColor.GOLD + enabled);

                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                        break;
                    case "disable":
                        if (!Setting.getInstance().arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if(!config.getBoolean("Arenas." + arenaName + ".Enabled")){
                            p.sendMessage(ChatColor.GOLD + "This arena is already disabled.");
                            return true;
                        }
                        config.set("Arenas." + arenaName + ".Enabled", false);
                        Setting.getInstance().saveConfig();
                        Arena.getEnabledArenas().remove(arenaName);

                        p.sendMessage(ChatColor.GREEN + "Arena " + arenaName + " was " + ChatColor.GOLD + "disabled.");
                        break;
                    case "enable":
                        if (!Setting.getInstance().arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if(config.getBoolean("Arenas." + arenaName + ".Enabled")){
                            p.sendMessage(ChatColor.GOLD + "This arena is already enabled.");
                            return true;
                        }
                        config.set("Arenas." + arenaName + ".Enabled", true);
                        Setting.getInstance().saveConfig();

                        Arena arena = new Arena(arenaName);
                        Arena.getEnabledArenas().put(arenaName, arena);

                        p.sendMessage(ChatColor.GREEN + "Arena " + arenaName + " was " + ChatColor.GOLD + "enabled.");
                        break;
                    default:
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                        break;
                }

            } else if (L == 3) {

                String oldArenaName = args[1];
                String newArenaName = args[2];

                switch (args[0].toLowerCase()) {
                    case "changename":

                        List<String> created = Setting.getInstance().getCreatedArenas();
                        if (!created.contains(oldArenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if (created.contains(newArenaName)) {
                            p.sendMessage(ChatColor.RED + "An arena with this name already exists.");
                            return true;
                        }
                        Setting.getInstance().changeArenaName(oldArenaName, newArenaName);

                        p.sendMessage(ChatColor.GOLD + oldArenaName + " arena" + ChatColor.GREEN + " was renamed to " + ChatColor.GOLD + newArenaName);

                        break;
                    default:
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                        break;
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

            } else if (L == 2) {

                final String A1 = args[0].toLowerCase();

                List<String> createdArenas = Setting.getInstance().getCreatedArenas();

                switch (A1) {
                    case "changename", "delete", "info" -> l.addAll(createdArenas);
                    case "enable" -> {
                        for (String arena : createdArenas) {
                            if (!Setting.getInstance().getConfig().getBoolean("Arenas." + arena + ".Enabled"))
                                l.add(arena);
                        }
                    }
                    case "disable" -> {
                        for (String arena : createdArenas) {
                            if (Setting.getInstance().getConfig().getBoolean("Arenas." + arena + ".Enabled"))
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

        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
    }
}
