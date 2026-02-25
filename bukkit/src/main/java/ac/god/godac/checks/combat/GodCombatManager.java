package ac.god.godac.checks.combat;

import ac.god.godac.utils.GeyserUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GodCombatManager {

    private static GodCombatManager instance;
    private final Map<String, CombatData> combatData = new ConcurrentHashMap<>();
    private Plugin plugin;

    private boolean enabled = true;
    private double reachThreshold = 3.1;
    private double maxCPS = 25;
    private boolean checkVelocity = true;
    private boolean checkKillAura = true;
    private boolean checkAutoClicker = true;
    private int alertVL = 10;
    private boolean exemptGeyser = true;

    public GodCombatManager(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
    }

    public static GodCombatManager getInstance() {
        return instance;
    }

    public void onAttack(Player attacker, Player target, PacketReceiveEvent event) {
        if (!enabled) return;

        if (exemptGeyser && (GeyserUtil.isBedrockPlayer(attacker) || GeyserUtil.isBedrockPlayer(target))) {
            return;
        }

        String attackerId = attacker.getUniqueId().toString();
        CombatData data = combatData.computeIfAbsent(attackerId, k -> new CombatData());

        long now = System.currentTimeMillis();
        data.addAttack(now);

        double distance = getDistance(attacker, target);
        data.addReach(distance);

        if (distance > reachThreshold) {
            flag(attacker, "Reach", String.format("%.2f blocks (max: %.2f)", distance, reachThreshold), data);
        }

        int cps = data.getCPS();
        if (cps > maxCPS) {
            flag(attacker, "CPS", "CPS: " + cps + " (max: " + maxCPS + ")", data);
        }

        checkAutoClickerPattern(attacker, data);
        checkVelocity(attacker, target, data);
    }

    private double getDistance(Player attacker, Player target) {
        Vector attPos = attacker.getLocation().toVector();
        Vector tarPos = target.getLocation().toVector();

        attPos.setY(attacker.getEyeHeight());
        tarPos.setY(target.getEyeHeight());

        return attPos.distance(tarPos);
    }

    private void checkAutoClickerPattern(Player player, CombatData data) {
        if (!checkAutoClicker) return;

        List<Long> attacks = data.getRecentAttacks();
        if (attacks.size() < 10) return;

        double stdDev = calculateStdDev(attacks);

        if (stdDev < 5 && data.getCPS() > 15) {
            flag(player, "AutoClicker", String.format("Consistent CPS: %.2f (stdDev: %.2f)", data.getCPS(), stdDev), data);
        }

        int[] pattern = detectPattern(attacks);
        if (pattern[0] > 0) {
            flag(player, "AutoClicker", "Pattern detected: " + pattern[0] + " repeats", data);
        }
    }

    private int[] detectPattern(List<Long> attacks) {
        if (attacks.size() < 12) return new int[]{0};

        int[] intervals = new int[attacks.size() - 1];
        for (int i = 1; i < attacks.size(); i++) {
            intervals[i - 1] = (int) (attacks.get(i) - attacks.get(i - 1));
        }

        int repeatCount = 0;
        for (int i = 0; i < intervals.length - 2; i++) {
            if (Math.abs(intervals[i] - intervals[i + 1]) < 5 &&
                Math.abs(intervals[i + 1] - intervals[i + 2]) < 5) {
                repeatCount++;
            }
        }

        return new int[]{repeatCount};
    }

    private double calculateStdDev(List<Long> times) {
        if (times.size() < 2) return 0;

        double[] intervals = new double[times.size() - 1];
        for (int i = 1; i < times.size(); i++) {
            intervals[i - 1] = times.get(i) - times.get(i - 1);
        }

        double mean = Arrays.stream(intervals).average().orElse(0);
        double variance = Arrays.stream(intervals).map(x -> Math.pow(x - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    private void checkVelocity(Player attacker, Player target, CombatData data) {
        if (!checkVelocity) return;

        double lastVelocity = data.getLastVelocity();
        if (lastVelocity > 0) {
            double movement = data.getMovementSinceHit();
            double expected = lastVelocity * 0.5;

            if (movement < expected * 0.5 && data.getTicksSinceHit() < 5) {
                flag(attacker, "Velocity", String.format("No movement: %.2f (expected: %.2f)", movement, expected), data);
            }
        }
    }

    private void flag(Player player, String check, String details, CombatData data) {
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

    public void onPlayerMove(Player player) {
        String id = player.getUniqueId().toString();
        CombatData data = combatData.get(id);
        if (data != null) {
            data.incrementMovement();
        }
    }

    public void onVelocity(Player player, double velocity) {
        String id = player.getUniqueId().toString();
        CombatData data = combatData.get(id);
        if (data != null) {
            data.setLastVelocity(velocity);
            data.resetMovement();
        }
    }

    public void onHit(Player player) {
        String id = player.getUniqueId().toString();
        CombatData data = combatData.get(id);
        if (data != null) {
            data.resetMovement();
            data.incrementTicksSinceHit();
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public double getReachThreshold() { return reachThreshold; }
    public void setReachThreshold(double threshold) { this.reachThreshold = threshold; }
    public double getMaxCPS() { return maxCPS; }
    public void setMaxCPS(double maxCPS) { this.maxCPS = maxCPS; }
    public void shutdown() { combatData.clear(); instance = null; }

    public static class CombatData {
        private final List<Long> attackTimes = new ArrayList<>();
        private final List<Double> reachValues = new ArrayList<>();
        private final Map<String, Integer> vl = new ConcurrentHashMap<>();
        private double lastVelocity = 0;
        private double movementSinceHit = 0;
        private int ticksSinceHit = 0;

        public void addAttack(long time) {
            long cutoff = System.currentTimeMillis() - 1000;
            attackTimes.removeIf(t -> t < cutoff);
            attackTimes.add(time);
        }

        public void addReach(double reach) {
            reachValues.add(reach);
            if (reachValues.size() > 20) reachValues.remove(0);
        }

        public int getCPS() {
            long cutoff = System.currentTimeMillis() - 1000;
            return (int) attackTimes.stream().filter(t -> t >= cutoff).count();
        }

        public List<Long> getRecentAttacks() {
            return new ArrayList<>(attackTimes);
        }

        public int getVL(String check) { return vl.getOrDefault(check, 0); }
        public void setVL(String check, int value) { vl.put(check, value); }
        public double getLastVelocity() { return lastVelocity; }
        public void setLastVelocity(double v) { this.lastVelocity = v; }
        public double getMovementSinceHit() { return movementSinceHit; }
        public void resetMovement() { this.movementSinceHit = 0; this.ticksSinceHit = 0; }
        public void incrementMovement() { this.movementSinceHit += 0.05; }
        public int getTicksSinceHit() { return ticksSinceHit; }
        public void incrementTicksSinceHit() { this.ticksSinceHit++; }
    }
}
