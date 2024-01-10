package com.epherical.kits_more;

import com.epherical.kits_more.config.KitConfig;
import com.epherical.kits_more.data.UserData;
import com.epherical.kits_more.util.Kit;
import com.epherical.kits_more.util.User;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitsMod {

    private static final Logger LOGGER = LogUtils.getLogger();


    public static final Map<String, Kit> KITS = new HashMap<>();

    public static final List<Permission> PERMISSIONS = new ArrayList<>();

    public final Permission KIT_CREATION = registerPermission(new Permission(Constants.id("kit.create"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public final Permission KIT_DELETION = registerPermission(new Permission(Constants.id("kit.delete"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public final Permission KIT_USE = registerPermission(new Permission(Constants.id("kit.use"), (stack, player) -> {
        // todo; we won't always assume this
        return getDefaultPerms(stack, player, 0);
    }));

    public UserData data;


    private boolean getDefaultPerms(CommandSourceStack stack, ServerPlayer player, int level) {
        if (player != null) {
            return player.hasPermissions(level);
        } else {
            return stack.hasPermission(level);
        }
    }

    public Permission registerPermission(Permission permission) {
        PERMISSIONS.add(permission);
        return permission;
    }


    public void onServerStarting(MinecraftServer server) {
        this.data = new UserData(LevelResource.ROOT, server, "kits_and_more/players");
    }


    public void onPlayerJoin(ServerPlayer player) {
        int value = player.getStats().getValue((Stats.CUSTOM), Stats.PLAYER_KILLS);
        User user = data.getUser(player);
        if (value > 0 && KitConfig.giveKitsInExistingWorlds && !user.hasReceivedMainKit()) {
            provideKit(player, user);
        } else if (value <= 0 && !user.hasReceivedMainKit()) {
            provideKit(player, user);

        }

        data.savePlayer(user);
    }

    private static void provideKit(ServerPlayer player, User user) {
        Kit main = KITS.get("main");
        if (main != null) {
            main.giveKitToPlayer(player);
            user.setReceivedMainKit(true);
        } else {
            LOGGER.debug("Could not provide a kit to player {} {} as it does not exist", player.getUUID(), player.getScoreboardName());
        }
    }

    public void onPlayerQuit(ServerPlayer player) {
        data.savePlayer(data.getUser(player));
    }


}
