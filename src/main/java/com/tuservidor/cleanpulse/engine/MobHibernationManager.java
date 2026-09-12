package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MobHibernationManager implements Listener {
    private final CleanPulse plugin;
    private int taskId = -1;

    public MobHibernationManager(CleanPulse plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        if (!plugin.getConfig().getBoolean("mob-hibernation.enabled", true)) return;

        int interval = plugin.getConfig().getInt("mob-hibernation.scan-interval-seconds", 10) * 20;
        int threshold = plugin.getConfig().getInt("mob-hibernation.density-threshold", 12);
        double wakeRadius = plugin.getConfig().getDouble("mob-hibernation.wake-radius", 8.0);
        double wakeSq = wakeRadius * wakeRadius;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    Entity[] entities = chunk.getEntities();
                    if (entities.length < threshold) continue;

                    for (Entity ent : entities) {
                        if (!(ent instanceof Mob mob)) continue;
                        if (mob instanceof Boss || mob instanceof Villager || mob instanceof Player) continue;
                        if (mob.customName() != null && plugin.getConfig().getBoolean("mob-hibernation.protect-named-mobs", true)) continue;
                        if (mob instanceof Tameable tameable && tameable.isTamed() && plugin.getConfig().getBoolean("mob-hibernation.protect-tamed-mobs", true)) continue;

                        boolean playerNear = false;
                        for (Player p : world.getPlayers()) {
                            if (p.getLocation().distanceSquared(mob.getLocation()) <= wakeSq) {
                                playerNear = true;
                                break;
                            }
                        }

                        if (!playerNear && mob.hasAI()) {
                            mob.setAI(false);
                        } else if (playerNear && !mob.hasAI()) {
                            mob.setAI(true);
                        }
                    }
                }
            }
        }, interval, interval);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Mob mob && !mob.hasAI()) {
            mob.setAI(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Mob mob && !mob.hasAI()) {
            mob.setAI(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (e.getEntity() instanceof Mob mob && !mob.hasAI()) {
            mob.setAI(true);
        }
    }
}
