package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;

import java.time.Duration;
import java.util.List;

public class PulseManager {
    private final CleanPulse plugin;
    private int currentCountdown = -1;
    private int taskScheduleId = -1;

    public PulseManager(CleanPulse plugin) {
        this.plugin = plugin;
        startAutoScheduler();
    }

    public void startAutoScheduler() {
        if (taskScheduleId != -1) {
            Bukkit.getScheduler().cancelTask(taskScheduleId);
        }
        if (!plugin.getConfig().getBoolean("pulse.auto-pulse.enabled", true)) return;

        int interval = plugin.getConfig().getInt("pulse.auto-pulse.interval-seconds", 300);
        taskScheduleId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            double threshold = plugin.getConfig().getDouble("pulse.auto-pulse.tps-threshold", 19.0);
            boolean onLowTps = plugin.getConfig().getBoolean("pulse.auto-pulse.auto-trigger-on-low-tps", true);
            double currentTps = Bukkit.getTPS()[0];

            if (!onLowTps || currentTps <= threshold) {
                initiatePulseSequence("System-Timer");
            }
        }, interval * 20L, interval * 20L);
    }

    public void initiatePulseSequence(String issuer) {
        if (currentCountdown > 0) return;
        currentCountdown = 10;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int timer = 10;

            @Override
            public void run() {
                if (timer > 0) {
                    List<Integer> alerts = plugin.getConfig().getIntegerList("pulse.announcements.countdown-seconds");
                    if (alerts.contains(timer)) {
                        broadcastCountdown(timer);
                    }
                    timer--;
                } else {
                    executeOptimizationPulse(issuer);
                    currentCountdown = -1;
                    throw new RuntimeException("DONE_PULSE");
                }
            }
        }, 0L, 20L);
    }

    public void broadcastCountdown(int seconds) {
        float pitch = 0.8f + (10 - seconds) * 0.12f;
        Title title = Title.title(
                plugin.getLang().getRawComponent("warnings.title-main"),
                LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getLang().getRaw("warnings.title-sub").replace("{time}", String.valueOf(seconds))),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(600), Duration.ofMillis(300))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfig().getBoolean("pulse.announcements.title-warnings", true)) {
                player.showTitle(title);
            }
            if (plugin.getConfig().getBoolean("pulse.announcements.sound-effects", true)) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
            }
        }
    }

    public int executeOptimizationPulse(String issuer) {
        long start = System.currentTimeMillis();
        int purged = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (!plugin.getItemRarityShieldManager().isImmune(item)) {
                        plugin.getLagRecoveryManager().recordItem(item.getItemStack(), item.getLocation(), item.getThrower());
                        item.remove();
                        purged++;
                    }
                } else if (entity instanceof Projectile proj) {
                    if (proj.isOnGround()) {
                        proj.remove();
                        purged++;
                    }
                }
            }
        }

        long elapsed = System.currentTimeMillis() - start;

        Title finalTitle = Title.title(
                plugin.getLang().getRawComponent("warnings.title-done-main"),
                LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getLang().getRaw("warnings.title-done-sub").replace("{count}", String.valueOf(purged))),
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(500))
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(finalTitle);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
        }

        String msg = plugin.getLang().getPrefixed("commands.pulse-completed")
                .replace("{count}", String.valueOf(purged))
                .replace("{time}", String.valueOf(elapsed));
        Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));

        return purged;
    }
}
