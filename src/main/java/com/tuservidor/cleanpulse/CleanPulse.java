package com.tuservidor.cleanpulse;

import com.tuservidor.cleanpulse.commands.CleanPulseCommand;
import com.tuservidor.cleanpulse.commands.CleanPulseTabCompleter;
import com.tuservidor.cleanpulse.commands.TrashCommand;
import com.tuservidor.cleanpulse.engine.PulseManager;
import com.tuservidor.cleanpulse.engine.SmartFilter;
import com.tuservidor.cleanpulse.inspector.LagInspectorManager;
import com.tuservidor.cleanpulse.player.TrashMenu;
import com.tuservidor.cleanpulse.protection.DeathShieldManager;
import com.tuservidor.cleanpulse.protection.RedstoneSentinelListener;
import com.tuservidor.cleanpulse.util.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CleanPulse extends JavaPlugin {
    private static CleanPulse instance;
    private LangManager langManager;
    private SmartFilter smartFilter;
    private DeathShieldManager deathShieldManager;
    private PulseManager pulseManager;
    private LagInspectorManager lagInspectorManager;
    private RedstoneSentinelListener redstoneSentinel;
    private TrashMenu trashMenu;
    private FileConfiguration uiConfig;
    private File uiFile;

    public static CleanPulse getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        saveDefaultConfig();
        loadUiConfig();

        this.langManager = new LangManager(this);
        this.smartFilter = new SmartFilter(this);
        this.deathShieldManager = new DeathShieldManager(this);
        this.pulseManager = new PulseManager(this);
        this.lagInspectorManager = new LagInspectorManager(this);
        this.redstoneSentinel = new RedstoneSentinelListener(this);
        this.trashMenu = new TrashMenu(this);

        Bukkit.getPluginManager().registerEvents(deathShieldManager, this);
        Bukkit.getPluginManager().registerEvents(redstoneSentinel, this);
        Bukkit.getPluginManager().registerEvents(trashMenu, this);

        if (getCommand("cleanpulse") != null) {
            getCommand("cleanpulse").setExecutor(new CleanPulseCommand(this));
            getCommand("cleanpulse").setTabCompleter(new CleanPulseTabCompleter());
        }
        if (getCommand("trash") != null) {
            getCommand("trash").setExecutor(new TrashCommand(this));
        }

        Bukkit.getScheduler().runTaskTimer(this, () -> redstoneSentinel.resetRates(), 20L, 20L);

        long time = System.currentTimeMillis() - start;
        getLogger().info("----------------------------------------");
        getLogger().info("CleanPulse | Next-Gen Adaptive Optimizer");
        getLogger().info("Version: " + getDescription().getVersion() + " by Dafealru");
        getLogger().info("Paper API Native: 1.21.x / Java 21");
        getLogger().info("Ready in " + time + "ms");
        getLogger().info("----------------------------------------");
    }

    @Override
    public void onDisable() {
        if (pulseManager != null) {
            pulseManager.stopSchedule();
        }
        getLogger().info("CleanPulse disabled safely.");
    }

    public void reloadAll() {
        reloadConfig();
        loadUiConfig();
        if (langManager != null) langManager.reload();
        if (pulseManager != null) pulseManager.startSchedule();
    }

    public void loadUiConfig() {
        this.uiFile = new File(getDataFolder(), "ui.yml");
        if (!uiFile.exists()) {
            saveResource("ui.yml", false);
        }
        this.uiConfig = YamlConfiguration.loadConfiguration(uiFile);
    }

    public FileConfiguration getUiConfig() {
        return uiConfig;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public SmartFilter getSmartFilter() {
        return smartFilter;
    }

    public DeathShieldManager getDeathShieldManager() {
        return deathShieldManager;
    }

    public PulseManager getPulseManager() {
        return pulseManager;
    }

    public LagInspectorManager getLagInspectorManager() {
        return lagInspectorManager;
    }

    public TrashMenu getTrashMenu() {
        return trashMenu;
    }
}
