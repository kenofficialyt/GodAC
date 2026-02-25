package ac.god.godac.antispoof;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpoofManager {

    private static AntiSpoofManager instance;
    private final Map<String, PlayerSpoofData> playerData = new ConcurrentHashMap<>();
    private Plugin plugin;

    private boolean enabled = true;
    private boolean debug = false;
    private boolean checkVanillaSpoof = true;
    private boolean checkBrandSpoof = true;
    private boolean checkGeyserSpoof = true;

    public AntiSpoofManager(Plugin plugin) {
        instance = this;
        this.plugin = plugin;

        if (enabled) {
            plugin.getLogger().info("GodAC AntiSpoof module initialized");
        }
    }

    public static AntiSpoofManager getInstance() {
        return instance;
    }

    public void onPlayerBrand(String uuid, String playerName, String brand) {
        if (brand == null || brand.isEmpty()) {
            return;
        }

        if (debug) {
            plugin.getLogger().info("[AntiSpoof] Brand for " + playerName + ": " + brand);
        }

        PlayerSpoofData data = playerData.computeIfAbsent(uuid, k -> new PlayerSpoofData());
        data.setBrand(brand);
        data.setPlayerName(playerName);
        data.setUuid(uuid);

        boolean spoofing = isSpoofing(brand, data.getChannels());
        data.setSpoofing(spoofing);

        if (spoofing && enabled) {
            alertStaff(playerName, brand, data.getChannels());
        }
    }

    public void addChannel(String uuid, String channel) {
        PlayerSpoofData data = playerData.get(uuid);
        if (data == null) {
            data = new PlayerSpoofData();
            data.setUuid(uuid);
            playerData.put(uuid, data);
        }

        data.addChannel(channel);

        String brand = data.getBrand();
        if (brand != null) {
            boolean spoofing = isSpoofing(brand, data.getChannels());
            data.setSpoofing(spoofing);

            if (spoofing && enabled) {
                alertStaff(data.getPlayerName(), brand, data.getChannels());
            }
        }
    }

    private boolean isSpoofing(String brand, java.util.Set<String> channels) {
        boolean hasChannels = channels != null && !channels.isEmpty();
        boolean claimsVanilla = "vanilla".equalsIgnoreCase(brand);

        if (checkVanillaSpoof && claimsVanilla && hasChannels) {
            return true;
        }

        if (checkBrandSpoof && (brand == null || brand.isEmpty())) {
            return true;
        }

        if (checkGeyserSpoof && brand != null) {
            boolean claimsGeyser = brand.toLowerCase().contains("geyser");
            boolean isBedrock = isBedrockPlayer(brand);

            if (claimsGeyser && !isBedrock) {
                return true;
            }
        }

        return false;
    }

    private boolean isBedrockPlayer(String brand) {
        try {
            Object floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
            if (floodgate != null) {
                Object instance = floodgate.getClass().getMethod("getInstance").invoke(null);
                return (Boolean) instance.getClass().getMethod("isFloodgatePlayer", java.util.UUID.class)
                    .invoke(instance, brand);
            }
        } catch (Exception ignored) {}

        return false;
    }

    private void alertStaff(String playerName, String brand, java.util.Set<String> channels) {
        String message = ChatColor.RED + "[AntiSpoof] " + ChatColor.YELLOW + playerName +
            ChatColor.RED + " flagged! " + ChatColor.GRAY + "Brand: " + ChatColor.WHITE + brand;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("godac.alerts") || p.isOp()) {
                p.sendMessage(message);
            }
        }

        if (debug) {
            plugin.getLogger().warning("[AntiSpoof] " + playerName + " spoofing detected! Brand: " + brand);
        }
    }

    public PlayerSpoofData getPlayerData(String uuid) {
        return playerData.get(uuid);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void shutdown() {
        playerData.clear();
        instance = null;
    }

    public static class PlayerSpoofData {
        private String uuid;
        private String playerName;
        private String brand;
        private java.util.Set<String> channels = ConcurrentHashMap.newKeySet();
        private boolean spoofing;

        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public java.util.Set<String> getChannels() { return channels; }
        public void addChannel(String channel) { this.channels.add(channel); }
        public boolean isSpoofing() { return spoofing; }
        public void setSpoofing(boolean spoofing) { this.spoofing = spoofing; }
    }
}
