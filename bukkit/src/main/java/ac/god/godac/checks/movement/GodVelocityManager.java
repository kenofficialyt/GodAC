package ac.god.godac.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GodVelocityManager implements Listener {

    private static GodVelocityManager instance;
    private final Map<String, VelocityData> playerData = new ConcurrentHashMap<>();
    private Plugin plugin;

    private boolean enabled = true;
    private int alertVL = 3;

    public GodVelocityManager(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("GodAC Velocity module initialized");
    }

    public static GodVelocityManager getInstance() {
        return instance;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        String id = player.getUniqueId().toString();

        VelocityData data = playerData.get(id);
        if (data == null) return;

        if (data.isWasHit() && data.getTicksSinceHit() < 5) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (from != null && to != null) {
                double dx = Math.abs(to.getX() - from.getX());
                double dz = Math.abs(to.getZ() - from.getZ());
                double movement = Math.sqrt(dx * dx + dz * dz);

                double expectedVelocity = data.getExpectedKnockback();

                if (movement < minVelocityThreshold && data.getTicksSinceHit() < 3) {
                    flag(player, "NoVelocity", String.format("Low movement: %.3f", movement), data);
                }
            }

            data.incrementTicksSinceHit();
        }
    }

    private double minVelocityThreshold = 0.1;

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!enabled) return;

        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        String id = attacker.getUniqueId().toString();
        VelocityData data = playerData.computeIfAbsent(id, k -> new VelocityData());

        double baseKnockback = 0.4;
        double enchantment = 0.0;

        if (attacker.getInventory().getItemInMainHand() != null &&
            attacker.getInventory().getItemInMainHand().getEnchantments() != null) {
            enchantment = attacker.getInventory().getItemInMainHand().getEnchantments()
                .getOrDefault(org.bukkit.enchantments.Enchantment.KNOCKBACK, 0) * 0.1;
        }

        double expectedKnockback = baseKnockback + enchantment;

        data.setExpectedKnockback(expectedKnockback);
        data.setWasHit(true);
        data.setTicksSinceHit(0);
        data.setHitCount(data.getHitCount() + 1);

        checkAimbot(attacker, victim, data);
    }

    private void checkAimbot(Player attacker, Player victim, VelocityData data) {
        Location attackerLoc = attacker.getLocation();
        Location victimLoc = victim.getLocation();

        double dx = victimLoc.getX() - attackerLoc.getX();
        double dz = victimLoc.getZ() - attackerLoc.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 3.0) return;

        double yawDiff = Math.abs(attackerLoc.getYaw() - victimLoc.getYaw());
        if (yawDiff > 180) yawDiff = 360 - yawDiff;

        if (yawDiff < 5 && distance < 2.0) {
            data.incrementPerfectAimbotHits();
        }

        if (data.getPerfectAimbotHits() > 5 && data.getHitCount() > 10) {
            double ratio = (double) data.getPerfectAimbotHits() / data.getHitCount();
            if (ratio > 0.5) {
                flag(attacker, "Aimbot", String.format("Perfect aim ratio: %.1f%%", ratio * 100), data);
            }
        }
    }

    private void flag(Player player, String check, String details, VelocityData data) {
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerData.remove(event.getPlayer().getUniqueId().toString());
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static class VelocityData {
        private double expectedKnockback = 0.4;
        private boolean wasHit = false;
        private int ticksSinceHit = 0;
        private int hitCount = 0;
        private int perfectAimbotHits = 0;
        private final Map<String, Integer> vl = new ConcurrentHashMap<>();

        public double getExpectedKnockback() { return expectedKnockback; }
        public void setExpectedKnockback(double v) { this.expectedKnockback = v; }
        public boolean isWasHit() { return wasHit; }
        public void setWasHit(boolean v) { this.wasHit = v; }
        public int getTicksSinceHit() { return ticksSinceHit; }
        public void setTicksSinceHit(int v) { this.ticksSinceHit = v; }
        public void incrementTicksSinceHit() { this.ticksSinceHit++; }
        public int getHitCount() { return hitCount; }
        public void setHitCount(int v) { this.hitCount = v; }
        public int getPerfectAimbotHits() { return perfectAimbotHits; }
        public void incrementPerfectAimbotHits() { this.perfectAimbotHits++; }
        public int getVL(String check) { return vl.getOrDefault(check, 0); }
        public void setVL(String check, int value) { vl.put(check, value); }
    }
}
