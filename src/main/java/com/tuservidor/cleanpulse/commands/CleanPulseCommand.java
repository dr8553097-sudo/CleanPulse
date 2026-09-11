package com.tuservidor.cleanpulse.commands;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            sender.sendMessage("§7• §d/cp pulse §8- §7Trigger manual optimization pulse");
            sender.sendMessage("§7• §d/cp inspect §8- §7Toggle visual particle lag heatmap");
            sender.sendMessage("§7• §d/cp gui §8- §7Open interactive performance dashboard");
            sender.sendMessage("§7• §d/cp monitor §8- §7View live server TPS & RAM status");
            sender.sendMessage("§7• §d/cp filter §8- §7Configure auto-mining junk filter");
            sender.sendMessage("§7• §d/cp death §8- §7Locate protected death drop coords");
            sender.sendMessage("§7• §d/trash §8- §7Open disposal trash bin GUI");
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
                    sender.sendMessage("§cOnly in-game players can use visual lag inspection.");
                    return true;
                }
                if (!player.hasPermission("cleanpulse.inspect")) {
                    plugin.getLangManager().send(player, "admin.no-permission");
                    return true;
                }
                plugin.getLagInspectorManager().runInspect(player);
            }
            case "gui", "panel" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly in-game players can open the GUI dashboard.");
                    return true;
                }
                if (!player.hasPermission("cleanpulse.monitor")) {
                    plugin.getLangManager().send(player, "admin.no-permission");
                    return true;
                }
                plugin.getPerformanceGui().open(player);
            }
            case "filter", "void" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly in-game players can use the auto-filter.");
                    return true;
                }
                plugin.getMiningFilterManager().open(player);
            }
            case "death", "deathpoint" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can check death coordinates.");
                    return true;
                }
                Location loc = plugin.getDeathShieldManager().getLastDeathLocation(player.getUniqueId());
                if (loc != null) {
                    plugin.getLangManager().send(player, "death-shield.location", Map.of(
                            "x", String.valueOf(loc.getBlockX()),
                            "y", String.valueOf(loc.getBlockY()),
                            "z", String.valueOf(loc.getBlockZ()),
                            "time", "5m"
                    ));
                } else {
                    plugin.getLangManager().send(player, "death-shield.no-death");
                }
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
            default -> sender.sendMessage("§cUnknown subcommand. Use /cp for help.");
        }
        return true;
    }
}
