package com.tuservidor.cleanpulse.protection;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RedstoneSentinelListener implements Listener {
    private final CleanPulse plugin;
    private final Map<Location, AtomicInteger> redstonePulseRates = new ConcurrentHashMap<>();
    private final Map<Location, Long> frozenClocks = new ConcurrentHashMap<>();

    public RedstoneSentinelListener(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRedstone(BlockRedstoneEvent event) {
        if (!plugin.getConfig().getBoolean("redstone-sentinel.enabled", true)) return;

        Block block = event.getBlock();
        Location loc = block.getLocation();

        Long frozenUntil = frozenClocks.get(loc);
        if (frozenUntil != null) {
            if (System.currentTimeMillis() < frozenUntil) {
                event.setNewCurrent(0);
                return;
            } else {
                frozenClocks.remove(loc);
            }
        }

        int maxRate = plugin.getConfig().getInt("redstone-sentinel.max-pulses-per-second", 12);
        AtomicInteger count = redstonePulseRates.computeIfAbsent(loc, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > maxRate) {
            event.setNewCurrent(0);
            int freezeDuration = plugin.getConfig().getInt("redstone-sentinel.freeze-duration-seconds", 60);
            frozenClocks.put(loc, System.currentTimeMillis() + (freezeDuration * 1000L));

            if (plugin.getConfig().getBoolean("redstone-sentinel.notify-staff", true)) {
                String msg = plugin.getLangManager().get("redstone.clock-frozen", Map.of(
                        "x", String.valueOf(loc.getBlockX()),
                        "y", String.valueOf(loc.getBlockY()),
                        "z", String.valueOf(loc.getBlockZ()),
                        "rate", String.valueOf(count.get())
                ));
                Bukkit.broadcast(msg, "cleanpulse.admin");
            }
        }
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (!plugin.getConfig().getBoolean("redstone-sentinel.enabled", true)) return;
        Vehicle v = event.getVehicle();
        int max = plugin.getConfig().getInt("redstone-sentinel.max-minecarts-per-block", 5);

        int count = 0;
        for (org.bukkit.entity.Entity e : v.getNearbyEntities(0.5, 0.5, 0.5)) {
            if (e instanceof Vehicle) count++;
        }

        if (count >= max) {
            v.remove();
        }
    }

    public void resetRates() {
        redstonePulseRates.clear();
    }
}
