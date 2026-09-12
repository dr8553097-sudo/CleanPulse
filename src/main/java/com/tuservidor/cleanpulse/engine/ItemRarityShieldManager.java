package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemRarityShieldManager {
    private final CleanPulse plugin;
    private final Set<Material> immuneMaterials = new HashSet<>();
    private final Set<Material> junkMaterials = new HashSet<>();
    private boolean enabled;

    public ItemRarityShieldManager(CleanPulse plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        this.enabled = plugin.getConfig().getBoolean("rarity-shield.enabled", true);
        this.immuneMaterials.clear();
        this.junkMaterials.clear();

        List<String> immunes = plugin.getConfig().getStringList("rarity-shield.immune-materials");
        for (String s : immunes) {
            try {
                immuneMaterials.add(Material.valueOf(s.toUpperCase()));
            } catch (Exception ignored) {}
        }

        List<String> junks = plugin.getConfig().getStringList("rarity-shield.junk-materials");
        for (String s : junks) {
            try {
                junkMaterials.add(Material.valueOf(s.toUpperCase()));
            } catch (Exception ignored) {}
        }
    }

    public boolean isImmune(Item item) {
        if (!enabled) return false;
        if (item.hasMetadata("cleanpulse_death_shield")) return true;
        ItemStack stack = item.getItemStack();
        if (stack.hasItemMeta()) {
            if (stack.getItemMeta().hasCustomModelData() || stack.getItemMeta().hasDisplayName() || stack.getItemMeta().hasEnchants()) {
                return true;
            }
        }
        return immuneMaterials.contains(stack.getType());
    }

    public boolean isJunk(Item item) {
        return junkMaterials.contains(item.getItemStack().getType());
    }
}
