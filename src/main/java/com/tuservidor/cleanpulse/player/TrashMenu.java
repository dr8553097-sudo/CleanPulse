package com.tuservidor.cleanpulse.player;

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

public class TrashMenu implements Listener {
    private final CleanPulse plugin;
    private final String title;

    public TrashMenu(CleanPulse plugin) {
        this.plugin = plugin;
        this.title = ColorUtil.color(plugin.getUiConfig().getString("trash-gui.title", "&9🗑️ Papelera de Descarte"));
    }

    public void open(Player player) {
        int rows = plugin.getUiConfig().getInt("trash-gui.rows", 4);
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        int confirmSlot = plugin.getUiConfig().getInt("trash-gui.confirm-slot", 35);
        ItemStack confirm = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = confirm.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(plugin.getUiConfig().getString("trash-gui.confirm-item.name", "&c&lVACIAR PAPELERA")));
            List<String> lore = plugin.getUiConfig().getStringList("trash-gui.confirm-item.lore");
            meta.setLore(lore.stream().map(ColorUtil::color).toList());
            confirm.setItemMeta(meta);
        }
        inv.setItem(confirmSlot, confirm);

        player.openInventory(inv);
        plugin.getLangManager().send(player, "trash.opened");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;

        int confirmSlot = plugin.getUiConfig().getInt("trash-gui.confirm-slot", 35);
        if (event.getRawSlot() == confirmSlot) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            Inventory inv = event.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (i != confirmSlot) {
                    inv.setItem(i, null);
                }
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0f, 1.0f);
            plugin.getLangManager().send(player, "trash.cleared");
        }
    }
}
