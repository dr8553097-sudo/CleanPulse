package com.tuservidor.cleanpulse.protection;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathShieldManager implements Listener {
    private final CleanPulse plugin;
    private final Map<UUID, Long> protectedItems = new ConcurrentHashMap<>();

    public DeathShieldManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("smart-sentinel.death-grace-shield.enabled", true)) {
            return;
        }

        Player player = event.getEntity();
        int immunitySec = plugin.getConfig().getInt("smart-sentinel.death-grace-shield.immunity-seconds", 300);
        long expiryTime = System.currentTimeMillis() + (immunitySec * 1000L);

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack != null && stack.getType() != org.bukkit.Material.AIR) {
                Item dropped = player.getWorld().dropItem(player.getLocation(), stack);
                protectedItems.put(dropped.getUniqueId(), expiryTime);
            }
        }
        event.getDrops().clear();

        if (plugin.getConfig().getBoolean("smart-sentinel.death-grace-shield.notify-player-on-death", true)) {
            String timeStr = (immunitySec / 60) + " minutos";
            plugin.getLangManager().send(player, "death-shield.player-protected", Map.of("time", timeStr));
        }
    }

    public boolean isProtected(Item item) {
        if (item == null) return false;
        Long expiry = protectedItems.get(item.getUniqueId());
        if (expiry == null) return false;

        if (System.currentTimeMillis() < expiry) {
            return true;
        } else {
            protectedItems.remove(item.getUniqueId());
            return false;
        }
    }

    public void cleanExpired() {
        long now = System.currentTimeMillis();
        protectedItems.entrySet().removeIf(entry -> now >= entry.getValue());
    }
}
