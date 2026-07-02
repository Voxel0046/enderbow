package com.voxel.enderbow;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EnderBowCommand implements CommandExecutor, TabCompleter {

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
            if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("enderbow.give")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /enderbow give <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found or not online: " + args[1]);
                    return true;
                }
                plugin.giveBowTo(target);
                sender.sendMessage("§aGave EnderBow to " + target.getName());
                return true;
            }
        }
        sender.sendMessage("§eEnderBow plugin. Use /enderbow reload or /enderbow give <player>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete first argument (subcommands)
            if (sender.hasPermission("enderbow.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("enderbow.give")) {
                completions.add("give");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Complete player names for give command
            if (sender.hasPermission("enderbow.give")) {
                String partialName = args[1].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partialName)) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        }

        return completions;
    }
}
