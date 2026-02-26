package ac.god.godac.antispoof;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiSpoofCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("godac.antispoof")) {
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
            case "?":
                showHelp(sender);
                break;
            case "reload":
                sender.sendMessage(ChatColor.GREEN + "AntiSpoof configuration reloaded!");
                break;
            case "check":
                if (args.length < 2) {
                    checkAllPlayers(sender);
                } else {
                    checkPlayer(sender, args[1]);
                }
                break;
            case "toggle":
                toggleAntiSpoof(sender);
                break;
            case "debug":
                toggleDebug(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                showHelp(sender);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== GodAC AntiSpoof Commands ===");
        sender.sendMessage(ChatColor.GRAY + "/antispoof check [player] " + ChatColor.WHITE + "- Check player spoof status");
        sender.sendMessage(ChatColor.GRAY + "/antispoof toggle " + ChatColor.WHITE + "- Enable/disable AntiSpoof");
        sender.sendMessage(ChatColor.GRAY + "/antispoof debug " + ChatColor.WHITE + "- Toggle debug mode");
        sender.sendMessage(ChatColor.GRAY + "/antispoof reload " + ChatColor.WHITE + "- Reload configuration");
    }

    private void checkAllPlayers(CommandSender sender) {
        AntiSpoofManager manager = AntiSpoofManager.getInstance();
        if (manager == null) {
            sender.sendMessage(ChatColor.RED + "AntiSpoof is not enabled!");
            return;
        }

        sender.sendMessage(ChatColor.AQUA + "=== Players Currently Flagging ===");

        boolean found = false;
        for (Player player : sender.getServer().getOnlinePlayers()) {
            AntiSpoofManager.PlayerSpoofData data = manager.getPlayerData(player.getUniqueId().toString());
            if (data != null && data.isSpoofing()) {
                found = true;
                sender.sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY +
                    " - Brand: " + ChatColor.WHITE + (data.getBrand() != null ? data.getBrand() : "unknown"));
            }
        }

        if (!found) {
            sender.sendMessage(ChatColor.GREEN + "No players are currently detected as spoofing.");
        }
    }

    private void checkPlayer(CommandSender sender, String playerName) {
        AntiSpoofManager manager = AntiSpoofManager.getInstance();
        if (manager == null) {
            sender.sendMessage(ChatColor.RED + "AntiSpoof is not enabled!");
            return;
        }

        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        AntiSpoofManager.PlayerSpoofData data = manager.getPlayerData(target.getUniqueId().toString());
        if (data == null) {
            sender.sendMessage(ChatColor.YELLOW + "No data available for this player.");
            return;
        }

        if (data.isSpoofing()) {
            sender.sendMessage(ChatColor.RED + target.getName() + " has been flagged!");
        } else {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " does not appear to be spoofing.");
        }

        sender.sendMessage(ChatColor.GRAY + "Client brand: " + ChatColor.WHITE + (data.getBrand() != null ? data.getBrand() : "unknown"));
        sender.sendMessage(ChatColor.GRAY + "Channels: " + ChatColor.WHITE + data.getChannels().size());
    }

    private void toggleAntiSpoof(CommandSender sender) {
        AntiSpoofManager manager = AntiSpoofManager.getInstance();
        if (manager == null) {
            sender.sendMessage(ChatColor.RED + "AntiSpoof is not enabled!");
            return;
        }

        boolean newState = !manager.isEnabled();
        manager.setEnabled(newState);

        sender.sendMessage(ChatColor.GREEN + "AntiSpoof " + (newState ? "enabled" : "disabled") + "!");
    }

    private void toggleDebug(CommandSender sender) {
        AntiSpoofManager manager = AntiSpoofManager.getInstance();
        if (manager == null) {
            sender.sendMessage(ChatColor.RED + "AntiSpoof is not enabled!");
            return;
        }

        boolean newState = !manager.isDebug();
        manager.setDebug(newState);

        sender.sendMessage(ChatColor.GREEN + "AntiSpoof debug " + (newState ? "enabled" : "disabled") + "!");
    }
}
