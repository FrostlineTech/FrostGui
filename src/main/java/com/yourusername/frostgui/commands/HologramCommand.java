package com.yourusername.frostgui.commands;

import com.yourusername.frostgui.FrostGUI;
import com.yourusername.frostgui.hologram.Hologram;
import com.yourusername.frostgui.hologram.HologramManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command handler for hologram-related commands
 */
public class HologramCommand implements CommandExecutor, TabCompleter {
    
    private final FrostGUI plugin;
    private final HologramManager hologramManager;
    
    public HologramCommand(FrostGUI plugin, HologramManager hologramManager) {
        this.plugin = plugin;
        this.hologramManager = hologramManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("frostgui.hologram")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.prefix") + 
                    plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        
        // No arguments - show help
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        // Process subcommands
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "remove":
                return handleRemove(player, args);
            case "list":
                return handleList(player);
            case "teleport":
            case "tp":
                return handleTeleport(player, args);
            case "addline":
                return handleAddLine(player, args);
            case "removeline":
                return handleRemoveLine(player, args);
            case "move":
                return handleMove(player, args);
            case "edit":
                return handleEdit(player, args);
            case "info":
                return handleInfo(player, args);
            default:
                showHelp(player);
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        // Check if enough arguments: /hologram create <id> <text...>
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram create <id> <text...>");
            return true;
        }
        
        String id = args[1];
        
        // Check if hologram with this ID already exists
        if (hologramManager.getHologram(id) != null) {
            player.sendMessage(ChatColor.RED + "A hologram with ID '" + id + "' already exists.");
            return true;
        }
        
        // Combine remaining arguments into a single line
        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        List<String> lines = new ArrayList<>();
        lines.add(text);
        
        // Create hologram at player's location
        Location location = player.getLocation().clone();
        Hologram hologram = hologramManager.createHologram(id, location, lines);
        hologram.show();
        
        player.sendMessage(ChatColor.GREEN + "Hologram '" + id + "' created successfully.");
        return true;
    }
    
    private boolean handleRemove(Player player, String[] args) {
        // Check if enough arguments: /hologram remove <id>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram remove <id>");
            return true;
        }
        
        String id = args[1];
        
        // Try to remove the hologram
        boolean removed = hologramManager.removeHologram(id);
        
        if (removed) {
            player.sendMessage(ChatColor.GREEN + "Hologram '" + id + "' removed successfully.");
        } else {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
        }
        
        return true;
    }
    
    private boolean handleList(Player player) {
        Map<String, Hologram> holograms = hologramManager.getAllHolograms();
        
        if (holograms.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "There are no holograms.");
            return true;
        }
        
        player.sendMessage(ChatColor.GREEN + "List of holograms:");
        for (Map.Entry<String, Hologram> entry : holograms.entrySet()) {
            Hologram hologram = entry.getValue();
            Location loc = hologram.getLocation();
            player.sendMessage(ChatColor.AQUA + "- " + entry.getKey() + 
                    ChatColor.GRAY + " (" + loc.getWorld().getName() + ", " + 
                    Math.round(loc.getX()) + ", " + Math.round(loc.getY()) + ", " + 
                    Math.round(loc.getZ()) + ") " + 
                    ChatColor.YELLOW + hologram.getLines().size() + " line(s)");
        }
        
        return true;
    }
    
    private boolean handleTeleport(Player player, String[] args) {
        // Check if enough arguments: /hologram tp <id>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram tp <id>");
            return true;
        }
        
        String id = args[1];
        Hologram hologram = hologramManager.getHologram(id);
        
        if (hologram == null) {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
            return true;
        }
        
        player.teleport(hologram.getLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to hologram '" + id + "'.");
        
        return true;
    }
    
    private boolean handleAddLine(Player player, String[] args) {
        // Check if enough arguments: /hologram addline <id> <text...>
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram addline <id> <text...>");
            return true;
        }
        
        String id = args[1];
        Hologram hologram = hologramManager.getHologram(id);
        
        if (hologram == null) {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
            return true;
        }
        
        // Combine remaining arguments into a single line
        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        hologram.addLine(text);
        player.sendMessage(ChatColor.GREEN + "Added line to hologram '" + id + "'.");
        
        return true;
    }
    
    private boolean handleRemoveLine(Player player, String[] args) {
        // Check if enough arguments: /hologram removeline <id> <line_index>
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram removeline <id> <line_index>");
            return true;
        }
        
        String id = args[1];
        Hologram hologram = hologramManager.getHologram(id);
        
        if (hologram == null) {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
            return true;
        }
        
        // Parse line index
        int lineIndex;
        try {
            lineIndex = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Line index must be a number.");
            return true;
        }
        
        // Line indices are zero-based for the code, but we'll make them 1-based for user convenience
        lineIndex--; 
        
        boolean removed = hologram.removeLine(lineIndex);
        
        if (removed) {
            player.sendMessage(ChatColor.GREEN + "Removed line " + (lineIndex + 1) + " from hologram '" + id + "'.");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid line index. The hologram has " + 
                    hologram.getLines().size() + " lines (1-" + hologram.getLines().size() + ").");
        }
        
        return true;
    }
    
    private boolean handleMove(Player player, String[] args) {
        // Check if enough arguments: /hologram move <id>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram move <id>");
            return true;
        }
        
        String id = args[1];
        Hologram hologram = hologramManager.getHologram(id);
        
        if (hologram == null) {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
            return true;
        }
        
        // Update hologram location to player's current location
        hologram.updateLocation(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Moved hologram '" + id + "' to your location.");
        
        return true;
    }
    
    private boolean handleEdit(Player player, String[] args) {
        // Check if enough arguments: /hologram edit <id> <line_index> <new_text...>
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram edit <id> <line_index> <new_text...>");
            return true;
        }
        
        String id = args[1];
        Hologram hologram = hologramManager.getHologram(id);
        
        if (hologram == null) {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
            return true;
        }
        
        // Parse line index
        int lineIndex;
        try {
            lineIndex = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Line index must be a number.");
            return true;
        }
        
        // Line indices are zero-based for the code, but we'll make them 1-based for user convenience
        lineIndex--; 
        
        List<String> lines = hologram.getLines();
        
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            player.sendMessage(ChatColor.RED + "Invalid line index. The hologram has " + 
                    lines.size() + " lines (1-" + lines.size() + ").");
            return true;
        }
        
        // Combine remaining arguments into a single line
        String newText = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        
        // Update the line
        lines.set(lineIndex, newText);
        hologram.updateLines(lines);
        
        player.sendMessage(ChatColor.GREEN + "Updated line " + (lineIndex + 1) + " of hologram '" + id + "'.");
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        // Check if enough arguments: /hologram info <id>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /hologram info <id>");
            return true;
        }
        
        String id = args[1];
        Hologram hologram = hologramManager.getHologram(id);
        
        if (hologram == null) {
            player.sendMessage(ChatColor.RED + "No hologram found with ID '" + id + "'.");
            return true;
        }
        
        Location loc = hologram.getLocation();
        List<String> lines = hologram.getLines();
        
        player.sendMessage(ChatColor.GREEN + "Information for hologram '" + id + "':");
        player.sendMessage(ChatColor.YELLOW + "Location: " + ChatColor.GRAY + loc.getWorld().getName() + ", " + 
                Math.round(loc.getX()) + ", " + Math.round(loc.getY()) + ", " + Math.round(loc.getZ()));
        player.sendMessage(ChatColor.YELLOW + "Lines (" + lines.size() + "):");
        
        for (int i = 0; i < lines.size(); i++) {
            player.sendMessage(ChatColor.AQUA + "  " + (i + 1) + ": " + ChatColor.WHITE + lines.get(i));
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== Hologram Commands ===");
        player.sendMessage(ChatColor.AQUA + "/hologram create <id> <text> " + ChatColor.GRAY + "- Create a new hologram");
        player.sendMessage(ChatColor.AQUA + "/hologram remove <id> " + ChatColor.GRAY + "- Remove a hologram");
        player.sendMessage(ChatColor.AQUA + "/hologram list " + ChatColor.GRAY + "- List all holograms");
        player.sendMessage(ChatColor.AQUA + "/hologram tp <id> " + ChatColor.GRAY + "- Teleport to a hologram");
        player.sendMessage(ChatColor.AQUA + "/hologram addline <id> <text> " + ChatColor.GRAY + "- Add a line to a hologram");
        player.sendMessage(ChatColor.AQUA + "/hologram removeline <id> <line_number> " + ChatColor.GRAY + "- Remove a line");
        player.sendMessage(ChatColor.AQUA + "/hologram edit <id> <line_number> <new_text> " + ChatColor.GRAY + "- Edit a line");
        player.sendMessage(ChatColor.AQUA + "/hologram move <id> " + ChatColor.GRAY + "- Move hologram to your location");
        player.sendMessage(ChatColor.AQUA + "/hologram info <id> " + ChatColor.GRAY + "- Show hologram information");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            String[] subCommands = {"create", "remove", "list", "tp", "teleport", "addline", "removeline", "move", "edit", "info"};
            return filterCompletions(subCommands, args[0]);
        } else if (args.length == 2) {
            // For commands that need a hologram ID as second argument
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("remove") || subCommand.equals("tp") || subCommand.equals("teleport") || 
                    subCommand.equals("addline") || subCommand.equals("removeline") || 
                    subCommand.equals("move") || subCommand.equals("edit") || subCommand.equals("info")) {
                
                return filterCompletions(hologramManager.getAllHolograms().keySet().toArray(new String[0]), args[1]);
            }
        }
        
        return completions;
    }
    
    private List<String> filterCompletions(String[] options, String partial) {
        return Arrays.stream(options)
                .filter(option -> option.toLowerCase().startsWith(partial.toLowerCase()))
                .collect(Collectors.toList());
    }
}
