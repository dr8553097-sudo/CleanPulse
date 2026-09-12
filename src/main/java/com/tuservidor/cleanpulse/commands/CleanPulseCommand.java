package com.tuservidor.cleanpulse.commands;

import com.tuservidor.cleanpulse.CleanPulse;
import com.tuservidor.cleanpulse.gui.PerformanceGui;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CleanPulseCommand implements CommandExecutor {
    private final CleanPulse plugin;

    public CleanPulseCommand(CleanPulse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p && p.hasPermission("cleanpulse.admin")) {
                new PerformanceGui(plugin).open(p);
                return true;
            }
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "pulse":
                if (!sender.hasPermission("cleanpulse.admin")) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
                    return true;
                }
                plugin.getPulseManager().executeOptimizationPulse(sender.getName());
                break;

            case "gui":
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.only-players"));
                    return true;
                }
                if (!p.hasPermission("cleanpulse.admin")) {
                    p.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
                    return true;
                }
                new PerformanceGui(plugin).open(p);
                break;

            case "blame":
            case "top":
                if (!sender.hasPermission("cleanpulse.admin")) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
                    return true;
                }
                plugin.getBlameManager().executeBlame(sender);
                break;

            case "myfarm":
            case "lagcheck":
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.only-players"));
                    return true;
                }
                plugin.getPlayerDiagnosticsManager().checkZone(p);
                break;

            case "restore":
                if (!sender.hasPermission("cleanpulse.admin")) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
                    return true;
                }
                if (args.length > 1 && !args[1].equalsIgnoreCase("all")) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer not found."));
                        return true;
                    }
                    int r = plugin.getLagRecoveryManager().restorePlayer(target.getUniqueId());
                    sender.sendMessage(plugin.getLang().getComponent(r > 0 ? "commands.restored-items" : "commands.restore-empty"));
                } else {
                    int r = plugin.getLagRecoveryManager().restoreAll();
                    sender.sendMessage(plugin.getLang().getComponent(r > 0 ? "commands.restored-items" : "commands.restore-empty"));
                }
                break;

            case "filter":
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.only-players"));
                    return true;
                }
                boolean state = plugin.getMiningFilterManager().toggleFilter(p);
                String stateStr = state ? "&aENABLED" : "&cDISABLED";
                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        plugin.getLang().getPrefixed("commands.filter-toggled").replace("{state}", stateStr)));
                break;

            case "death":
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.only-players"));
                    return true;
                }
                plugin.getDeathShieldManager().sendDeathShieldStatus(p);
                break;

            case "reload":
                if (!sender.hasPermission("cleanpulse.admin")) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
                    return true;
                }
                plugin.reloadAll();
                sender.sendMessage(plugin.getLang().getComponent("commands.reload-success"));
                break;

            case "lang":
                if (!sender.hasPermission("cleanpulse.admin")) {
                    sender.sendMessage(plugin.getLang().getComponent("commands.no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&eActive language: &d" + plugin.getLang().getActiveLang() + " &7(Usage: /cp lang <en|es|fr|pt>)"));
                    return true;
                }
                plugin.getLang().setLanguage(args[1].toLowerCase());
                sender.sendMessage(plugin.getLang().getComponent("commands.reload-success"));
                break;

            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8&m-----------------&r &d&lCleanPulse v2.0.0 &8&m-----------------"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp gui &7- Open interactive control dashboard"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp pulse &7- Instant zero-loss optimization pulse"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp blame &7- Instant diagnostic of top lag hot-spots"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp restore [player|all] &7- Restore items from recovery buffer"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp myfarm &7- Check entity health of your current zone"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp filter &7- Toggle auto-mining junk void filter"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp death &7- Check your active death shield"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/cp reload &7- Reload configuration & language"));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7• &d/trash &7- Open disposable trash bin"));
    }
}
