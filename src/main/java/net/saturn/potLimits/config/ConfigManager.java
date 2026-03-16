package net.saturn.potLimits.config;

import net.saturn.potLimits.PotLimits;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final PotLimits plugin;
    private List<String> disabledPotions;

    public ConfigManager(PotLimits plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        disabledPotions = config.getStringList("disabled-potions");

        if (disabledPotions == null) {
            disabledPotions = new ArrayList<>();
        }
    }

    public boolean isDisabled(PotionEffectType type) {
        if (type == null) return false;
        return disabledPotions.contains(type.getName());
    }

    public void disablePotion(PotionEffectType type) {
        if (type == null) return;

        String name = type.getName();
        if (!disabledPotions.contains(name)) {
            disabledPotions.add(name);
            saveConfig();
        }
    }

    public void enablePotion(PotionEffectType type) {
        if (type == null) return;

        String name = type.getName();
        if (disabledPotions.remove(name)) {
            saveConfig();
        }
    }

    public List<PotionEffectType> getDisabledPotions() {
        return disabledPotions.stream()
                .map(PotionEffectType::getByName)
                .filter(type -> type != null)
                .collect(Collectors.toList());
    }

    private void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("disabled-potions", disabledPotions);
        plugin.saveConfig();
    }

    public String getBlockMessage() {
        return plugin.getConfig().getString("messages.potion-blocked", "§cThis potion effect is disabled!");
    }
}