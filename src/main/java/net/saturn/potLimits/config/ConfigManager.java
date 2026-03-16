package net.saturn.potLimits.config;

import net.saturn.potLimits.PotLimits;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    private final PotLimits plugin;
    // Map of potion name -> list of disabled levels (0 = I, 1 = II, etc.)
    // If list is empty or contains -1, all levels are disabled
    private Map<String, List<Integer>> disabledPotions;

    public ConfigManager(PotLimits plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        disabledPotions = new HashMap<>();

        List<String> configList = config.getStringList("disabled-potions");
        if (configList != null) {
            for (String entry : configList) {
                parseDisabledEntry(entry);
            }
        }
    }

    private void parseDisabledEntry(String entry) {
        // Format: "STRENGTH:1" for Strength II, "SPEED" for all levels of Speed
        String[] parts = entry.split(":");
        String potionName = parts[0];

        if (parts.length == 1) {
            // No level specified - disable all levels
            disabledPotions.put(potionName, new ArrayList<>(List.of(-1)));
        } else {
            // Level specified
            try {
                int level = Integer.parseInt(parts[1]);
                disabledPotions.computeIfAbsent(potionName, k -> new ArrayList<>()).add(level);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid potion level format: " + entry);
            }
        }
    }

    /**
     * Check if a potion effect with a specific amplifier is disabled
     * @param type The potion effect type
     * @param amplifier The amplifier (0 = I, 1 = II, 2 = III, etc.)
     * @return true if this specific level is disabled
     */
    public boolean isDisabled(PotionEffectType type, int amplifier) {
        if (type == null) return false;

        String name = type.getName();
        List<Integer> disabledLevels = disabledPotions.get(name);

        if (disabledLevels == null || disabledLevels.isEmpty()) {
            return false;
        }

        // If -1 is in the list, all levels are disabled
        if (disabledLevels.contains(-1)) {
            return true;
        }

        // Check if this specific level is disabled
        return disabledLevels.contains(amplifier);
    }

    /**
     * Legacy method for backwards compatibility - checks if any level is disabled
     */
    public boolean isDisabled(PotionEffectType type) {
        if (type == null) return false;
        return disabledPotions.containsKey(type.getName());
    }

    /**
     * Disable a specific potion level
     * @param type The potion effect type
     * @param amplifier The amplifier (0 = I, 1 = II, etc.), or -1 for all levels
     */
    public void disablePotion(PotionEffectType type, int amplifier) {
        if (type == null) return;

        String name = type.getName();
        List<Integer> levels = disabledPotions.computeIfAbsent(name, k -> new ArrayList<>());

        if (amplifier == -1) {
            // Disable all levels
            levels.clear();
            levels.add(-1);
        } else if (!levels.contains(-1) && !levels.contains(amplifier)) {
            // Add this specific level if not already disabled
            levels.add(amplifier);
        }

        saveConfig();
    }

    /**
     * Enable a specific potion level
     * @param type The potion effect type
     * @param amplifier The amplifier (0 = I, 1 = II, etc.), or -1 to enable all levels
     */
    public void enablePotion(PotionEffectType type, int amplifier) {
        if (type == null) return;

        String name = type.getName();
        List<Integer> levels = disabledPotions.get(name);

        if (levels == null) return;

        if (amplifier == -1) {
            // Enable all levels
            disabledPotions.remove(name);
        } else {
            levels.remove(Integer.valueOf(amplifier));
            if (levels.isEmpty()) {
                disabledPotions.remove(name);
            }
        }

        saveConfig();
    }

    /**
     * Get all disabled potion entries
     */
    public Map<PotionEffectType, List<Integer>> getDisabledPotions() {
        Map<PotionEffectType, List<Integer>> result = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : disabledPotions.entrySet()) {
            PotionEffectType type = PotionEffectType.getByName(entry.getKey());
            if (type != null) {
                result.put(type, new ArrayList<>(entry.getValue()));
            }
        }

        return result;
    }

    private void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        List<String> configList = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : disabledPotions.entrySet()) {
            String potionName = entry.getKey();
            List<Integer> levels = entry.getValue();

            if (levels.contains(-1)) {
                // All levels disabled
                configList.add(potionName);
            } else {
                // Specific levels disabled
                for (int level : levels) {
                    configList.add(potionName + ":" + level);
                }
            }
        }

        config.set("disabled-potions", configList);
        plugin.saveConfig();
    }

    public String getBlockMessage() {
        return plugin.getConfig().getString("messages.potion-blocked", "§cThis potion effect is disabled!");
    }
}