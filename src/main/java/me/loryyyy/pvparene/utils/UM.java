package me.loryyyy.pvparene.utils;

import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UM {

    @Getter
    private static final UM instance = new UM();

    private UM(){}

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

}
