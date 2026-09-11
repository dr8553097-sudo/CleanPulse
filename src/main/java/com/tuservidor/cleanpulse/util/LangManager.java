package com.tuservidor.cleanpulse.util;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LangManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private String defaultLang = "en";

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        languages.clear();
        this.defaultLang = plugin.getConfig().getString("language.default", "en").toLowerCase(Locale.ROOT);
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        String[] langs = {"es", "en", "fr", "pt"};
        for (String lang : langs) {
            File file = new File(langFolder, lang + ".yml");
            if (!file.exists()) {
                try (InputStream in = plugin.getResource("lang/" + lang + ".yml")) {
                    if (in != null) {
                        Files.copy(in, file.toPath());
                    }
                } catch (Exception ignored) {}
            }
            if (file.exists()) {
                languages.put(lang, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    public String get(String key) {
        return get(defaultLang, key, null);
    }

    public String get(String key, Map<String, String> placeholders) {
        return get(defaultLang, key, placeholders);
    }

    public String get(String lang, String key, Map<String, String> placeholders) {
        String selected = (lang != null && languages.containsKey(lang.toLowerCase(Locale.ROOT))) 
                ? lang.toLowerCase(Locale.ROOT) : defaultLang;
        
        FileConfiguration config = languages.get(selected);
        String msg = (config != null) ? config.getString(key) : null;
        if (msg == null && !selected.equals("en")) {
            FileConfiguration enConfig = languages.get("en");
            if (enConfig != null) msg = enConfig.getString(key);
        }
        if (msg == null) msg = key;

        String prefix = plugin.getConfig().getString("prefix", "&8[&9CleanPulse&8] &7");
        msg = msg.replace("{prefix}", prefix);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return ColorUtil.color(msg);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, null);
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String msg = get(key, placeholders);
        String prefix = ColorUtil.color(plugin.getConfig().getString("prefix", "&8[&9CleanPulse&8] &7"));
        sender.sendMessage(prefix + msg);
    }
}

