package me.loryyyy.pvparena.utils;

import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UM {

    @Getter
    private static final UM instance = new UM();

    private UM(){}

    public String capitalize(String s){
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    public ArrayList<String> filterList(ArrayList<String> list, String[] args) {
        list.removeIf(s -> !s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
        return list;
    }

    public ArrayList<String> getCommaList(FileConfiguration config, String configPlace, boolean lowerCase) {
        String s = config.getString(configPlace);
        if (s == null || s.isEmpty()) {
            return new ArrayList<>();
        }
        if (lowerCase) return new ArrayList<>(Arrays.asList(s.toLowerCase().split(", ")));
        return new ArrayList<>(Arrays.asList(s.split(", ")));
    }

    public void addToCommaList(FileConfiguration config, String configPlace, String value) {
        String s = config.getString(configPlace);
        ArrayList<String> list;
        if (s != null && !s.isEmpty()) {
            String[] array = s.split(", ");
            list = new ArrayList<>(Arrays.asList(array));
        } else {
            list = new ArrayList<>();
        }
        list.add(value);
        String finalString = list.toString().replace("[", "").replace("]", "");
        config.set(configPlace, finalString);
    }

    public void removeFromCommaList(FileConfiguration config, String configPlace, String value) {
        String s = config.getString(configPlace);
        if (s == null) return;
        String[] array = s.split(", ");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(array));
        list.remove(value);
        String finalString = list.toString().replace("[", "").replace("]", "");
        config.set(configPlace, finalString);
    }

    public String approximate(double d, int digits) {
        DecimalFormat df = new DecimalFormat("##########." + "#".repeat(Math.max(0, digits)));
        return df.format(d);
    }

    public String locToString(Location loc, boolean extended){

        if(!extended) return locToString(loc);

        return "x = " + approximate(loc.getX(), 2) + ", y = " + approximate(loc.getY(), 2) + ", z = " + approximate(loc.getZ(), 2) + ", pitch = " +
                approximate(loc.getPitch(), 2) + ", yaw = " +  approximate(loc.getYaw(), 2) + ", world = " + loc.getWorld().getName();

    }
    public String locToString(Location loc){

        return "x = " + approximate(loc.getX(), 2) + ", y = " + approximate(loc.getY(), 2) + ", z = " + approximate(loc.getZ(), 2);

    }

    public boolean isIn(double min, double max, double number) {
        return min <= number && number <= max;
    }
    public void sendUsageOfCommand(String usage, String description, CommandSender sender){

        if(!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.GOLD + usage + " " + ChatColor.AQUA + description);
            return;
        }

        TextComponent component = new TextComponent(ChatColor.GOLD + usage + " " + ChatColor.AQUA + description);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to put the command in the chat").color(net.md_5.bungee.api.ChatColor.GRAY).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage));
        p.spigot().sendMessage(component);

    }

    public ItemStack createItem(Material material, String displayName, List<String> lore, Inventory inventory, int index, int amount) {
        return createItem(material, 0, displayName, lore, inventory, index, amount);

    }
    public ItemStack createItem(Material material, int dur, String displayName, List<String> lore, int amount) {
        ItemStack item = new ItemStack(material);
        item.setDurability((short) dur);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(displayName);
        meta.setLore(lore == null ? new ArrayList<>() : lore);
        item.setItemMeta(meta);
        item.setAmount(amount);

        return item;
    }
    public ItemStack createItem(Material material, String displayName, List<String> lore, int amount) {
        return createItem(material, 0, displayName, lore, amount);
    }

    public ItemStack createItem(Material material, int dur, String displayName, List<String> lore, Inventory inventory, int index, int amount) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(displayName);
        meta.setLore(lore == null ? new ArrayList<>() : lore);
        item.setItemMeta(meta);
        item.setDurability((byte) dur);
        item.setAmount(amount);
        if (inventory.getSize() == 36) {
            if (index < 36) {
                inventory.setItem(index, item);
            } else {
                PlayerInventory inv = (PlayerInventory) inventory;
                if (index == 36) {
                    inv.setBoots(item);
                }
                else if (index == 37) {
                    inv.setLeggings(item);
                }
                else if (index == 38) {
                    inv.setChestplate(item);
                }
                else if (index == 39) {
                    inv.setHelmet(item);
                }
            }
        } else {
            inventory.setItem(index, item);
        }
        return item;

    }
}
