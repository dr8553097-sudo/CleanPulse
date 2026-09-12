package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedstoneSentinelManager implements Listener {
    private final CleanPulse plugin;
    private final Map<Location, Integer> pulseCounts = new ConcurrentHashMap<>();

    public RedstoneSentinelManager(CleanPulse plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, pulseCounts::clear, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRedstone(BlockRedstoneEvent e) {
        if (!plugin.getConfig().getBoolean("redstone-sentinel.enabled", true)) return;
        Block b = e.getBlock();
        Location loc = b.getLocation();

        int count = pulseCounts.merge(loc, 1, Integer::sum);
        int maxHz = plugin.getConfig().getInt("redstone-sentinel.max-frequency-hz", 14);

        if (count > maxHz) {
            e.setNewCurrent(0);
            if (plugin.getConfig().getBoolean("redstone-sentinel.auto-freeze", true)) {
                if (b.getType() == Material.REDSTONE_WIRE || b.getType() == Material.REDSTONE_TORCH || b.getType() == Material.REPEATER) {
                    b.setType(Material.LEVER);
                    if (plugin.getConfig().getBoolean("redstone-sentinel.particle-cue", true)) {
                        b.getWorld().spawnParticle(Particle.SMOKE, loc.add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.05);
                        b.getWorld().playSound(loc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 0.8f);
                    }
                    plugin.getLogger().warning("Redstone Sentinel froze runaway clock at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                }
            }
        }
    }
}
