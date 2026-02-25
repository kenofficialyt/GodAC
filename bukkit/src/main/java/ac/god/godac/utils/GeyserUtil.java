package ac.god.godac.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

public class GeyserUtil {

    private static Object floodgateApi = null;
    private static boolean floodgateLoaded = false;
    private static boolean checkPerformed = false;

    public static void init() {
        if (checkPerformed) return;
        checkPerformed = true;

        try {
            Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
            if (floodgate != null) {
                Method getInstance = floodgate.getClass().getMethod("getInstance");
                floodgateApi = getInstance.invoke(null);
                floodgateLoaded = true;
                Bukkit.getLogger().info("[GodAC] Floodgate API loaded successfully!");
            }
        } catch (Exception e) {
            floodgateLoaded = false;
        }
    }

    public static boolean isBedrockPlayer(Player player) {
        init();

        if (floodgateApi != null) {
            try {
                Method isFloodgatePlayer = floodgateApi.getClass().getMethod("isFloodgatePlayer", UUID.class);
                Boolean result = (Boolean) isFloodgatePlayer.invoke(floodgateApi, player.getUniqueId());
                if (result != null && result) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        if (floodgateApi != null) {
            try {
                Method isFloodgatePlayer = floodgateApi.getClass().getMethod("isFloodgatePlayer", String.class);
                Boolean result = (Boolean) isFloodgatePlayer.invoke(floodgateApi, player.getName());
                if (result != null && result) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        String name = player.getName();
        if (name.startsWith(".") || name.contains(":")) {
            return true;
        }

        return false;
    }

    public static boolean isFloodgateLoaded() {
        init();
        return floodgateLoaded;
    }

    public static String getPlayerType(Player player) {
        if (isBedrockPlayer(player)) {
            return "bedrock";
        }
        return "java";
    }
}
