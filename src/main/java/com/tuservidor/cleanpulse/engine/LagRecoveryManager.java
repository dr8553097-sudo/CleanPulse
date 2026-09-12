package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LagRecoveryManager {
    private final CleanPulse plugin;
    private final List<PurgedRecord> recoveryBuffer = Collections.synchronizedList(new ArrayList<>());
    private final Map<UUID, List<PurgedRecord>> playerBuffer = new ConcurrentHashMap<>();

    public record PurgedRecord(ItemStack itemStack, Location location, UUID ownerUuid, long timestamp) {}

    public LagRecoveryManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    public void recordItem(ItemStack stack, Location loc, UUID owner) {
        if (!plugin.getConfig().getBoolean("lag-recovery.enabled", true)) return;
        PurgedRecord record = new PurgedRecord(stack.clone(), loc.clone(), owner, System.currentTimeMillis());
        recoveryBuffer.add(record);
        if (owner != null) {
            playerBuffer.computeIfAbsent(owner, k -> new ArrayList<>()).add(record);
        }

        int max = plugin.getConfig().getInt("lag-recovery.max-saved-items", 5000);
        if (recoveryBuffer.size() > max) {
            recoveryBuffer.remove(0);
        }
    }

    public int restoreAll() {
        int count = 0;
        synchronized (recoveryBuffer) {
            for (PurgedRecord rec : recoveryBuffer) {
                if (rec.location().getWorld() != null) {
                    rec.location().getWorld().dropItemNaturally(rec.location(), rec.itemStack());
                    count++;
                }
            }
            recoveryBuffer.clear();
            playerBuffer.clear();
        }
        return count;
    }

    public int restorePlayer(UUID uuid) {
        List<PurgedRecord> list = playerBuffer.remove(uuid);
        if (list == null || list.isEmpty()) return 0;
        int count = 0;
        for (PurgedRecord rec : list) {
            if (rec.location().getWorld() != null) {
                rec.location().getWorld().dropItemNaturally(rec.location(), rec.itemStack());
                count++;
            }
            recoveryBuffer.remove(rec);
        }
        return count;
    }

    public void clearExpired() {
        long limit = System.currentTimeMillis() - (plugin.getConfig().getInt("lag-recovery.buffer-duration-minutes", 10) * 60000L);
        recoveryBuffer.removeIf(r -> r.timestamp() < limit);
    }
}
