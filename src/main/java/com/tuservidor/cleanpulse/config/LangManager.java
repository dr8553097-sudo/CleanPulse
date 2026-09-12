package com.tuservidor.cleanpulse.config;

import com.tuservidor.cleanpulse.CleanPulse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangManager {
    private final CleanPulse plugin;
    private YamlConfiguration langConfig;
    private String activeLang;

    public LangManager(CleanPulse plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        this.activeLang = plugin.getConfig().getString("language", "en");
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        for (String lang : new String[]{"en", "es", "fr", "pt"}) {
            File f = new File(langDir, lang + ".yml");
            if (!f.exists()) {
                plugin.saveResource("lang/" + lang + ".yml", false);
            }
        }

        File activeFile = new File(langDir, activeLang + ".yml");
        if (!activeFile.exists()) {
            activeFile = new File(langDir, "en.yml");
            this.activeLang = "en";
        }

        this.langConfig = YamlConfiguration.loadConfiguration(activeFile);
        InputStream defStream = plugin.getResource("lang/" + activeLang + ".yml");
        if (defStream != null) {
            this.langConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8)));
        }
    }

    public String getRaw(String path) {
        return langConfig.getString(path, "&cMissing message: " + path);
    }

    public String getPrefixed(String path) {
        String prefix = langConfig.getString("prefix", "&d&lCleanPulse &8» &f");
        return ChatColor.translateAlternateColorCodes('&', prefix + getRaw(path));
    }

    public Component getComponent(String path) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(getPrefixed(path));
    }

    public Component getRawComponent(String path) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.translateAlternateColorCodes('&', getRaw(path)));
    }

    public void setLanguage(String lang) {
        this.activeLang = lang;
        plugin.getConfig().set("language", lang);
        plugin.saveConfig();
        load();
    }

    public String getActiveLang() {
        return activeLang;
    }
}
