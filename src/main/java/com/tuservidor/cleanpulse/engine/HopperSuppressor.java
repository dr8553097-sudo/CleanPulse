package com.tuservidor.cleanpulse.engine;

import com.tuservidor.cleanpulse.CleanPulse;
import org.bukkit.event.Listener;

public class HopperSuppressor implements Listener {
    private final CleanPulse plugin;

    public HopperSuppressor(CleanPulse plugin) {
        this.plugin = plugin;
    }
}
