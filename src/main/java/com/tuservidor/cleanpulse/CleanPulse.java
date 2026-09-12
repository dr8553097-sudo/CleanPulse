package com.tuservidor.cleanpulse;

import com.tuservidor.cleanpulse.commands.CleanPulseCommand;
import com.tuservidor.cleanpulse.commands.CleanPulseTabCompleter;
import com.tuservidor.cleanpulse.commands.TrashCommand;
import com.tuservidor.cleanpulse.config.LangManager;
import com.tuservidor.cleanpulse.engine.*;
import com.tuservidor.cleanpulse.filter.MiningFilterManager;
import com.tuservidor.cleanpulse.protection.DeathShieldManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CleanPulse extends JavaPlugin {
    private static CleanPulse instance;
    private LangManager langManager;
    private PulseManager pulseManager;
    private ItemRarityShieldManager itemRarityShieldManager;
    private LagRecoveryManager lagRecoveryManager;
    private BlameManager blameManager;
    private MobHibernationManager mobHibernationManager;
    private HolographicStackManager holographicStackManager;
    private RedstoneSentinelManager redstoneSentinelManager;
    private DynamicSimulationScaler dynamicSimulationScaler;
    private VillagerOptimizer villagerOptimizer;
    private PlayerDiagnosticsManager playerDiagnosticsManager;
    private DeathShieldManager deathShieldManager;
    private MiningFilterManager miningFilterManager;
    private GameAIOptimizer gameAIOptimizer;
    private AdvancedPerformanceMonitor advancedPerformanceMonitor;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.langManager = new LangManager(this);
        this.itemRarityShieldManager = new ItemRarityShieldManager(this);
        this.lagRecoveryManager = new LagRecoveryManager(this);
        this.blameManager = new BlameManager(this);
        this.mobHibernationManager = new MobHibernationManager(this);
        this.holographicStackManager = new HolographicStackManager(this);
        this.redstoneSentinelManager = new RedstoneSentinelManager(this);
        this.dynamicSimulationScaler = new DynamicSimulationScaler(this);
        this.villagerOptimizer = new VillagerOptimizer(this);
        this.playerDiagnosticsManager = new PlayerDiagnosticsManager(this);
        this.deathShieldManager = new DeathShieldManager(this);
        this.miningFilterManager = new MiningFilterManager(this);
        this.gameAIOptimizer = new GameAIOptimizer(this);
        this.advancedPerformanceMonitor = new AdvancedPerformanceMonitor(this);
        this.pulseManager = new PulseManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(mobHibernationManager, this);
        getServer().getPluginManager().registerEvents(holographicStackManager, this);
        getServer().getPluginManager().registerEvents(redstoneSentinelManager, this);
        getServer().getPluginManager().registerEvents(deathShieldManager, this);
        getServer().getPluginManager().registerEvents(miningFilterManager, this);
        getServer().getPluginManager().registerEvents(gameAIOptimizer, this);

        // Register commands
        if (getCommand("cleanpulse") != null) {
            getCommand("cleanpulse").setExecutor(new CleanPulseCommand(this));
            getCommand("cleanpulse").setTabCompleter(new CleanPulseTabCompleter());
        }
        if (getCommand("trash") != null) {
            getCommand("trash").setExecutor(new TrashCommand(this));
        }

        getLogger().info("----------------------------------------");
        getLogger().info("CleanPulse | Ultimate Server Optimizer");
        getLogger().info("Version: 2.0.0 by Dafealru");
        getLogger().info("Paper API Native: 1.21.x / Java 21");
        getLogger().info("Advanced Root-Cause AI Monitor & Optimizer Active!");
        getLogger().info("----------------------------------------");
    }

    @Override
    public void onDisable() {
        getLogger().info("CleanPulse v2.0.0 disabled safely.");
    }

    public void reloadAll() {
        reloadConfig();
        langManager.load();
        itemRarityShieldManager.load();
        pulseManager.startAutoScheduler();
        mobHibernationManager.start();
        dynamicSimulationScaler.start();
        villagerOptimizer.start();
        miningFilterManager.load();
        gameAIOptimizer.start();
    }

    public static CleanPulse getInstance() { return instance; }
    public LangManager getLang() { return langManager; }
    public PulseManager getPulseManager() { return pulseManager; }
    public ItemRarityShieldManager getItemRarityShieldManager() { return itemRarityShieldManager; }
    public LagRecoveryManager getLagRecoveryManager() { return lagRecoveryManager; }
    public BlameManager getBlameManager() { return blameManager; }
    public MobHibernationManager getMobHibernationManager() { return mobHibernationManager; }
    public HolographicStackManager getHolographicStackManager() { return holographicStackManager; }
    public RedstoneSentinelManager getRedstoneSentinelManager() { return redstoneSentinelManager; }
    public DynamicSimulationScaler getDynamicSimulationScaler() { return dynamicSimulationScaler; }
    public VillagerOptimizer getVillagerOptimizer() { return villagerOptimizer; }
    public PlayerDiagnosticsManager getPlayerDiagnosticsManager() { return playerDiagnosticsManager; }
    public DeathShieldManager getDeathShieldManager() { return deathShieldManager; }
    public MiningFilterManager getMiningFilterManager() { return miningFilterManager; }
    public GameAIOptimizer getGameAIOptimizer() { return gameAIOptimizer; }
    public AdvancedPerformanceMonitor getAdvancedPerformanceMonitor() { return advancedPerformanceMonitor; }
}
