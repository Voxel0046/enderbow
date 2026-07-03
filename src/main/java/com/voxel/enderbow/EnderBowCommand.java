package com.voxel.enderbow;

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
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("enderbow.reload")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                plugin.reloadEbConfig();
                sender.sendMessage("§aEnderBow config reloaded. Name and lore updated.");
                return true;
            }
        }
        sender.sendMessage("§eEnderBow plugin. Use /enderbow reload");
        return true;
    }
}
