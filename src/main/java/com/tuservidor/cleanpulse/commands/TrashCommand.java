package com.tuservidor.cleanpulse.commands;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrashCommand implements CommandExecutor {
    private final CleanPulse plugin;

    public TrashCommand(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSolo jugadores pueden abrir la papelera.");
            return true;
        }
        if (!player.hasPermission("cleanpulse.trash")) {
            plugin.getLangManager().send(player, "admin.no-permission");
            return true;
        }
        plugin.getTrashMenu().open(player);
        return true;
    }
}
