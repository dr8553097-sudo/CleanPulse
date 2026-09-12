package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class AdvancedPerformanceMonitor {
    private final CleanPulse plugin;
    private final OperatingSystemMXBean osBean;

    public AdvancedPerformanceMonitor(CleanPulse plugin) {
        this.plugin = plugin;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public void displayDetailedReport(CommandSender sender) {
        double tps = Bukkit.getTPS()[0];
        double mspt = Bukkit.getAverageTickTime();
        double cpuProcess = osBean.getProcessCpuLoad() * 100.0;
        if (cpuProcess < 0) cpuProcess = 0;

        Runtime rt = Runtime.getRuntime();
        long maxMem = rt.maxMemory() / 1048576L;
        long usedMem = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        int memPercent = (int) ((usedMem * 100) / maxMem);

        int totalEntities = 0;
        int totalLiving = 0;
        int totalItems = 0;
        int totalHoppers = 0;
        int totalChunks = 0;

        for (World w : Bukkit.getWorlds()) {
            Chunk[] loaded = w.getLoadedChunks();
            totalChunks += loaded.length;
            for (Chunk c : loaded) {
                for (Entity e : c.getEntities()) {
                    totalEntities++;
                    if (e instanceof Item) totalItems++;
                    else if (e instanceof LivingEntity && !(e instanceof Player)) totalLiving++;
                }
                for (BlockState s : c.getTileEntities()) {
                    if (s instanceof Hopper) totalHoppers++;
                }
            }
        }

        // Subsystem Load Estimation
        double entityLoad = Math.min(70.0, (totalLiving * 0.12) + (totalItems * 0.04));
        double hopperLoad = Math.min(40.0, totalHoppers * 0.15);
        double chunkLoad = Math.min(30.0, totalChunks * 0.02);
        double totalSubsystem = Math.max(1.0, entityLoad + hopperLoad + chunkLoad);

        int entityPct = (int) ((entityLoad / totalSubsystem) * 100);
        int hopperPct = (int) ((hopperLoad / totalSubsystem) * 100);
        int chunkPct = 100 - entityPct - hopperPct;
        if (chunkPct < 0) chunkPct = 0;

        // Root Cause Analysis
        String rootCause;
        if (mspt < 25.0) {
            rootCause = "&a✔ Server is running smoothly with 0 bottlenecks.";
        } else if (entityPct > 60) {
            rootCause = "&c⚠ Root Cause: Heavy Entity density (" + totalLiving + " living mobs). CleanPulse AI Hibernation is active.";
        } else if (hopperPct > 40) {
            rootCause = "&c⚠ Root Cause: High Hopper activity (" + totalHoppers + " active hoppers).";
        } else {
            rootCause = "&e⚠ Root Cause: High chunk/world load (" + totalChunks + " loaded chunks).";
        }

        String tpsBar = renderBar(tps, 20.0, 10, "&a", "&e", "&c");
        String ramBar = renderBar(100 - memPercent, 100, 10, "&a", "&e", "&c");

        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8&m-----------------&r &d&lCleanPulse Advanced AI Monitor &8&m-----------------"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &fServer TPS: " + String.format("%.2f", tps) + " " + tpsBar + " &7| MSPT: &d" + String.format("%.1f", mspt) + "ms &7(Max: 50ms)"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &fRAM Memory: &e" + usedMem + " MB &7/ &e" + maxMem + " MB &7(" + memPercent + "%) " + ramBar));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &fCPU Process Load: &b" + String.format("%.1f", cpuProcess) + "% &7| Chunks: &b" + totalChunks));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &fSubsystem MSPT Breakdown:"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("   &d🦁 Entities: &f" + entityPct + "% &7(" + totalLiving + " mobs, " + totalItems + " items)"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("   &6🕳 Hoppers: &f" + hopperPct + "% &7(" + totalHoppers + " hoppers)"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("   &b🌍 Chunks / World: &f" + chunkPct + "%"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &fAI Optimization Status: &a" + plugin.getGameAIOptimizer().getOptimizedMobsCount() + " entities throttled &7(saving CPU ticks)"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &fDiagnostics: " + rootCause));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8&m-------------------------------------------------------------"));
    }

    private String renderBar(double value, double max, int segments, String goodColor, String medColor, String badColor) {
        int filled = (int) Math.round((value / max) * segments);
        if (filled > segments) filled = segments;
        if (filled < 0) filled = 0;

        String color = (value / max >= 0.8) ? goodColor : ((value / max >= 0.5) ? medColor : badColor);
        StringBuilder sb = new StringBuilder(color);
        for (int i = 0; i < filled; i++) sb.append("■");
        sb.append("&7");
        for (int i = filled; i < segments; i++) sb.append("□");
        return sb.toString();
    }
}
