package com.tuservidor.cleanpulse.protection;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathShieldManager implements Listener {
    private final CleanPulse plugin;
    private final Map<UUID, DeathRecord> deathRecords = new ConcurrentHashMap<>();

    public record DeathRecord(Location location, long expiryTime) {}

    public DeathShieldManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        if (!plugin.getConfig().getBoolean("death-shield.enabled", true)) return;
        Player p = e.getEntity();
        int durSeconds = plugin.getConfig().getInt("death-shield.duration-seconds", 300);
        long expiry = System.currentTimeMillis() + (durSeconds * 1000L);

        deathRecords.put(p.getUniqueId(), new DeathRecord(p.getLocation(), expiry));

        for (ItemStack drop : e.getDrops()) {
            if (drop == null || drop.getType().isAir()) continue;
            Item item = p.getWorld().dropItemNaturally(p.getLocation(), drop);
            item.setMetadata("cleanpulse_death_shield", new FixedMetadataValue(plugin, p.getUniqueId().toString()));
        }
        e.getDrops().clear();
    }

    public void sendDeathShieldStatus(Player player) {
        DeathRecord record = deathRecords.get(player.getUniqueId());
        if (record == null || System.currentTimeMillis() > record.expiryTime()) {
            player.sendMessage(plugin.getLang().getComponent("commands.death-none"));
            return;
        }

        long remainingSec = (record.expiryTime() - System.currentTimeMillis()) / 1000;
        Location loc = record.location();
        String msg = plugin.getLang().getPrefixed("commands.death-shield-info")
                .replace("{x}", String.valueOf(loc.getBlockX()))
                .replace("{y}", String.valueOf(loc.getBlockY()))
                .replace("{z}", String.valueOf(loc.getBlockZ()))
                .replace("{time}", String.valueOf(remainingSec));

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
    }
}
