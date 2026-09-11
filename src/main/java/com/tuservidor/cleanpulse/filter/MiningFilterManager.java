package com.tuservidor.cleanpulse.filter;

import com.tuservidor.cleanpulse.CleanPulse;
import com.tuservidor.cleanpulse.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MiningFilterManager implements Listener {
    private final CleanPulse plugin;
    private final Map<UUID, Set<Material>> playerFilters = new ConcurrentHashMap<>();
    private final String guiTitle;

    public MiningFilterManager(CleanPulse plugin) {
        this.plugin = plugin;
        this.guiTitle = ColorUtil.color(plugin.getUiConfig().getString("filter-gui.title", "&9🧹 Auto-Filtro"));
    }

    public void open(Player player) {
        int rows = plugin.getUiConfig().getInt("filter-gui.rows", 4);
        Inventory inv = Bukkit.createInventory(null, rows * 9, guiTitle);

        Set<Material> active = playerFilters.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        List<String> matNames = plugin.getUiConfig().getStringList("filter-gui.materials");

        int slot = 0;
        for (String mName : matNames) {
            if (slot >= inv.getSize()) break;
            Material mat = Material.matchMaterial(mName);
            if (mat != null) {
                boolean isFiltered = active.contains(mat);
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColorUtil.color((isFiltered ? "&a✔ &l" : "&c✖ &l") + mat.name()));
                    meta.setLore(List.of(
                            ColorUtil.color(isFiltered ? "&aFiltro ACTIVO (No lo recogerás)" : "&7Filtro INACTIVO (Se recoge normal)"),
                            ColorUtil.color("&eHaz clic para alternar")
                    ));
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
                slot++;
            }
        }

        player.openInventory(inv);
        plugin.getLangManager().send(player, "filter.opened");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(guiTitle)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Material mat = clicked.getType();
        Set<Material> active = playerFilters.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (active.contains(mat)) {
            active.remove(mat);
            plugin.getLangManager().send(player, "filter.toggle-off", Map.of("material", mat.name()));
        } else {
            active.add(mat);
            plugin.getLangManager().send(player, "filter.toggle-on", Map.of("material", mat.name()));
        }

        open(player); // Refresh GUI
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Set<Material> active = playerFilters.get(player.getUniqueId());
        if (active != null && active.contains(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
            event.getItem().remove(); // Auto-voids the junk item
        }
    }
}
