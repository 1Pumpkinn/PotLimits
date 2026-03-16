package net.saturn.potLimits.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.saturn.potLimits.PotLimits;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PotLimitCommand implements BasicCommand {

    private final PotLimits plugin;

    public PotLimitCommand(PotLimits plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        CommandSender sender = stack.getSender();

        if (!sender.hasPermission("potlimits.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {

            case "disable":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /potlimit disable <potion_effect>");
                    sender.sendMessage("§7Examples: /potlimit disable strength2, /potlimit disable speed");
                    return;
                }
                handleDisable(sender, args[1]);
                break;

            case "enable":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /potlimit enable <potion_effect>");
                    sender.sendMessage("§7Examples: /potlimit enable strength2, /potlimit enable speed");
                    return;
                }
                handleEnable(sender, args[1]);
                break;

            case "list":
                handleList(sender);
                break;

            case "reload":
                plugin.reloadPluginConfig();
                sender.sendMessage("§aConfiguration reloaded!");
                break;

            default:
                sendHelp(sender);
        }
    }

    private void handleDisable(CommandSender sender, String potionName) {

        PotionInfo info = parsePotionName(potionName);

        if (info.type == null) {
            sender.sendMessage("§cInvalid potion effect: " + potionName);
            return;
        }

        if (plugin.getConfigManager().isDisabled(info.type, info.level)) {
            sender.sendMessage("§e" + formatPotionName(info.type, info.level) + " is already disabled.");
            return;
        }

        plugin.getConfigManager().disablePotion(info.type, info.level);
        sender.sendMessage("§aDisabled potion effect: " + formatPotionName(info.type, info.level));
    }

    private void handleEnable(CommandSender sender, String potionName) {

        PotionInfo info = parsePotionName(potionName);

        if (info.type == null) {
            sender.sendMessage("§cInvalid potion effect: " + potionName);
            return;
        }

        if (!plugin.getConfigManager().isDisabled(info.type, info.level)) {
            sender.sendMessage("§e" + formatPotionName(info.type, info.level) + " is already enabled.");
            return;
        }

        plugin.getConfigManager().enablePotion(info.type, info.level);
        sender.sendMessage("§aEnabled potion effect: " + formatPotionName(info.type, info.level));
    }

    private void handleList(CommandSender sender) {

        Map<PotionEffectType, List<Integer>> disabled = plugin.getConfigManager().getDisabledPotions();

        if (disabled.isEmpty()) {
            sender.sendMessage("§eNo potion effects are currently disabled.");
            return;
        }

        sender.sendMessage("§6§lDisabled Potion Effects:");

        for (Map.Entry<PotionEffectType, List<Integer>> entry : disabled.entrySet()) {
            PotionEffectType type = entry.getKey();
            List<Integer> levels = entry.getValue();

            if (levels.contains(-1)) {
                // All levels disabled
                sender.sendMessage(" §7- §c" + formatPotionName(type, -1));
            } else {
                // Specific levels disabled
                for (int level : levels) {
                    sender.sendMessage(" §7- §c" + formatPotionName(type, level));
                }
            }
        }
    }

    private void sendHelp(CommandSender sender) {

        sender.sendMessage("§6§lPotLimits Commands:");
        sender.sendMessage(" §e/potlimit disable <effect> §7- Disable a potion effect");
        sender.sendMessage(" §e/potlimit enable <effect> §7- Enable a potion effect");
        sender.sendMessage(" §e/potlimit list §7- List disabled effects");
        sender.sendMessage(" §e/potlimit reload §7- Reload configuration");
        sender.sendMessage("");
        sender.sendMessage("§7Examples:");
        sender.sendMessage(" §e/potlimit disable strength2 §7- Disable only Strength II");
        sender.sendMessage(" §e/potlimit disable strength §7- Disable all levels of Strength");
        sender.sendMessage(" §e/potlimit enable speed2 §7- Enable Speed II");
    }

    /**
     * Parse potion name with optional level
     * Examples: "strength2" -> Strength with level 1 (II)
     *           "strength" -> Strength with level -1 (all levels)
     *           "speed_2" -> Speed with level 1 (II)
     */
    private PotionInfo parsePotionName(String input) {

        String normalized = input.toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        // Check for level suffix (2, II, _2, _II)
        int level = -1; // -1 means all levels

        // Pattern: STRENGTH2, STRENGTHII, STRENGTH_2, STRENGTH_II
        if (normalized.matches(".*[_]?(2|II)$")) {
            level = 1; // Amplifier 1 = Level II
            normalized = normalized.replaceAll("[_]?(2|II)$", "");
        } else if (normalized.matches(".*[_]?(1|I)$")) {
            level = 0; // Amplifier 0 = Level I
            normalized = normalized.replaceAll("[_]?(1|I)$", "");
        }

        PotionEffectType type = PotionEffectType.getByName(normalized);

        return new PotionInfo(type, level);
    }

    /**
     * Format potion name for display
     */
    private String formatPotionName(PotionEffectType type, int level) {
        String base = type.getName().toLowerCase().replace("_", " ");

        if (level == -1) {
            return base + " (all levels)";
        } else if (level == 0) {
            return base + " I";
        } else if (level == 1) {
            return base + " II";
        } else {
            return base + " " + (level + 1);
        }
    }

    // Tab completion (Paper automatically detects this method)
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        CommandSender sender = stack.getSender();

        if (!sender.hasPermission("potlimits.admin")) {
            return Collections.emptyList();
        }

        List<String> baseCommands = Arrays.asList("disable", "enable", "list", "reload");

        // /potlimit <TAB>
        if (args.length == 0) {
            return baseCommands;
        }

        // /potlimit d<TAB>
        if (args.length == 1) {
            return baseCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // /potlimit disable <TAB>
        if (args.length == 2 &&
                (args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("enable"))) {

            List<String> potions = Arrays.asList(
                    "strength",
                    "strength1",
                    "strength2",
                    "speed",
                    "speed1",
                    "speed2",
                    "jump_boost",
                    "jump_boost1",
                    "jump_boost2",
                    "regeneration",
                    "regeneration1",
                    "regeneration2",
                    "fire_resistance",
                    "water_breathing",
                    "invisibility",
                    "night_vision",
                    "weakness",
                    "poison",
                    "poison1",
                    "poison2",
                    "slowness",
                    "slowness1",
                    "slowness4",
                    "turtle_master",
                    "turtle_master1",
                    "turtle_master2",
                    "slow_falling"
            );

            return potions.stream()
                    .filter(p -> p.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Helper class to store parsed potion information
     */
    private static class PotionInfo {
        final PotionEffectType type;
        final int level; // -1 = all levels, 0 = I, 1 = II, etc.

        PotionInfo(PotionEffectType type, int level) {
            this.type = type;
            this.level = level;
        }
    }
}