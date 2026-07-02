package com.voxel.enderbow;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnderBowCommand implements CommandExecutor {

    private final EnderBowPlugin plugin;

    public EnderBowCommand(EnderBowPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("enderbow.reload")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            plugin.reloadEbConfig();
            sender.sendMessage(ChatColor.GREEN + "EnderBow config reloaded.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "EnderBow plugin. Use /enderbow reload");
        return true;
    }
}
