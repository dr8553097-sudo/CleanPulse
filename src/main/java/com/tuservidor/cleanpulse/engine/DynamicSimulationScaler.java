package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class DynamicSimulationScaler {
    private final CleanPulse plugin;

    public DynamicSimulationScaler(CleanPulse plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("simulation-scaler.enabled", true)) return;
        int interval = plugin.getConfig().getInt("simulation-scaler.check-interval-seconds", 30) * 20;
        double scaleDown = plugin.getConfig().getDouble("simulation-scaler.scale-down-tps", 18.0);
        double scaleUp = plugin.getConfig().getDouble("simulation-scaler.scale-up-tps", 19.8);
        int minDistance = plugin.getConfig().getInt("simulation-scaler.min-distance", 4);
        int baseDistance = plugin.getConfig().getInt("simulation-scaler.base-distance", 10);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            double currentTps = Bukkit.getTPS()[0];
            for (World w : Bukkit.getWorlds()) {
                int cur = w.getSimulationDistance();
                if (currentTps < scaleDown && cur > minDistance) {
                    w.setSimulationDistance(Math.max(minDistance, cur - 1));
                } else if (currentTps >= scaleUp && cur < baseDistance) {
                    w.setSimulationDistance(Math.min(baseDistance, cur + 1));
                }
            }
        }, interval, interval);
    }
}
