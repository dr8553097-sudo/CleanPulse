package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerDiagnosticsManager {
    private final CleanPulse plugin;

    public PlayerDiagnosticsManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    public void checkZone(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        int mobs = 0;
        int items = 0;
        for (Entity e : chunk.getEntities()) {
            if (e instanceof Item) items++;
            else if (e instanceof LivingEntity && !(e instanceof Player)) mobs++;
        }

        int tiles = 0;
        for (BlockState state : chunk.getTileEntities()) {
            if (state instanceof Container) tiles++;
        }

        String grade;
        if (mobs < 15 && items < 20 && tiles < 15) {
            grade = "&a&l🟢 A+ (Optimal Performance)";
        } else if (mobs < 40 && items < 60 && tiles < 40) {
            grade = "&e&l🟡 B (Moderate Load)";
        } else {
            grade = "&c&l🔴 C (Heavy Entity Density)";
        }

        player.sendMessage(plugin.getLang().getRawComponent("commands.myfarm-header"));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getLang().getRaw("commands.myfarm-grade").replace("{grade}", grade)));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getLang().getRaw("commands.myfarm-entities").replace("{mobs}", String.valueOf(mobs)).replace("{items}", String.valueOf(items))));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getLang().getRaw("commands.myfarm-tiles").replace("{tiles}", String.valueOf(tiles))));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getLang().getRaw("commands.myfarm-redstone").replace("{redstone}", "&aNormal")));
    }
}
