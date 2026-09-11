package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PulseManager {
    private final CleanPulse plugin;
    private BukkitTask pulseTask;
    private BukkitTask emergencyMonitorTask;
    private int countdownSeconds = 600;
    private long lastEmergencyPulse = 0;

    public PulseManager(CleanPulse plugin) {
        this.plugin = plugin;
        startSchedule();
    }

    public void startSchedule() {
        stopSchedule();
        this.countdownSeconds = plugin.getConfig().getInt("pulse-engine.interval-seconds", 600);

        pulseTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds <= 0) {
                    executePulse(null, false);
                    countdownSeconds = plugin.getConfig().getInt("pulse-engine.interval-seconds", 600);
                    return;
                }

                checkAnnouncements(countdownSeconds);
                countdownSeconds--;
            }
        }.runTaskTimer(plugin, 20L, 20L);

        if (plugin.getConfig().getBoolean("pulse-engine.emergency-mode.enabled", true)) {
            double threshold = plugin.getConfig().getDouble("pulse-engine.emergency-mode.tps-threshold", 17.5);
            int period = plugin.getConfig().getInt("pulse-engine.emergency-mode.check-period-seconds", 15);
            int cooldown = plugin.getConfig().getInt("pulse-engine.emergency-mode.cooldown-seconds", 120);

            emergencyMonitorTask = new BukkitRunnable() {
                @Override
                public void run() {
                    double[] tps = Bukkit.getTPS();
                    if (tps != null && tps.length > 0 && tps[0] < threshold) {
                        long now = System.currentTimeMillis();
                        if (now - lastEmergencyPulse > (cooldown * 1000L)) {
                            lastEmergencyPulse = now;
                            String tpsFormatted = String.format("%.1f", tps[0]);
                            Bukkit.broadcastMessage(plugin.getLangManager().get("pulse.emergency-triggered", Map.of("tps", tpsFormatted)));
                            executePulse(null, true);
                        }
                    }
                }
            }.runTaskTimer(plugin, period * 20L, period * 20L);
        }
    }

    public void stopSchedule() {
        if (pulseTask != null) {
            pulseTask.cancel();
            pulseTask = null;
        }
        if (emergencyMonitorTask != null) {
            emergencyMonitorTask.cancel();
            emergencyMonitorTask = null;
        }
    }

    private void checkAnnouncements(int seconds) {
        List<Integer> warnings = plugin.getConfig().getIntegerList("pulse-engine.announcements.warning-seconds");
        if (warnings.isEmpty()) {
            warnings = List.of(60, 30, 10, 5, 4, 3, 2, 1);
        }

        if (warnings.contains(seconds)) {
            List<String> channels = plugin.getConfig().getStringList("pulse-engine.announcements.channels");
            String chatMsg = plugin.getLangManager().get("pulse.countdown-chat", Map.of("seconds", String.valueOf(seconds)));
            String actionBarMsg = plugin.getLangManager().get("pulse.countdown-actionbar", Map.of("seconds", String.valueOf(seconds)));

            boolean enableSound = plugin.getConfig().getBoolean("pulse-engine.announcements.sound.enabled", true);

            for (Player player : Bukkit.getOnlinePlayers()) {
                // Chat
                if (channels.contains("CHAT") && (seconds == 60 || seconds == 30)) {
                    player.sendMessage(chatMsg);
                }

                // Action Bar
                if (channels.contains("ACTION_BAR")) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMsg));
                }

                // On-Screen Titles
                if (channels.contains("TITLE")) {
                    if (seconds == 10 || seconds == 5) {
                        String title = plugin.getLangManager().get("pulse.title-warning");
                        String subtitle = plugin.getLangManager().get("pulse.subtitle-warning", Map.of("seconds", String.valueOf(seconds)));
                        player.sendTitle(title, subtitle, 5, 20, 5);
                        if (enableSound) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
                    } else if (seconds <= 4) {
                        String title = plugin.getLangManager().get("pulse.title-countdown", Map.of("seconds", String.valueOf(seconds)));
                        String subtitle = plugin.getLangManager().get("pulse.subtitle-countdown");
                        player.sendTitle(title, subtitle, 0, 18, 2);
                        
                        // Rising Pitch Audio Curve: 1.2 -> 1.5 -> 1.8 -> 2.0
                        float pitch = 1.0f + (5 - seconds) * 0.25f;
                        if (enableSound) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
                    }
                }
            }
        }
    }

    public void executePulse(CommandSender initiator, boolean isEmergency) {
        long startTime = System.currentTimeMillis();
        AtomicInteger itemsCleared = new AtomicInteger(0);
        AtomicInteger entitiesCleared = new AtomicInteger(0);

        SmartFilter filter = plugin.getSmartFilter();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (!filter.isItemProtected(item)) {
                        item.remove();
                        itemsCleared.incrementAndGet();
                    }
                } else if (entity instanceof Projectile proj) {
                    if (proj.isOnGround() || !proj.isValid()) {
                        proj.remove();
                        entitiesCleared.incrementAndGet();
                    }
                } else if (entity instanceof Monster monster) {
                    if (!filter.isEntityProtected(monster)) {
                        if (monster.getTicksLived() > 2400) {
                            monster.remove();
                            entitiesCleared.incrementAndGet();
                        }
                    }
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        String broadcast = plugin.getLangManager().get("pulse.cleared-broadcast", Map.of(
                "items", String.valueOf(itemsCleared.get()),
                "entities", String.valueOf(entitiesCleared.get()),
                "ms", String.valueOf(duration)
        ));

        String titleDone = plugin.getLangManager().get("pulse.title-finished");
        String subDone = plugin.getLangManager().get("pulse.subtitle-finished", Map.of(
                "items", String.valueOf(itemsCleared.get()),
                "entities", String.valueOf(entitiesCleared.get())
        ));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(broadcast));
            p.sendTitle(titleDone, subDone, 5, 35, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.8f);
        }

        if (plugin.getDeathShieldManager() != null) {
            plugin.getDeathShieldManager().cleanExpired();
        }
    }
}
