package com.tuservidor.cleanpulse.commands;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CleanPulseCommand implements CommandExecutor {
    private final CleanPulse plugin;

    public CleanPulseCommand(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§x§9§d§4§e§d§d§l⚡ §x§c§7§7§d§f§f§lClean§x§e§0§a§a§f§f§lPulse §7v" + plugin.getDescription().getVersion() + " por §fDafealru");
            sender.sendMessage("§7• §d/cp pulse §8- §7Ejecutar limpieza manual");
            sender.sendMessage("§7• §d/cp inspect §8- §7Activar mapa de calor con partículas");
            sender.sendMessage("§7• §d/cp monitor §8- §7Ver estado de TPS y memoria");
            sender.sendMessage("§7• §d/cp reload §8- §7Recargar configuraciones");
            sender.sendMessage("§7• §d/trash §8- §7Abrir papelera de descarte");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "pulse", "clear" -> {
                if (!sender.hasPermission("cleanpulse.pulse")) {
                    plugin.getLangManager().send(sender, "admin.no-permission");
                    return true;
                }
                plugin.getPulseManager().executePulse(sender, false);
                plugin.getLangManager().send(sender, "pulse.manual-triggered", Map.of("sender", sender.getName()));
            }
            case "inspect", "chunk" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cSolo jugadores en juego pueden usar el inspector visual.");
                    return true;
                }
                if (!player.hasPermission("cleanpulse.inspect")) {
                    plugin.getLangManager().send(player, "admin.no-permission");
                    return true;
                }
                plugin.getLagInspectorManager().runInspect(player);
            }
            case "monitor", "stats" -> {
                if (!sender.hasPermission("cleanpulse.monitor")) {
                    plugin.getLangManager().send(sender, "admin.no-permission");
                    return true;
                }
                double[] tps = Bukkit.getTPS();
                double currentTps = (tps != null && tps.length > 0) ? Math.min(20.0, tps[0]) : 20.0;
                int health = (int) ((currentTps / 20.0) * 100);

                Runtime r = Runtime.getRuntime();
                long usedRam = (r.totalMemory() - r.freeMemory()) / 1048576L;
                long maxRam = r.maxMemory() / 1048576L;

                int totalEntities = Bukkit.getWorlds().stream().mapToInt(w -> w.getEntities().size()).sum();

                sender.sendMessage(plugin.getLangManager().get("monitor.title"));
                sender.sendMessage(plugin.getLangManager().get("monitor.tps", Map.of("tps", String.format("%.2f", currentTps), "health", String.valueOf(health))));
                sender.sendMessage(plugin.getLangManager().get("monitor.ram", Map.of("used_ram", String.valueOf(usedRam), "max_ram", String.valueOf(maxRam))));
                sender.sendMessage(plugin.getLangManager().get("monitor.worlds", Map.of("worlds", String.valueOf(Bukkit.getWorlds().size()))));
                sender.sendMessage(plugin.getLangManager().get("monitor.total-entities", Map.of("entities", String.valueOf(totalEntities))));
            }
            case "reload" -> {
                if (!sender.hasPermission("cleanpulse.admin")) {
                    plugin.getLangManager().send(sender, "admin.no-permission");
                    return true;
                }
                long start = System.currentTimeMillis();
                plugin.reloadAll();
                long duration = System.currentTimeMillis() - start;
                plugin.getLangManager().send(sender, "admin.reloaded", Map.of("ms", String.valueOf(duration)));
            }
            default -> sender.sendMessage("§cComando desconocido. Usa /cp para ver la ayuda.");
        }
        return true;
    }
}
