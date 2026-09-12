package com.tuservidor.cleanpulse.filter;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MiningFilterManager implements Listener {
    private final CleanPulse plugin;
    private final Set<UUID> activeFilters = ConcurrentHashMap.newKeySet();
    private final Set<Material> filterMaterials = new HashSet<>();

    public MiningFilterManager(CleanPulse plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        filterMaterials.clear();
        List<String> list = plugin.getConfig().getStringList("mining-filter.default-materials");
        for (String s : list) {
            try {
                filterMaterials.add(Material.valueOf(s.toUpperCase()));
            } catch (Exception ignored) {}
        }
    }

    public boolean toggleFilter(Player player) {
        if (activeFilters.contains(player.getUniqueId())) {
            activeFilters.remove(player.getUniqueId());
            return false;
        } else {
            activeFilters.add(player.getUniqueId());
            return true;
        }
    }

    public boolean isFilterActive(Player player) {
        return activeFilters.contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!plugin.getConfig().getBoolean("mining-filter.enabled", true)) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (!isFilterActive(p)) return;

        ItemStack stack = e.getItem().getItemStack();
        if (filterMaterials.contains(stack.getType())) {
            e.getItem().remove();
            e.setCancelled(true);
        }
    }
}
