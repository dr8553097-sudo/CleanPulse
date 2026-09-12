package com.tuservidor.cleanpulse.commands;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class TrashCommand implements CommandExecutor {
    private final CleanPulse plugin;

    public TrashCommand(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.getLang().getComponent("commands.only-players"));
            return true;
        }

        if (!p.hasPermission("cleanpulse.trash")) {
            p.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
            return true;
        }

        Inventory trashInv = Bukkit.createInventory(null, 36, LegacyComponentSerializer.legacyAmpersand().deserialize("&8[ &dCleanPulse Trash Bin &8]"));
        p.openInventory(trashInv);
        return true;
    }
}
