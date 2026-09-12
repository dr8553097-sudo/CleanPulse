package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

public class VillagerOptimizer {
    private final CleanPulse plugin;

    public VillagerOptimizer(CleanPulse plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("villager-optimizer.enabled", true)) return;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (World w : Bukkit.getWorlds()) {
                for (Entity e : w.getEntities()) {
                    if (e instanceof Villager v && v.isValid()) {
                        if (v.getVillagerLevel() >= 2 && isEnclosed(v)) {
                            if (v.hasAI()) {
                                v.setAware(false);
                            }
                        } else if (!v.hasAI()) {
                            v.setAware(true);
                        }
                    }
                }
            }
        }, 200L, 200L);
    }

    private boolean isEnclosed(Villager v) {
        Block b = v.getLocation().getBlock();
        return b.getRelative(1, 0, 0).getType().isSolid()
                && b.getRelative(-1, 0, 0).getType().isSolid()
                && b.getRelative(0, 0, 1).getType().isSolid()
                && b.getRelative(0, 0, -1).getType().isSolid();
    }
}
