package ac.god.godac.checks.combat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GodCombatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("godac.combat")) {
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
                toggleCombat(sender);
                break;
            case "reach":
                if (args.length > 1) {
                    try {
                        double threshold = Double.parseDouble(args[1]);
                        GodCombatManager.getInstance().setReachThreshold(threshold);
                        sender.sendMessage(ChatColor.GREEN + "Reach threshold set to " + threshold);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number!");
                    }
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Current reach threshold: " +
                        GodCombatManager.getInstance().getReachThreshold());
                }
                break;
            case "cps":
                if (args.length > 1) {
                    try {
                        double maxCPS = Double.parseDouble(args[1]);
                        GodCombatManager.getInstance().setMaxCPS(maxCPS);
                        sender.sendMessage(ChatColor.GREEN + "Max CPS set to " + maxCPS);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number!");
                    }
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Current max CPS: " +
                        GodCombatManager.getInstance().getMaxCPS());
                }
                break;
            case "reload":
                sender.sendMessage(ChatColor.GREEN + "Combat configuration reloaded!");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                showHelp(sender);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== GodAC Combat Commands ===");
        sender.sendMessage(ChatColor.GRAY + "/combat toggle " + ChatColor.WHITE + "- Enable/disable combat checks");
        sender.sendMessage(ChatColor.GRAY + "/combat reach [threshold] " + ChatColor.WHITE + "- Set reach threshold");
        sender.sendMessage(ChatColor.GRAY + "/combat cps [max] " + ChatColor.WHITE + "- Set max CPS");
        sender.sendMessage(ChatColor.GRAY + "/combat reload " + ChatColor.WHITE + "- Reload configuration");
    }

    private void toggleCombat(CommandSender sender) {
        GodCombatManager manager = GodCombatManager.getInstance();
        boolean newState = !manager.isEnabled();
        manager.setEnabled(newState);
        sender.sendMessage(ChatColor.GREEN + "Combat checks " + (newState ? "enabled" : "disabled") + "!");
    }
}
