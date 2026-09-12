package com.tuservidor.cleanpulse.gui;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PerformanceGui implements Listener {
    private final CleanPulse plugin;

    public PerformanceGui(CleanPulse plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, LegacyComponentSerializer.legacyAmpersand().deserialize("&8[ &d&lCleanPulse &fDashboard &8]"));

        // Background filler
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, "&7 ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // TPS Gauge (Slot 11)
        double tps = Bukkit.getTPS()[0];
        Material tpsMat = tps >= 19.5 ? Material.LIME_CONCRETE : (tps >= 17.0 ? Material.YELLOW_CONCRETE : Material.RED_CONCRETE);
        inv.setItem(11, createItem(tpsMat, "&a&lServer TPS: &f" + String.format("%.2f", tps),
                "&7• Health: &d" + (tps >= 19.5 ? "99%" : "85%"),
                "&7• Status: " + (tps >= 18.0 ? "&aStable" : "&cUnder Load")));

        // Memory RAM Gauge (Slot 13)
        Runtime rt = Runtime.getRuntime();
        long maxMem = rt.maxMemory() / 1048576L;
        long usedMem = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        inv.setItem(13, createItem(Material.END_CRYSTAL, "&d&lMemory RAM Usage",
                "&7• Used: &e" + usedMem + " MB &7/ &e" + maxMem + " MB",
                "&7• Free: &a" + (maxMem - usedMem) + " MB"));

        // Global Entities (Slot 15)
        int entities = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) entities += w.getEntities().size();
        inv.setItem(15, createItem(Material.AMETHYST_CLUSTER, "&b&lEntity Density",
                "&7• Global Entities: &e" + entities,
                "&7• Loaded Worlds: &e" + Bukkit.getWorlds().size()));

        // Quick Pulse Button (Slot 22)
        inv.setItem(22, createItem(Material.NETHER_STAR, "&d&l⚡ Execute Instant Pulse",
                "&7Click to immediately purge clutter",
                "&7and optimize nearby entities."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            List<Component> loreList = new ArrayList<>();
            for (String l : lore) {
                loreList.add(LegacyComponentSerializer.legacyAmpersand().deserialize(l));
            }
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[ &d&lCleanPulse &fDashboard &8]"))) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (e.getRawSlot() == 22) {
            p.closeInventory();
            plugin.getPulseManager().executeOptimizationPulse(p.getName());
        }
    }
}
