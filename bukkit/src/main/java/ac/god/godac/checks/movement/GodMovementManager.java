package ac.god.godac.checks.movement;

import ac.god.godac.utils.GeyserUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GodMovementManager {

    private static GodMovementManager instance;
    private final Map<String, MovementData> playerData = new ConcurrentHashMap<>();
    private Plugin plugin;

    private boolean enabled = true;
    private double maxSpeed = 0.55;
    private double maxSprintSpeed = 0.72;
    private int alertVL = 3;
    private boolean exemptGeyser = true;

    public GodMovementManager(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        plugin.getLogger().info("GodAC Movement module initialized");
    }

    public static GodMovementManager getInstance() {
        return instance;
    }

    public void onPlayerMove(Player player) {
        if (!enabled) return;

        if (exemptGeyser && GeyserUtil.isBedrockPlayer(player)) {
            return;
        }

        String id = player.getUniqueId().toString();
        MovementData data = playerData.computeIfAbsent(id, k -> new MovementData());

        Location loc = player.getLocation();
        long now = System.currentTimeMillis();

        double deltaX = loc.getX() - data.lastX;
        double deltaY = loc.getY() - data.lastY;
        double deltaZ = loc.getZ() - data.lastZ;

        double horizontalSpeed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        data.addMovement(now, horizontalSpeed);

        data.lastX = loc.getX();
        data.lastY = loc.getY();
        data.lastZ = loc.getZ();

        checkSpeed(player, data, horizontalSpeed);
        checkFlight(player, data, deltaY);
        checkScaffold(player, data);
        checkTimer(player, data);
    }

    private void checkSpeed(Player player, MovementData data, double horizontalSpeed) {
        boolean isSprinting = player.isSprinting();
        boolean isOnGround = player.isOnGround();

        double threshold = maxSpeed;
        if (isSprinting && isOnGround) threshold = maxSprintSpeed;

        if (horizontalSpeed > threshold && !player.isInsideVehicle()) {
            flag(player, "Speed", String.format("%.3f > %.3f", horizontalSpeed, threshold), data);
        }
    }

    private void checkFlight(Player player, MovementData data, double deltaY) {
        boolean isOnGround = player.isOnGround();

        if (player.getVehicle() != null) return;

        if (!isOnGround) {
            data.ticksInAir++;
        } else {
            data.ticksInAir = 0;
        }

        if (!isOnGround && deltaY > 0.1 && data.ticksInAir > 5) {
            flag(player, "Flight", "Moving up in air", data);
        }

        if (!isOnGround && Math.abs(deltaY) < 0.001 && data.ticksInAir > 15) {
            flag(player, "Flight", "Hovering", data);
        }
    }

    private void checkScaffold(Player player, MovementData data) {
        boolean isOnGround = player.isOnGround();

        if (!isOnGround) {
            data.ticksInAir++;
        } else {
            data.ticksInAir = 0;
        }

        boolean hasBlockInHand = player.getInventory().getItemInMainHand().getType().isBlock();

        if (hasBlockInHand && data.ticksInAir > 2 && data.ticksInAir < 15) {
            Location loc = player.getLocation();
            boolean hasBlockBelow = false;
            for (int i = 1; i <= 4; i++) {
                Block block = loc.subtract(0, i, 0).getBlock();
                if (!block.getType().isAir()) {
                    hasBlockBelow = true;
                    break;
                }
            }
            if (!hasBlockBelow) {
                flag(player, "Scaffold", "Bridge building", data);
            }
        }
    }

    private void checkTimer(Player player, MovementData data) {
        List<Long> timestamps = data.getTimestamps();

        if (timestamps.size() < 10) return;

        long[] intervals = new long[timestamps.size() - 1];
        for (int i = 1; i < timestamps.size(); i++) {
            intervals[i - 1] = timestamps.get(i) - timestamps.get(i - 1);
        }

        double avgInterval = Arrays.stream(intervals).average().orElse(50);

        if (avgInterval < 15) {
            flag(player, "Timer", String.format("Avg tick: %.1fms", avgInterval), data);
        }
    }

    private void flag(Player player, String check, String details, MovementData data) {
        int vl = data.getVL(check) + 1;
        data.setVL(check, vl);

        if (vl >= alertVL) {
            String message = ChatColor.RED + "[GodAC] " + ChatColor.YELLOW + player.getName() +
                ChatColor.RED + " flagged for " + check + "! " + ChatColor.GRAY + details;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("godac.alerts") || p.isOp()) {
                    p.sendMessage(message);
                }
            }

            plugin.getLogger().warning("[GodAC] " + player.getName() + " " + check + ": " + details);
        }
    }

    public void onPlayerQuit(Player player) {
        playerData.remove(player.getUniqueId().toString());
    }

    public MovementData getPlayerData(String id) {
        return playerData.get(id);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void shutdown() { playerData.clear(); instance = null; }

    public static class MovementData {
        private double lastX, lastY, lastZ;
        private final List<Long> timestamps = new ArrayList<>();
        private final List<Double> horizontalSpeeds = new ArrayList<>();
        private final Map<String, Integer> vl = new ConcurrentHashMap<>();
        private int ticksInAir = 0;

        public void addMovement(long time, double hSpeed) {
            timestamps.add(time);
            horizontalSpeeds.add(hSpeed);

            long cutoff = time - 1000;
            timestamps.removeIf(t -> t < cutoff);

            if (horizontalSpeeds.size() > 20) horizontalSpeeds.remove(0);
            if (timestamps.size() > 20) timestamps.remove(0);
        }

        public List<Long> getTimestamps() { return new ArrayList<>(timestamps); }
        public int getVL(String check) { return vl.getOrDefault(check, 0); }
        public void setVL(String check, int value) { vl.put(check, value); }
    }
}
