package com.tuservidor.cleanpulse.gui;

import com.tuservidor.cleanpulse.CleanPulse;
import com.tuservidor.cleanpulse.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PerformanceGui implements Listener {
    private final CleanPulse plugin;
    private final String title;

    public PerformanceGui(CleanPulse plugin) {
        this.plugin = plugin;
        this.title = ColorUtil.color(plugin.getUiConfig().getString("performance-gui.title", "&9⚡ CleanPulse Diagnostics"));
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, title);

        double[] tps = Bukkit.getTPS();
        double currentTps = (tps != null && tps.length > 0) ? Math.min(20.0, tps[0]) : 20.0;
        int health = (int) ((currentTps / 20.0) * 100);

        Runtime r = Runtime.getRuntime();
        long usedRam = (r.totalMemory() - r.freeMemory()) / 1048576L;
        long maxRam = r.maxMemory() / 1048576L;
        int totalEntities = Bukkit.getWorlds().stream().mapToInt(w -> w.getEntities().size()).sum();

        // Slot 11: TPS Gauge
        ItemStack tpsItem = new ItemStack(currentTps > 18.0 ? Material.LIME_DYE : currentTps > 15.0 ? Material.YELLOW_DYE : Material.RED_DYE);
        ItemMeta tpsMeta = tpsItem.getItemMeta();
        if (tpsMeta != null) {
            tpsMeta.setDisplayName(ColorUtil.color("&#00FF88&lTPS: &f" + String.format("%.2f", currentTps)));
            tpsMeta.setLore(List.of(
                    ColorUtil.color("&7Salud del servidor: &#e0aaff" + health + "%"),
                    ColorUtil.color("&7Estado: " + (currentTps > 18.0 ? "&aÓPTIMO" : "&cBAJO CARGA"))
            ));
            tpsItem.setItemMeta(tpsMeta);
        }
        inv.setItem(11, tpsItem);

        // Slot 13: RAM Usage
        ItemStack ramItem = new ItemStack(Material.HOPPER);
        ItemMeta ramMeta = ramItem.getItemMeta();
        if (ramMeta != null) {
            ramMeta.setDisplayName(ColorUtil.color("&#c77dff&lMEMORIA RAM"));
            ramMeta.setLore(List.of(
                    ColorUtil.color("&7En uso: &#e0aaff" + usedRam + " MB"),
                    ColorUtil.color("&7Asignada: &#c77dff" + maxRam + " MB")
            ));
            ramItem.setItemMeta(ramMeta);
        }
        inv.setItem(13, ramItem);

        // Slot 15: Entities Diagnostic
        ItemStack entItem = new ItemStack(Material.COMPASS);
        ItemMeta entMeta = entItem.getItemMeta();
        if (entMeta != null) {
            entMeta.setDisplayName(ColorUtil.color("&#00F0FF&lENTIDADES"));
            entMeta.setLore(List.of(
                    ColorUtil.color("&7Entidades globales: &#e0aaff" + totalEntities),
                    ColorUtil.color("&7Mundos cargados: &#e0aaff" + Bukkit.getWorlds().size())
            ));
            entItem.setItemMeta(entMeta);
        }
        inv.setItem(15, entItem);

        // Slot 22: Force Optimization Pulse Button
        ItemStack pulseBtn = new ItemStack(Material.NETHER_STAR);
        ItemMeta pMeta = pulseBtn.getItemMeta();
        if (pMeta != null) {
            pMeta.setDisplayName(ColorUtil.color("&#9d4edd&l⚡ EJECUTAR PULSO MANUAL"));
            pMeta.setLore(List.of(
                    ColorUtil.color("&7Haz clic para forzar un pulso"),
                    ColorUtil.color("&7de optimización inmediato.")
            ));
            pulseBtn.setItemMeta(pMeta);
        }
        inv.setItem(22, pulseBtn);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        event.setCancelled(true);

        if (event.getRawSlot() == 22 && event.getWhoClicked() instanceof Player player) {
            player.closeInventory();
            plugin.getPulseManager().executePulse(player, false);
        }
    }
}
