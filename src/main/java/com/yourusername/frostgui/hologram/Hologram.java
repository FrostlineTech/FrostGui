package com.yourusername.frostgui.hologram;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a hologram with multiple text lines
 */
public class Hologram {
    private final String id;
    private Location location;
    private List<String> lines;
    private List<ArmorStand> entities;
    private boolean isVisible;
    private final JavaPlugin plugin;

    /**
     * Creates a new hologram
     * 
     * @param plugin   The plugin instance
     * @param id       Unique identifier for the hologram
     * @param location Location where the hologram should be displayed
     * @param lines    Lines of text to display (supports color codes with &)
     */
    public Hologram(JavaPlugin plugin, String id, Location location, List<String> lines) {
        this.plugin = plugin;
        this.id = id;
        this.location = location.clone();
        this.lines = new ArrayList<>(lines);
        this.entities = new ArrayList<>();
        this.isVisible = false;
    }

    /**
     * Creates the hologram entities in the world
     */
    public void show() {
        if (isVisible) {
            return;
        }

        // Remove any existing entities
        remove();
        
        // Calculate starting position (start from top line)
        Location currentLocation = location.clone();
        
        // Create an armor stand for each line
        for (String line : lines) {
            // Convert color codes
            String coloredLine = ChatColor.translateAlternateColorCodes('&', line);
            
            // Create invisible armor stand (with null-safety for 1.17+)
            ArmorStand stand = (ArmorStand) Objects.requireNonNull(location.getWorld()).spawnEntity(currentLocation, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setCustomName(coloredLine);
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            
            // 1.17+ specific features
            stand.setCollidable(false);  // Prevent entity collisions in 1.17+
            
            // Mark this entity as a hologram using PersistentDataContainer (1.17+ feature)
            NamespacedKey key = new NamespacedKey(plugin, "frostgui_hologram");
            PersistentDataContainer container = stand.getPersistentDataContainer();
            container.set(key, PersistentDataType.STRING, id);
            
            entities.add(stand);
            
            // Move down for next line (using spacing from config)
            double lineSpacing = plugin.getConfig().getDouble("holograms.line-spacing", 0.25);
            currentLocation = currentLocation.subtract(0, lineSpacing, 0);
        }
        
        isVisible = true;
    }

    /**
     * Removes all hologram entities from the world
     */
    public void remove() {
        if (entities != null) {
            for (ArmorStand stand : entities) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
            entities.clear();
        }
        isVisible = false;
    }

    /**
     * Updates the text lines of the hologram
     * 
     * @param newLines New lines of text
     */
    public void updateLines(List<String> newLines) {
        this.lines = new ArrayList<>(newLines);
        if (isVisible) {
            // Refresh the hologram
            show();
        }
    }

    /**
     * Updates the location of the hologram
     * 
     * @param newLocation New location
     */
    public void updateLocation(Location newLocation) {
        this.location = newLocation.clone();
        if (isVisible) {
            // Refresh the hologram
            show();
        }
    }

    /**
     * Adds a line to the end of the hologram
     * 
     * @param line Line to add
     */
    public void addLine(String line) {
        lines.add(line);
        if (isVisible) {
            // Refresh the hologram
            show();
        }
    }

    /**
     * Removes a specific line from the hologram
     * 
     * @param index Index of the line to remove
     * @return True if removed, false if index is out of bounds
     */
    public boolean removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            if (isVisible) {
                // Refresh the hologram
                show();
            }
            return true;
        }
        return false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location.clone();
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    public boolean isVisible() {
        return isVisible;
    }
}
