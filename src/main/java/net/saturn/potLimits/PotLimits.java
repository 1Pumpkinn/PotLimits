package net.saturn.potLimits;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.saturn.potLimits.commands.PotLimitCommand;
import net.saturn.potLimits.config.ConfigManager;
import net.saturn.potLimits.listeners.PotionListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class PotLimits extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {

        configManager = new ConfigManager(this);

        // Register command using Paper lifecycle
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {

            Commands commands = event.registrar();

            commands.register(
                    "potlimit",
                    "Manage disabled potion effects",
                    new PotLimitCommand(this)
            );
        });

        // Register listeners
        getServer().getPluginManager().registerEvents(new PotionListener(this), this);

        getLogger().info("PotLimits has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PotLimits has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadPluginConfig() {
        configManager.loadConfig();
    }
}