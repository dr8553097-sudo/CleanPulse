package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

import java.util.concurrent.atomic.AtomicInteger;

public class GameAIOptimizer implements Listener {
    private final CleanPulse plugin;
    private int taskScheduleId = -1;
    private final AtomicInteger optimizedMobsCount = new AtomicInteger(0);

    public GameAIOptimizer(CleanPulse plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        if (taskScheduleId != -1) {
            Bukkit.getScheduler().cancelTask(taskScheduleId);
        }
        if (!plugin.getConfig().getBoolean("ai-optimizer.enabled", true)) return;

        int interval = plugin.getConfig().getInt("ai-optimizer.scan-interval-ticks", 100);

        taskScheduleId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    Entity[] entities = chunk.getEntities();
                    if (entities.length < 5) continue;

                    int animalCluster = 0;
                    for (Entity e : entities) {
                        if (e instanceof Animals) animalCluster++;
                    }

                    for (Entity e : entities) {
                        if (e instanceof Mob mob) {
                            if (mob instanceof Boss || mob.customName() != null || (mob instanceof Tameable t && t.isTamed())) continue;

                            // 1. Stuck Pathfinding Suppression (Pens / Grinders)
                            if (mob.getVelocity().lengthSquared() < 0.001 && mob.getLocation().getBlock().getType().isSolid()) {
                                if (mob.hasAI()) {
                                    mob.setAware(false);
                                    count++;
                                }
                            }

                            // 2. Animal Look & Wander Suppression in Packed Pens
                            if (animalCluster > 8 && mob instanceof Animals animal) {
                                if (animal.isAware()) {
                                    animal.setAware(false);
                                    count++;
                                }
                            }

                            // 3. Villager Memory / Gossip Pruner
                            if (mob instanceof Villager villager) {
                                if (villager.getVillagerLevel() >= 2 && !villager.isTrading()) {
                                    villager.setAware(false);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
            optimizedMobsCount.set(count);
        }, interval, interval);
    }

    public int getOptimizedMobsCount() {
        return optimizedMobsCount.get();
    }
}
