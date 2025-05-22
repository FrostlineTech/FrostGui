package com.yourusername.frostgui.listeners;

import com.yourusername.frostgui.FrostGUI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles events related to holograms
 */
public class HologramListener implements Listener {
    
    private final NamespacedKey hologramKey;
    
    public HologramListener(FrostGUI plugin) {
        this.hologramKey = new NamespacedKey(plugin, "frostgui_hologram");
    }
    
    /**
     * Prevents players from manipulating hologram armor stands
     */
    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        ArmorStand stand = event.getRightClicked();
        if (isHologramEntity(stand)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Prevents players from interacting with hologram armor stands
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof ArmorStand && isHologramEntity((ArmorStand) entity)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Prevents entities from damaging hologram armor stands
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ArmorStand && isHologramEntity((ArmorStand) entity)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Checks if an armor stand is a hologram entity
     */
    private boolean isHologramEntity(ArmorStand stand) {
        return stand.getPersistentDataContainer().has(hologramKey, PersistentDataType.STRING);
    }
}
