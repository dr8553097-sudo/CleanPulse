package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SmartFilter {
    private final CleanPulse plugin;

    public SmartFilter(CleanPulse plugin) {
        this.plugin = plugin;
    }

    public boolean isItemProtected(Item item) {
        if (item == null || !item.isValid() || item.isDead()) return true;

        if (plugin.getDeathShieldManager() != null && plugin.getDeathShieldManager().isProtected(item)) {
            return true;
        }

        ItemStack stack = item.getItemStack();
        if (stack.getType() == Material.AIR) return true;

        Material type = stack.getType();
        String name = type.name();

        if (plugin.getConfig().getBoolean("smart-sentinel.protected-items.netherite-gear", true) 
                && name.startsWith("NETHERITE_")) {
            return true;
        }
        if (plugin.getConfig().getBoolean("smart-sentinel.protected-items.diamond-gear", true) 
                && name.startsWith("DIAMOND_")) {
            return true;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (plugin.getConfig().getBoolean("smart-sentinel.protected-items.named-items", true) 
                    && meta.hasDisplayName()) {
                return true;
            }
            if (plugin.getConfig().getBoolean("smart-sentinel.protected-items.custom-lore", true) 
                    && meta.hasLore()) {
                return true;
            }
            if (plugin.getConfig().getBoolean("smart-sentinel.protected-items.high-enchantments", true) 
                    && meta.hasEnchants()) {
                return true;
            }
        }

        return false;
    }

    public boolean isEntityProtected(Entity entity) {
        if (entity == null || !entity.isValid() || entity.isDead()) return true;
        if (entity instanceof Player) return true;

        if (plugin.getConfig().getBoolean("smart-sentinel.protected-entities.named-mobs", true) 
                && entity.getCustomName() != null) {
            return true;
        }

        if (plugin.getConfig().getBoolean("smart-sentinel.protected-entities.tamed-animals", true) 
                && entity instanceof Tameable tameable && tameable.isTamed()) {
            return true;
        }

        if (plugin.getConfig().getBoolean("smart-sentinel.protected-entities.villagers-with-trades", true) 
                && entity instanceof Villager villager && villager.getVillagerExperience() > 0) {
            return true;
        }

        if (plugin.getConfig().getBoolean("smart-sentinel.protected-entities.chest-boats-and-minecarts", true)) {
            if (entity instanceof ChestBoat || entity instanceof org.bukkit.entity.minecart.StorageMinecart 
                    || entity instanceof org.bukkit.entity.minecart.HopperMinecart) {
                return true;
            }
        }

        if (plugin.getConfig().getBoolean("smart-sentinel.protected-entities.armour-stands", true) 
                && entity instanceof ArmorStand) {
            return true;
        }

        return false;
    }
}
