package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class HolographicStackManager implements Listener {
    private final CleanPulse plugin;

    public HolographicStackManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (!plugin.getConfig().getBoolean("item-stacker.enabled", true)) return;
        Item spawned = e.getEntity();
        ItemStack stack = spawned.getItemStack();
        double radius = plugin.getConfig().getDouble("item-stacker.merge-radius", 4.0);

        for (Entity nearby : spawned.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof Item target) || target.equals(spawned) || target.isDead()) continue;
            ItemStack targetStack = target.getItemStack();

            if (targetStack.isSimilar(stack)) {
                int total = stack.getAmount() + targetStack.getAmount();
                targetStack.setAmount(total);
                target.setItemStack(targetStack);

                if (plugin.getConfig().getBoolean("item-stacker.display-hologram", true)) {
                    String format = plugin.getConfig().getString("item-stacker.hologram-format", "&d✦ &f[x{amount}] &b{name}");
                    String name = format.replace("{amount}", String.valueOf(total))
                            .replace("{name}", formatItemName(targetStack.getType().name()));
                    target.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.translateAlternateColorCodes('&', name)));
                    target.setCustomNameVisible(true);
                }

                spawned.remove();
                e.setCancelled(true);
                break;
            }
        }
    }

    private String formatItemName(String enumName) {
        String[] parts = enumName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
