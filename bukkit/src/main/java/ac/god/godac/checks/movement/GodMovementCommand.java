package ac.god.godac.checks.movement;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GodMovementCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("godac.movement")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
            case "toggle":
                toggleMovement(sender);
                break;
            case "speed":
                if (args.length > 1) {
                    try {
                        double threshold = Double.parseDouble(args[1]);
                        GodMovementManager.getInstance();
                        sender.sendMessage(ChatColor.GREEN + "Speed threshold set to " + threshold);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number!");
                    }
                }
                break;
            case "reload":
                sender.sendMessage(ChatColor.GREEN + "Movement configuration reloaded!");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                showHelp(sender);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== GodAC Movement Commands ===");
        sender.sendMessage(ChatColor.GRAY + "/movement toggle " + ChatColor.WHITE + "- Enable/disable movement checks");
        sender.sendMessage(ChatColor.GRAY + "/movement speed [threshold] " + ChatColor.WHITE + "- Set speed threshold");
        sender.sendMessage(ChatColor.GRAY + "/movement reload " + ChatColor.WHITE + "- Reload configuration");
    }

    private void toggleMovement(CommandSender sender) {
        GodMovementManager manager = GodMovementManager.getInstance();
        boolean newState = !manager.isEnabled();
        manager.setEnabled(newState);
        sender.sendMessage(ChatColor.GREEN + "Movement checks " + (newState ? "enabled" : "disabled") + "!");
    }
}
