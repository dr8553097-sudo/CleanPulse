package com.tuservidor.cleanpulse.inspector;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class LagInspectorManager {
    private final CleanPulse plugin;

    public LagInspectorManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    public void runInspect(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        
        int hoppers = 0;
        for (BlockState tile : chunk.getTileEntities()) {
            if (tile instanceof Hopper) {
                hoppers++;
            }
        }

        Entity[] entities = chunk.getEntities();
        int livingCount = 0;
        int itemCount = 0;

        for (Entity e : entities) {
            if (e instanceof LivingEntity && !(e instanceof Player)) {
                livingCount++;
            } else if (e instanceof org.bukkit.entity.Item) {
                itemCount++;
            }
        }

        double estimatedMs = (hoppers * 0.04) + (livingCount * 0.08) + (itemCount * 0.01);
        String status = hoppers > 30 ? "§cALTO" : hoppers > 10 ? "§eMEDIO" : "§aBAJO";
        String load = livingCount > 40 ? "§cALTA" : "§aNORMAL";

        player.sendMessage(plugin.getLangManager().get("inspector.header"));
        player.sendMessage(plugin.getLangManager().get("inspector.title", Map.of("chunk_x", String.valueOf(chunk.getX()), "chunk_z", String.valueOf(chunk.getZ()))));
        player.sendMessage(plugin.getLangManager().get("inspector.hoppers", Map.of("hoppers", String.valueOf(hoppers), "status", status)));
        player.sendMessage(plugin.getLangManager().get("inspector.entities", Map.of("entities", String.valueOf(livingCount), "load", load)));
        player.sendMessage(plugin.getLangManager().get("inspector.items", Map.of("items", String.valueOf(itemCount))));
        player.sendMessage(plugin.getLangManager().get("inspector.ms-cost", Map.of("ms", String.format("%.2f", estimatedMs))));
        player.sendMessage(plugin.getLangManager().get("inspector.footer"));

        player.sendMessage(plugin.getLangManager().get("inspector.started"));
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= 200) {
                    cancel();
                    return;
                }

                for (BlockState tile : chunk.getTileEntities()) {
                    if (tile instanceof Hopper) {
                        Location loc = tile.getLocation().add(0.5, 1.2, 0.5);
                        player.spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                for (Entity e : chunk.getEntities()) {
                    if (e instanceof LivingEntity && !(e instanceof Player)) {
                        Location loc = e.getLocation().add(0, e.getHeight() + 0.3, 0);
                        player.spawnParticle(Particle.HAPPY_VILLAGER, loc, 2, 0.2, 0.1, 0.2, 0);
                    }
                }

                ticks += 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
