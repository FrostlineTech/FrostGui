package com.yourusername.frostgui;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.yourusername.frostgui.commands.HologramCommand;
import com.yourusername.frostgui.hologram.HologramManager;
import com.yourusername.frostgui.listeners.HologramListener;

/**
 * Main class for the FrostGUI plugin
 */
public class FrostGUI extends JavaPlugin implements Listener {
    
    private FileConfiguration config;
    private BukkitTask tabUpdateTask;
    private HologramManager hologramManager;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("FrostGUI has been enabled!");
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Load configuration
        config = getConfig();
        
        // Register events if welcome messages are enabled
        if (config.getBoolean("settings.enable-welcome-messages")) {
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("Welcome messages have been enabled!");
        }
        
        // Start tab list update task if enabled
        if (config.getBoolean("tab-list.enabled", true)) {
            startTabListUpdateTask();
            getLogger().info("Tab list customization has been enabled!");
        }
        
        // Initialize hologram manager
        hologramManager = new HologramManager(this);
        
        // Register hologram command
        HologramCommand hologramCommand = new HologramCommand(this, hologramManager);
        getCommand("hologram").setExecutor(hologramCommand);
        getCommand("hologram").setTabCompleter(hologramCommand);
        
        // Register hologram listener for 1.17+ entity interaction protection
        HologramListener hologramListener = new HologramListener(this);
        getServer().getPluginManager().registerEvents(hologramListener, this);
        
        // Show all holograms if enabled
        if (config.getBoolean("holograms.enabled", true)) {
            hologramManager.showAllHolograms();
            getLogger().info("Holograms have been enabled and loaded!");
        }
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("FrostGUI has been disabled!");
        
        // Cancel tab list update task if running
        if (tabUpdateTask != null) {
            tabUpdateTask.cancel();
            tabUpdateTask = null;
        }
        
        // Remove all holograms from the world
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
            hologramManager.saveAllHolograms();
            getLogger().info("All holograms have been saved and removed!");
        }
    }
    
    /**
     * Starts the recurring task to update the player tab list header/footer
     */
    private void startTabListUpdateTask() {
        // Cancel existing task if running
        if (tabUpdateTask != null) {
            tabUpdateTask.cancel();
        }
        
        // Get update interval from config (in seconds)
        int updateInterval = config.getInt("tab-list.update-interval", 30) * 20; // Convert to ticks
        
        // Start new task
        tabUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateTabListForAllPlayers();
            }
        }.runTaskTimer(this, 20, updateInterval); // 20 tick delay (1 second) before first run
        
        // Update immediately for players already online
        updateTabListForAllPlayers();
    }
    
    /**
     * Updates the tab list header/footer for all online players
     */
    private void updateTabListForAllPlayers() {
        if (!config.getBoolean("tab-list.enabled", true)) {
            return;
        }
        
        // Get header and footer from config
        String header = ChatColor.translateAlternateColorCodes('&', 
                config.getString("tab-list.header", "&b&lFrostCraft Development Server"));
        String footer = ChatColor.translateAlternateColorCodes('&',
                config.getString("tab-list.footer", "&7Have a great time on our server!"));
        
        // Update for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeaderFooter(header, footer);
        }
    }
    
    /**
     * Handle the /frostgui command
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Handle FrostGUI command
        if (cmd.getName().equalsIgnoreCase("frostgui")) {
            // Handle reload command
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("frostgui.admin")) {
                    reloadConfig();
                    config = getConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            config.getString("messages.prefix") + "Configuration reloaded!"));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            config.getString("messages.no-permission")));
                }
                return true;
            }
            
            // Send plugin info
            sender.sendMessage(ChatColor.AQUA + "⚡ FrostGUI " + ChatColor.GRAY + "v" + getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "A customizable welcome message plugin");
            if (sender.hasPermission("frostgui.admin")) {
                sender.sendMessage(ChatColor.GRAY + "Use /frostgui reload to reload the configuration");
            }
            return true;
        }
        
        // Handle Discord command
        if (cmd.getName().equalsIgnoreCase("discord")) {
            if (!config.getBoolean("discord.enabled", true)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        config.getString("messages.prefix") + "&cThe Discord feature is currently disabled."));
                return true;
            }
            
            String discordLink = config.getString("discord.link", "https://discord.gg/yourserver");
            String discordMessage = config.getString("discord.message", "&a&lJoin our Discord server: &b{link}")
                    .replace("{link}", discordLink);
            
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.prefix") + discordMessage));
            
            return true;
        }
        
        // Handle Support command for admins
        if (cmd.getName().equalsIgnoreCase("support")) {
            if (sender.hasPermission("frostgui.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        "&b&l[FrostGUI Support] &aJoin Frostline's discord for plugin support! &b&nhttps://discord.gg/FGUEEj6k7k"));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        config.getString("messages.prefix") + config.getString("messages.no-permission")));
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle player join events
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Set custom join message if broadcast is enabled
        if (config.getBoolean("welcome.broadcast-join")) {
            String joinMessage = config.getString("welcome.join-message", "&a{player_name} &fhas joined the server!")
                    .replace("{player_name}", player.getName());
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.prefix") + joinMessage));
        } else {
            // Disable default join message
            event.setJoinMessage(null);
        }
        
        // Handle first join message (only for new players)
        if (!player.hasPlayedBefore() && config.getBoolean("welcome.first-join.enabled")) {
            if (config.getBoolean("welcome.first-join.broadcast")) {
                String firstJoinMsg = config.getString("welcome.first-join.message")
                        .replace("{player_name}", player.getName());
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', firstJoinMsg));
            }
        }
        
        // Send personal chat welcome message
        if (config.getBoolean("welcome.chat-welcome.enabled")) {
            String chatMessage = config.getString("welcome.chat-welcome.message")
                    .replace("{player_name}", player.getName());
            if (config.getBoolean("welcome.chat-welcome.colorful", true)) {
                chatMessage = ChatColor.translateAlternateColorCodes('&', chatMessage);
            }
            player.sendMessage(chatMessage);
        }
        
        // Apply tab list header/footer for the player
        if (config.getBoolean("tab-list.enabled", true)) {
            String header = ChatColor.translateAlternateColorCodes('&', 
                    config.getString("tab-list.header", "&b&lFrostCraft Development Server"));
            String footer = ChatColor.translateAlternateColorCodes('&',
                    config.getString("tab-list.footer", "&7Have a great time on our server!"));
            player.setPlayerListHeaderFooter(header, footer);
        }
    }
}
