package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class PulseManager {
    private final CleanPulse plugin;
    private BukkitTask mainAutoTask = null;
    private int remainingSeconds = 300;
    private int configuredInterval = 300;

    public PulseManager(CleanPulse plugin) {
        this.plugin = plugin;
        startAutoScheduler();
    }

    public void startAutoScheduler() {
        if (mainAutoTask != null) {
            mainAutoTask.cancel();
            mainAutoTask = null;
        }

        if (!plugin.getConfig().getBoolean("pulse.auto-pulse.enabled", true)) return;

        this.configuredInterval = plugin.getConfig().getInt("pulse.auto-pulse.interval-seconds", 300);
        this.remainingSeconds = this.configuredInterval;

        mainAutoTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            remainingSeconds--;

            // Warnings at 60s and 30s with Chat, Action Bar, and Bell chime Sound
            if (remainingSeconds == 60 || remainingSeconds == 30) {
                broadcastWarning(remainingSeconds);
            }

            // 10s down to 1s: Full Real-Time Chat, Action Bar & Title Countdown with Rising Sound Note
            if (remainingSeconds <= 10 && remainingSeconds >= 1) {
                broadcastCountdown(remainingSeconds);
            }

            // 0s: Execute Pulse and Reset Timer
            if (remainingSeconds <= 0) {
                executeOptimizationPulse("Auto-Timer");
                remainingSeconds = configuredInterval;
            }
        }, 20L, 20L);
    }

    public void broadcastWarning(int seconds) {
        String chatMsg = plugin.getLang().getPrefixed("warnings.broadcast")
                .replace("{time}", String.valueOf(seconds));
        Component chatComp = LegacyComponentSerializer.legacyAmpersand().deserialize(chatMsg);

        String actionMsg = plugin.getLang().getRaw("warnings.actionbar")
                .replace("{time}", String.valueOf(seconds));
        Component actionComp = LegacyComponentSerializer.legacyAmpersand().deserialize(actionMsg);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfig().getBoolean("pulse.announcements.chat-broadcast", true)) {
                player.sendMessage(chatComp);
            }
            if (plugin.getConfig().getBoolean("pulse.announcements.actionbar-warnings", true)) {
                player.sendActionBar(actionComp);
            }
            if (plugin.getConfig().getBoolean("pulse.announcements.sound-effects", true)) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            }
        }
    }

    public void initiatePulseSequence(String issuer) {
        this.remainingSeconds = 10;
        String startMsg = plugin.getLang().getPrefixed("commands.pulse-started").replace("{issuer}", issuer);
        Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(startMsg));
        broadcastCountdown(10);
    }

    public void broadcastCountdown(int seconds) {
        float pitch = 0.8f + (10 - seconds) * 0.12f;

        // 1. Center Title
        Title title = Title.title(
                plugin.getLang().getRawComponent("warnings.title-main"),
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        plugin.getLang().getRaw("warnings.title-sub").replace("{time}", String.valueOf(seconds))
                ),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(600), Duration.ofMillis(300))
        );

        // 2. Chat Countdown
        String chatMsg = plugin.getLang().getPrefixed("warnings.chat-countdown")
                .replace("{time}", String.valueOf(seconds));
        Component chatComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(chatMsg);

        // 3. Action Bar Countdown
        String actionMsg = plugin.getLang().getRaw("warnings.actionbar")
                .replace("{time}", String.valueOf(seconds));
        Component actionComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(actionMsg);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getConfig().getBoolean("pulse.announcements.title-warnings", true)) {
                player.showTitle(title);
            }
            if (plugin.getConfig().getBoolean("pulse.announcements.chat-broadcast", true)) {
                player.sendMessage(chatComponent);
            }
            if (plugin.getConfig().getBoolean("pulse.announcements.actionbar-warnings", true)) {
                player.sendActionBar(actionComponent);
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
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        plugin.getLang().getRaw("warnings.title-done-sub").replace("{count}", String.valueOf(purged))
                ),
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

    public String getFormattedRemainingTime() {
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        if (m > 0) {
            return m + "m " + s + "s";
        }
        return s + "s";
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
