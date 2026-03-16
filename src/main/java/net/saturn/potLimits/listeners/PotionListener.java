package net.saturn.potLimits.listeners;

import net.saturn.potLimits.PotLimits;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionConsume(PlayerItemConsumeEvent event) {

        ItemStack item = event.getItem();

        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return;
        }

        for (PotionEffect effect : meta.getCustomEffects()) {

            if (plugin.getConfigManager().isDisabled(effect.getType())) {

                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getConfigManager().getBlockMessage());
                return;
            }
        }
    }

    /*
     * Block splash potions completely
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {

        ThrownPotion potion = event.getPotion();
        PotionMeta meta = (PotionMeta) potion.getItem().getItemMeta();

        if (meta == null) return;

        for (PotionEffect effect : meta.getCustomEffects()) {

            if (plugin.getConfigManager().isDisabled(effect.getType())) {

                event.setCancelled(true);

                if (potion.getShooter() instanceof Player player) {
                    player.sendMessage(plugin.getConfigManager().getBlockMessage());
                }

                return;
            }
        }
    }
}