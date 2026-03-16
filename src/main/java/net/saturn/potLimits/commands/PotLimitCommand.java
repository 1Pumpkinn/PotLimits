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
                    return;
                }
                handleDisable(sender, args[1]);
                break;

            case "enable":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /potlimit enable <potion_effect>");
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

        String normalizedName = normalizePotionName(potionName);
        PotionEffectType type = getPotionEffectType(normalizedName);

        if (type == null) {
            sender.sendMessage("§cInvalid potion effect: " + potionName);
            return;
        }

        if (plugin.getConfigManager().isDisabled(type)) {
            sender.sendMessage("§e" + formatPotionName(type) + " is already disabled.");
            return;
        }

        plugin.getConfigManager().disablePotion(type);
        sender.sendMessage("§aDisabled potion effect: " + formatPotionName(type));
    }

    private void handleEnable(CommandSender sender, String potionName) {

        String normalizedName = normalizePotionName(potionName);
        PotionEffectType type = getPotionEffectType(normalizedName);

        if (type == null) {
            sender.sendMessage("§cInvalid potion effect: " + potionName);
            return;
        }

        if (!plugin.getConfigManager().isDisabled(type)) {
            sender.sendMessage("§e" + formatPotionName(type) + " is already enabled.");
            return;
        }

        plugin.getConfigManager().enablePotion(type);
        sender.sendMessage("§aEnabled potion effect: " + formatPotionName(type));
    }

    private void handleList(CommandSender sender) {

        List<PotionEffectType> disabled = plugin.getConfigManager().getDisabledPotions();

        if (disabled.isEmpty()) {
            sender.sendMessage("§eNo potion effects are currently disabled.");
            return;
        }

        sender.sendMessage("§6§lDisabled Potion Effects:");

        for (PotionEffectType type : disabled) {
            sender.sendMessage(" §7- §c" + formatPotionName(type));
        }
    }

    private void sendHelp(CommandSender sender) {

        sender.sendMessage("§6§lPotLimits Commands:");
        sender.sendMessage(" §e/potlimit disable <effect> §7- Disable a potion effect");
        sender.sendMessage(" §e/potlimit enable <effect> §7- Enable a potion effect");
        sender.sendMessage(" §e/potlimit list §7- List disabled effects");
        sender.sendMessage(" §e/potlimit reload §7- Reload configuration");
    }

    private String normalizePotionName(String input) {

        return input.toUpperCase()
                .replace("STRENGTH2", "STRENGTH_2")
                .replace("STRENGTHII", "STRENGTH_2")
                .replace(" ", "_")
                .replace("-", "_");
    }

    private PotionEffectType getPotionEffectType(String name) {

        PotionEffectType type = PotionEffectType.getByName(name);

        if (type != null) {
            return type;
        }

        if (name.endsWith("_2") || name.endsWith("_II")) {
            String base = name.replaceAll("_(2|II)$", "");
            return PotionEffectType.getByName(base);
        }

        return null;
    }

    private String formatPotionName(PotionEffectType type) {
        return type.getName().toLowerCase().replace("_", " ");
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
                    "strength2",
                    "speed",
                    "speed2",
                    "jump_boost",
                    "regeneration",
                    "regeneration2",
                    "fire_resistance",
                    "water_breathing",
                    "invisibility",
                    "night_vision",
                    "weakness",
                    "poison",
                    "slowness",
                    "turtle_master",
                    "slow_falling"
            );

            return potions.stream()
                    .filter(p -> p.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}