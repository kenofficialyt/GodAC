package ac.grim.grimac.manager;

import ac.grim.grimac.player.GrimPlayer;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GodKickManager {
    private static GodKickManager instance;

    @Getter
    private final Map<UUID, Integer> kickCount = new HashMap<>();

    public static GodKickManager getInstance() {
        if (instance == null) {
            instance = new GodKickManager();
        }
        return instance;
    }

    public void onPlayerKick(GrimPlayer player) {
        UUID uuid = player.platformPlayer.getUniqueId();
        int count = kickCount.getOrDefault(uuid, 0) + 1;
        kickCount.put(uuid, count);

        if (count >= 3) {
            player.platformPlayer.kickPlayer("§cYou have been banned by GodAC");
            kickCount.remove(uuid);
        } else {
            player.platformPlayer.kickPlayer("§cYou have been flagged by GodAC (§e" + count + "/3§c)");
        }
    }

    public int getKickCount(UUID uuid) {
        return kickCount.getOrDefault(uuid, 0);
    }

    public void removePlayer(UUID uuid) {
        kickCount.remove(uuid);
    }
}
