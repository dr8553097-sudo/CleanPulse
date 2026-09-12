package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class BlameManager {
    private final CleanPulse plugin;

    public BlameManager(CleanPulse plugin) {
        this.plugin = plugin;
    }

    public static class ChunkStat {
        public final Chunk chunk;
        public final int count;
        public final String type;

        public ChunkStat(Chunk chunk, int count, String type) {
            this.chunk = chunk;
            this.count = count;
            this.type = type;
        }
    }

    public void executeBlame(CommandSender sender) {
        sender.sendMessage(plugin.getLang().getRawComponent("commands.blame-header"));

        List<ChunkStat> entityChunks = new ArrayList<>();
        List<ChunkStat> hopperChunks = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Entity[] entities = chunk.getEntities();
                if (entities.length > 15) {
                    entityChunks.add(new ChunkStat(chunk, entities.length, "Entities"));
                }

                int hoppers = 0;
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Hopper) {
                        hoppers++;
                    }
                }
                if (hoppers > 5) {
                    hopperChunks.add(new ChunkStat(chunk, hoppers, "Hoppers"));
                }
            }
        }

        entityChunks.sort((a, b) -> Integer.compare(b.count, a.count));
        hopperChunks.sort((a, b) -> Integer.compare(b.count, a.count));

        sender.sendMessage(plugin.getLang().getRawComponent("commands.blame-entity-title"));
        int limit = Math.min(3, entityChunks.size());
        if (limit == 0) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a  ✔ No entity hotspots detected. All clear!"));
        } else {
            for (int i = 0; i < limit; i++) {
                sendClickableChunkStat(sender, entityChunks.get(i));
            }
        }

        sender.sendMessage(plugin.getLang().getRawComponent("commands.blame-hopper-title"));
        int hLimit = Math.min(3, hopperChunks.size());
        if (hLimit == 0) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a  ✔ No hopper overloads detected. All clear!"));
        } else {
            for (int i = 0; i < hLimit; i++) {
                sendClickableChunkStat(sender, hopperChunks.get(i));
            }
        }
    }

    private void sendClickableChunkStat(CommandSender sender, ChunkStat stat) {
        int x = (stat.chunk.getX() << 4) + 8;
        int z = (stat.chunk.getZ() << 4) + 8;
        int y = stat.chunk.getWorld().getHighestBlockYAt(x, z) + 1;
        String worldName = stat.chunk.getWorld().getName();

        String rawEntry = plugin.getLang().getRaw("commands.blame-entry")
                .replace("{x}", String.valueOf(stat.chunk.getX()))
                .replace("{z}", String.valueOf(stat.chunk.getZ()))
                .replace("{world}", worldName)
                .replace("{count}", String.valueOf(stat.count))
                .replace("{type}", stat.type);

        String rawBtn = plugin.getLang().getRaw("commands.blame-tp-button");
        String rawHover = plugin.getLang().getRaw("commands.blame-tp-hover")
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));

        Component entryComp = LegacyComponentSerializer.legacyAmpersand().deserialize(rawEntry);
        Component btnComp = LegacyComponentSerializer.legacyAmpersand().deserialize(rawBtn)
                .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(rawHover)))
                .clickEvent(ClickEvent.runCommand("/minecraft:tp " + x + " " + y + " " + z));

        sender.sendMessage(entryComp.append(btnComp));
    }
}
