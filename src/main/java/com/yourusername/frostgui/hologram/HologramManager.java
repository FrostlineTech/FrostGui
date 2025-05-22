package com.yourusername.frostgui.hologram;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all holograms in the plugin
 */
public class HologramManager {
    private JavaPlugin plugin;
    private Map<String, Hologram> holograms;
    private File hologramsFile;
    private FileConfiguration hologramsConfig;

    /**
     * Creates a new HologramManager
     * 
     * @param plugin The JavaPlugin instance
     */
    public HologramManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        this.hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        
        // Create config file if it doesn't exist
        if (!hologramsFile.exists()) {
            try {
                hologramsFile.getParentFile().mkdirs();
                
                // Copy default config from resources
                plugin.saveResource("holograms-default.yml", false);
                
                // Load default config
                File defaultConfigFile = new File(plugin.getDataFolder(), "holograms-default.yml");
                if (defaultConfigFile.exists()) {
                    // Copy default config to actual config
                    FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigFile);
                    defaultConfig.save(hologramsFile);
                    
                    // Optionally delete the default file
                    defaultConfigFile.delete();
                } else {
                    // If default resource copying failed, create an empty file
                    hologramsFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create holograms.yml: " + e.getMessage());
                
                // Fallback - try to create an empty file
                try {
                    hologramsFile.createNewFile();
                } catch (IOException ex) {
                    plugin.getLogger().severe("Fallback creation also failed: " + ex.getMessage());
                }
            }
        }
        
        this.hologramsConfig = YamlConfiguration.loadConfiguration(hologramsFile);
        
        // Load holograms from config
        loadHolograms();
    }

    /**
     * Creates a new hologram
     * 
     * @param id       Unique identifier for the hologram
     * @param location Location where the hologram should appear
     * @param lines    Lines of text to display
     * @return The created hologram, or null if one with the ID already exists
     */
    public Hologram createHologram(String id, Location location, List<String> lines) {
        if (holograms.containsKey(id)) {
            return null; // Hologram with this ID already exists
        }
        
        Hologram hologram = new Hologram(plugin, id, location, lines);
        holograms.put(id, hologram);
        saveHologram(hologram);
        
        return hologram;
    }

    /**
     * Gets a hologram by its ID
     * 
     * @param id The hologram ID
     * @return The hologram, or null if not found
     */
    public Hologram getHologram(String id) {
        return holograms.get(id);
    }

    /**
     * Removes a hologram by its ID
     * 
     * @param id The hologram ID
     * @return True if removed, false if not found
     */
    public boolean removeHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            hologram.remove();
            hologramsConfig.set("holograms." + id, null);
            saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Shows all holograms in the world
     */
    public void showAllHolograms() {
        for (Hologram hologram : holograms.values()) {
            hologram.show();
        }
    }

    /**
     * Removes all holograms from the world
     */
    public void removeAllHolograms() {
        for (Hologram hologram : holograms.values()) {
            hologram.remove();
        }
    }

    /**
     * Gets all holograms
     * 
     * @return Map of hologram IDs to hologram instances
     */
    public Map<String, Hologram> getAllHolograms() {
        return new HashMap<>(holograms);
    }

    /**
     * Loads holograms from the config file
     */
    private void loadHolograms() {
        ConfigurationSection section = hologramsConfig.getConfigurationSection("holograms");
        if (section == null) {
            return;
        }
        
        for (String id : section.getKeys(false)) {
            ConfigurationSection hologramSection = section.getConfigurationSection(id);
            if (hologramSection == null) {
                continue;
            }
            
            // Load location
            String worldName = hologramSection.getString("world");
            double x = hologramSection.getDouble("x");
            double y = hologramSection.getDouble("y");
            double z = hologramSection.getDouble("z");
            
            // Skip if world doesn't exist or isn't loaded
            if (plugin.getServer().getWorld(worldName) == null) {
                plugin.getLogger().warning("Skipping hologram '" + id + "' as world '" + worldName + "' is not loaded");
                continue;
            }
            
            Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z);
            
            // Load lines
            List<String> lines = hologramSection.getStringList("lines");
            
            // Create the hologram
            Hologram hologram = new Hologram(plugin, id, location, lines);
            holograms.put(id, hologram);
        }
    }

    /**
     * Saves a hologram to the config
     * 
     * @param hologram The hologram to save
     */
    private void saveHologram(Hologram hologram) {
        String id = hologram.getId();
        Location location = hologram.getLocation();
        
        hologramsConfig.set("holograms." + id + ".world", location.getWorld().getName());
        hologramsConfig.set("holograms." + id + ".x", location.getX());
        hologramsConfig.set("holograms." + id + ".y", location.getY());
        hologramsConfig.set("holograms." + id + ".z", location.getZ());
        hologramsConfig.set("holograms." + id + ".lines", hologram.getLines());
        
        saveConfig();
    }

    /**
     * Saves all holograms to the config file
     */
    public void saveAllHolograms() {
        // Clear the current section
        hologramsConfig.set("holograms", null);
        
        // Save each hologram
        for (Hologram hologram : holograms.values()) {
            saveHologram(hologram);
        }
    }

    /**
     * Saves the holograms config to disk
     */
    private void saveConfig() {
        try {
            hologramsConfig.save(hologramsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save holograms.yml: " + e.getMessage());
        }
    }
}
