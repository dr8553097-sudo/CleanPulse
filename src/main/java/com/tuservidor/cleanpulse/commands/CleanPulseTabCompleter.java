package com.tuservidor.cleanpulse.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CleanPulseTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList("gui", "pulse", "blame", "myfarm", "restore", "filter", "death", "reload", "lang");
            List<String> res = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) res.add(s);
            }
            return res;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("lang")) {
                return Arrays.asList("en", "es", "fr", "pt");
            } else if (args[0].equalsIgnoreCase("restore")) {
                List<String> list = new ArrayList<>();
                list.add("all");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    list.add(p.getName());
                }
                return list;
            }
        }
        return List.of();
    }
}
