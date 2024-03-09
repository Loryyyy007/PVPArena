package me.loryyyy.pvparena.commands;

import lombok.Getter;
import me.loryyyy.pvparena.PVPArena;
import me.loryyyy.pvparena.files.Messages;
import me.loryyyy.pvparena.files.Setting;
import me.loryyyy.pvparena.managers.ArenaCheckTask;
import me.loryyyy.pvparena.utils.*;
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

            if(hasNoPerm(p)) return Messages.getInstance().sendNoPermMessage(p);

            final int L = args.length;
            Setting setting = Setting.getInstance();
            FileConfiguration setConfig = setting.getConfig();
            FileConfiguration config = PVPArena.getInstance().getConfig();

            if (L == 0) {

                if(!hasPerm(p, "arena.which")) return Messages.getInstance().sendNoPermMessage(p);

                Map<Player, Arena> playersInArena = ArenaCheckTask.getInstance().getPlayersInArena();
                String message;
                if (playersInArena.containsKey(p)) {
                    message = Messages.getInstance().getMessage(ConstantPaths.ARENA_WHERE_MESSAGE).replace("<arena>", playersInArena.get(p).getName());
                } else {
                    message = Messages.getInstance().getMessage(ConstantPaths.NOT_IN_ARENA_MESSAGE);
                }
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            } else if (L == 1) {

                switch (args[0].toLowerCase()) {
                    case "wand", "w" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        ItemStack wand = UM.getInstance().createItem(Material.IRON_AXE, ChatColor.GOLD + "Arena Wand", Arrays.asList(" ",
                                ChatColor.YELLOW + "Left-click a block to set pos1", ChatColor.YELLOW + "Right-click a block to set pos2", ChatColor.YELLOW + "Drop to move the region",
                                ChatColor.YELLOW + "Right-click air to expand.", ChatColor.YELLOW + "Shift+Right-click air to reduce."), 1);
                        wand.addEnchantment(Enchantment.DURABILITY, 1);
                        p.getInventory().addItem(wand);
                        p.sendMessage(ChatColor.GOLD + "Arena Wand " + ChatColor.GREEN + "has been added to your inventory.");
                    }
                    case "usage", "us" -> showUsage(p);
                    case "reload", "rl" -> {
                        if(!hasPerm(p, "arena.reload")) return Messages.getInstance().sendNoPermMessage(p);

                        PVPArena.getInstance().reload();
                        p.sendMessage(ChatColor.GREEN + "The plugin was reloaded.");
                    }
                    case "confreload", "cfrl" -> {
                        if(!hasPerm(p, "arena.configs-reload")) return Messages.getInstance().sendNoPermMessage(p);

                        Setting.getInstance().reloadConfig();
                        Messages.getInstance().reloadConfig();
                        PVPArena.getInstance().reloadConfig();
                        p.sendMessage(ChatColor.GREEN + "All config files were reloaded.");
                    }
                    case "info", "i" -> {
                        if(!hasPerm(p, "arena.info")) return Messages.getInstance().sendNoPermMessage(p);

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
                    case "disable", "dis" -> {
                        if(!hasPerm(p, "arena.enable")) return Messages.getInstance().sendNoPermMessage(p);

                        if (!ArenaCheckTask.getInstance().isTaskEnabled()) {
                            p.sendMessage(ChatColor.GOLD + "The arena check task is already disabled.");
                            return true;
                        }
                        ArenaCheckTask.getInstance().setTaskEnabled(false);
                        config.set(ConstantPaths.TASK_ENABLED, false);
                        PVPArena.getInstance().saveConfig();

                        ArenaCheckTask.getInstance().cancel();
                        ArenaCheckTask.getInstance().getPlayersInArena().clear();
                        p.sendMessage(ChatColor.GOLD + "Arena check task was disabled.");
                    }
                    case "enable", "en" -> {
                        if(!hasPerm(p, "arena.enable")) return Messages.getInstance().sendNoPermMessage(p);

                        if (ArenaCheckTask.getInstance().isTaskEnabled()) {
                            p.sendMessage(ChatColor.GOLD + "The arena check task is already enabled.");
                            return true;
                        }
                        ArenaCheckTask.getInstance().setTaskEnabled(true);
                        config.set(ConstantPaths.TASK_ENABLED, true);
                        PVPArena.getInstance().saveConfig();

                        ArenaCheckTask.getInstance().start();
                        p.sendMessage(ChatColor.GREEN + "Arena check task was enabled.");
                    }
                    case "pos1", "pos2" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        selectedRegions.putIfAbsent(p, new Region());
                        Region region = selectedRegions.get(p);

                        Location blockLoc = p.getLocation().getBlock().getLocation();

                        if (args[0].equalsIgnoreCase("pos1"))
                            region.setCorner1(blockLoc);
                        else region.setCorner2(blockLoc);

                        region.updateVisualEffect(p);

                        p.sendMessage(ChatColor.GREEN + UM.getInstance().capitalize(args[0].toLowerCase()) + " of the region was set to: " + ChatColor.GOLD + UM.getInstance().locToString(blockLoc));
                    }
                    default -> {
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                    }
                }

            } else if (L == 2) {

                String arenaName = args[1];

                switch (args[0].toLowerCase()) {
                    case "save", "sv" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

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
                        p.sendMessage(ChatColor.AQUA + "Enabled: " + ChatColor.GOLD + config.getBoolean(ConstantPaths.ARENA_CREATION_ENABLED));
                        p.sendMessage(ChatColor.YELLOW + "-------------------------------------");

                    }
                    case "delete", "del" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        boolean exists = setting.deleteArena(arenaName);

                        if (exists) {
                            p.sendMessage(ChatColor.GREEN + "You deleted arena: " + ChatColor.GOLD + arenaName);
                        } else {
                            p.sendMessage(ChatColor.GOLD + "No arena with this name found.");
                        }

                    }
                    case "info", "i" -> {
                        if(!hasPerm(p, "arena.info")) return Messages.getInstance().sendNoPermMessage(p);

                        if (!setting.arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena with this name found.");
                            return true;
                        }

                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");

                        setting.sendInfoOfArena(p, arenaName);

                        p.sendMessage(ChatColor.LIGHT_PURPLE + "==================================");
                    }
                    case "disable", "dis" -> {
                        if(!hasPerm(p, "arena.enable")) return Messages.getInstance().sendNoPermMessage(p);

                        if (!setting.arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena with this name found.");
                            return true;
                        }
                        if (!setConfig.getBoolean(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED)) {
                            p.sendMessage(ChatColor.GOLD + "This arena is already disabled.");
                            return true;
                        }
                        setting.disableArena(arenaName);

                        p.sendMessage(ChatColor.GREEN + "Arena " + ChatColor.GOLD + arenaName + ChatColor.GREEN + " was disabled.");
                    }
                    case "enable", "en" -> {
                        if(!hasPerm(p, "arena.enable")) return Messages.getInstance().sendNoPermMessage(p);

                        if (!setting.arenaExists(arenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena found with this name.");
                            return true;
                        }
                        if (setConfig.getBoolean(ConstantPaths.ARENA_SETTING + arenaName + ConstantPaths.ARENA_ENABLED)) {
                            p.sendMessage(ChatColor.GOLD + "This arena is already enabled.");
                            return true;
                        }
                        setting.enableArena(arenaName);

                        p.sendMessage(ChatColor.GREEN + "Arena " + ChatColor.GOLD + arenaName + ChatColor.GREEN + " was enabled.");
                    }
                    case "expand", "exp" -> p.performCommand("arena expand " + args[1] + " " + CardinalDirection.getDirection(p).name());
                    case "reduce", "rdc" -> p.performCommand("arena reduce " + args[1] + " " + CardinalDirection.getDirection(p).name());
                    case "move", "mv" -> p.performCommand("arena move " + args[1] + " " + CardinalDirection.getDirection(p).name());
                    default -> {
                        p.sendMessage(ChatColor.RED + "Unknown argument: " + args[0]);
                        showUsage(p);
                    }
                }

            } else if (L == 3) {

                String oldArenaName = args[1];
                String newArenaName = args[2];

                switch (args[0].toLowerCase()) {
                    case "changename", "chn" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        if (!setting.arenaExists(oldArenaName)) {
                            p.sendMessage(ChatColor.RED + "No arena with this name found.");
                            return true;
                        }
                        if (setting.arenaExists(newArenaName)) {
                            p.sendMessage(ChatColor.RED + "An arena with this name already exists.");
                            return true;
                        }
                        setting.changeArenaName(oldArenaName, newArenaName);

                        p.sendMessage(ChatColor.GOLD + oldArenaName + " arena" + ChatColor.GREEN + " was renamed to " + ChatColor.GOLD + newArenaName);
                    }
                    case "expand", "exp" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        Region region = selectedRegions.getOrDefault(p, null);

                        if(region == null){
                            p.sendMessage(ChatColor.GOLD + "You have not selected any region.");
                            return true;
                        }
                        if (region.getCorner1() == null || region.getCorner2() == null) {
                            p.sendMessage(ChatColor.GOLD + "You have to finish the region selection before expanding it.");
                            return true;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[1]);
                        }catch (NumberFormatException ex){
                            p.sendMessage(ChatColor.RED  + "You need to provide a valid amount.");
                            showUsage(p);
                            return true;
                        }
                        CardinalDirection direction;
                        try{
                            direction = CardinalDirection.valueOf(args[2]);
                        }catch (IllegalArgumentException ex){
                            p.sendMessage(ChatColor.RED + "Unknown direction: " + args[2]);
                            return true;
                        }

                        region.changeSize(direction, amount, true);
                        region.updateVisualEffect(p);

                        p.sendMessage(ChatColor.GREEN + "The region was expanded by " + ChatColor.GOLD + amount + " blocks " + ChatColor.GREEN + "to " + direction.name());
                    }
                    case "reduce", "rdc" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        Region region = selectedRegions.getOrDefault(p, null);

                        if(region == null){
                            p.sendMessage(ChatColor.GOLD + "You have not selected any region.");
                            return true;
                        }
                        if (region.getCorner1() == null || region.getCorner2() == null) {
                            p.sendMessage(ChatColor.GOLD + "You have to finish the region selection before reducing it.");
                            return true;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[1]);
                        }catch (NumberFormatException ex){
                            p.sendMessage(ChatColor.RED  + "You need to provide a valid amount.");
                            showUsage(p);
                            return true;
                        }
                        CardinalDirection direction;
                        try{
                            direction = CardinalDirection.valueOf(args[2]);
                        }catch (IllegalArgumentException ex){
                            p.sendMessage(ChatColor.RED + "Unknown direction: " + args[2]);
                            return true;
                        }

                        region.changeSize(direction, amount, false);
                        region.updateVisualEffect(p);

                        p.sendMessage(ChatColor.GREEN + "The region was reduced by " + ChatColor.GOLD + amount + " blocks " + ChatColor.GREEN + "to " + direction.name());
                    }case "move", "mv" -> {
                        if(!hasPerm(p, "arena.set")) return Messages.getInstance().sendNoPermMessage(p);

                        Region region = selectedRegions.getOrDefault(p, null);

                        if(region == null){
                            p.sendMessage(ChatColor.GOLD + "You have not selected any region.");
                            return true;
                        }
                        if (region.getCorner1() == null || region.getCorner2() == null) {
                            p.sendMessage(ChatColor.GOLD + "You have to finish the region selection before moving it.");
                            return true;
                        }
                        int amount;
                        try {
                            amount = Integer.parseInt(args[1]);
                        }catch (NumberFormatException ex){
                            p.sendMessage(ChatColor.RED  + "You need to provide a valid amount.");
                            showUsage(p);
                            return true;
                        }

                        CardinalDirection direction;
                        try{
                            direction = CardinalDirection.valueOf(args[2]);
                        }catch (IllegalArgumentException ex){
                            p.sendMessage(ChatColor.RED + "Unknown direction: " + args[2]);
                            return true;
                        }
                        region.move(direction, amount);
                        region.updateVisualEffect(p);

                        p.sendMessage(ChatColor.GREEN + "Region has been moved by " + ChatColor.GOLD + amount + ChatColor.GREEN + " to " + ChatColor.GOLD + direction.name());
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
        FileConfiguration config = PVPArena.getInstance().getConfig();

        if (sender instanceof Player p) {
            final int L = args.length;

            if (L == 1) {

                l.add("usage");
                if(hasPerm(p, "arena.reload"))
                    l.add("reload");
                if(hasPerm(p, "arena.configs-reload"))
                    l.add("confReload");
                if(hasPerm(p, "arena.set")){
                    l.add("wand");
                    l.add("save");
                    l.add("changeName");
                    l.add("delete");
                    l.add("pos1");
                    l.add("pos2");
                    l.add("move");
                    l.add("expand");
                    l.add("reduce");
                }
                if(hasPerm(p, "arena.info"))
                    l.add("info");
                if(hasPerm(p, "arena.enable")){
                    l.add("enable");
                    l.add("disable");
                }

            } else if (L == 2) {

                final String A1 = args[0].toLowerCase();

                List<String> createdArenas = Setting.getInstance().getCreatedArenas();

                switch (A1) {
                    case "changename", "delete", "info", "chn", "del", "i" -> {
                        if(hasPerm(p, "arena.info") || hasPerm(p, "arena.set"))
                            l.addAll(createdArenas);
                    }
                    case "enable", "en" -> {
                        if(hasPerm(p, "arena.enable")) {
                            for (String arena : createdArenas) {
                                if (!Setting.getInstance().getConfig().getBoolean(ConstantPaths.ARENA_SETTING + arena + ConstantPaths.ARENA_ENABLED))
                                    l.add(arena);
                            }
                        }
                    }
                    case "disable", "dis" -> {
                        if(hasPerm(p, "arena.enable")) {
                            for (String arena : createdArenas) {
                                if (Setting.getInstance().getConfig().getBoolean(ConstantPaths.ARENA_SETTING + arena + ConstantPaths.ARENA_ENABLED))
                                    l.add(arena);
                            }
                        }
                    }case "expand", "exp", "reduce", "rdc" -> l.add(config.getInt(ConstantPaths.EXPANDING_REDUCING_AMOUNT) + "");
                    case "move", "mv" -> l.add(config.getInt(ConstantPaths.MOVING_AMOUNT) + "");
                }

            }else if(L == 3){

                switch (args[0].toLowerCase()){

                    case "expand", "exp", "move", "mv", "reduce", "rdc" -> {
                        for(CardinalDirection direction : CardinalDirection.values()){
                            l.add(direction.name());
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

        if(hasPerm(p, "arena.which"))
            um.sendUsageOfCommand("/arena", "shows the arena you are currently in.", p);
        if(hasPerm(p, "arena.set")){
            um.sendUsageOfCommand("/arena wand", "gives the tool to select regions for the arenas.", p);
            um.sendUsageOfCommand("/arena save <arenaName>", "saves an arena as the selected region.", p);
            um.sendUsageOfCommand("/arena changeName <oldArenaName> <newArenaName>", "changes the name of an arena.", p);
            um.sendUsageOfCommand("/arena delete <arenaName>", "deletes an arena.", p);
            um.sendUsageOfCommand("/arena pos1", "sets pos1 to your current location.", p);
            um.sendUsageOfCommand("/arena pos2", "sets pos2 to your current location.", p);
            um.sendUsageOfCommand("/arena move <amount> (<direction>)", "moves the region in a certain direction.", p);
            um.sendUsageOfCommand("/arena expand <amount> (<direction>)", "expands the region in a certain direction.", p);
            um.sendUsageOfCommand("/arena reduce <amount> (<direction>)", "reduces the region in a certain direction.", p);
        }
        if(hasPerm(p, "arena.enable")){
            um.sendUsageOfCommand("/arena enable", "enables the enter/exit arena check.", p);
            um.sendUsageOfCommand("/arena disable", "disables the enter/exit arena check.", p);
            um.sendUsageOfCommand("/arena enable <arenaName>", "enables a certain arena.", p);
            um.sendUsageOfCommand("/arena disable <arenaName>", "disables a certain arena.", p);
        }
        if(hasPerm(p, "arena.info")){
            um.sendUsageOfCommand("/arena info", "shows some info of all arenas.", p);
            um.sendUsageOfCommand("/arena info <arenaName>", "shows some info of a certain arena.", p);
        }
        if(hasPerm(p, "arena.reload"))
            um.sendUsageOfCommand("/arena reload", "reloads the plugin.", p);
        if(hasPerm(p, "arena.configs-reload"))
            um.sendUsageOfCommand("/arena confReload", "reloads all configs.", p);


        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
    }

    private boolean hasNoPerm(Player p){
        return !p.hasPermission("arena.*") && !p.hasPermission("arena.set") && !p.hasPermission("arena.enable") && !p.hasPermission("arena.info")
                && !p.hasPermission("arena.which") && !p.hasPermission("arena.configs-reload") && !p.hasPermission("arena.reload");
    }
    private boolean hasPerm(Player p, String perm){
        return p.hasPermission("arena.*") || p.hasPermission(perm);
    }
}
