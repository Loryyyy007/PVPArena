package me.loryyyy.pvparene.commands;

import me.loryyyy.pvparene.utils.UM;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {



        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        ArrayList<String> l = new ArrayList<>();


        return l;
    }

    private void sendUsage(Player p){
        UM um = UM.getInstance();

        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Arena command usages:");

        um.sendUsageOfCommand("/arena ", "", p);

        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
    }
}
