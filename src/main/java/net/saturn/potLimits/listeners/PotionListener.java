package net.saturn.potLimits.listeners;

import net.saturn.potLimits.PotLimits;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public class PotionListener implements Listener {

    private final PotLimits plugin;

    public PotionListener(PotLimits plugin) {
        this.plugin = plugin;
    }

    /*
     * Block drinking disabled potions
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionConsume(PlayerItemConsumeEvent event) {

        ItemStack item = event.getItem();

        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return;
        }

        // Check if any effect in this potion is disabled
        if (hasDisabledEffect(meta)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getConfigManager().getBlockMessage());
        }
    }

    /*
     * Block splash potions with disabled effects
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {

        ThrownPotion potion = event.getPotion();
        ItemStack item = potion.getItem();

        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return;
        }

        // Check if any effect in this potion is disabled
        if (hasDisabledEffect(meta)) {
            event.setCancelled(true);

            if (potion.getShooter() instanceof Player player) {
                player.sendMessage(plugin.getConfigManager().getBlockMessage());
            }
        }
    }

    /*
     * Block lingering potions with disabled effects
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {

        ThrownPotion potion = event.getEntity();
        ItemStack item = potion.getItem();

        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return;
        }

        // Check if any effect in this potion is disabled
        if (hasDisabledEffect(meta)) {
            event.setCancelled(true);

            if (potion.getShooter() instanceof Player player) {
                player.sendMessage(plugin.getConfigManager().getBlockMessage());
            }
        }
    }

    /*
     * Block area effect clouds with disabled effects (from lingering potions)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {

        AreaEffectCloud cloud = event.getEntity();

        // Check all custom effects on the cloud
        for (PotionEffect effect : cloud.getCustomEffects()) {
            if (plugin.getConfigManager().isDisabled(effect.getType(), effect.getAmplifier())) {
                event.setCancelled(true);
                return;
            }
        }

        // Check base potion data
        if (cloud.getBasePotionType() != null) {
            for (PotionEffect effect : cloud.getBasePotionType().getPotionEffects()) {
                if (plugin.getConfigManager().isDisabled(effect.getType(), effect.getAmplifier())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Check if a potion has any disabled effects
     * Checks both base potion data and custom effects
     * Now also checks the amplifier level (0 = I, 1 = II, etc.)
     */
    private boolean hasDisabledEffect(PotionMeta meta) {

        // Check custom effects
        for (PotionEffect effect : meta.getCustomEffects()) {
            if (plugin.getConfigManager().isDisabled(effect.getType(), effect.getAmplifier())) {
                return true;
            }
        }

        // Check base potion data (for non-custom potions)
        if (meta.getBasePotionType() != null) {
            for (PotionEffect effect : meta.getBasePotionType().getPotionEffects()) {
                if (plugin.getConfigManager().isDisabled(effect.getType(), effect.getAmplifier())) {
                    return true;
                }
            }
        }

        return false;
    }
}