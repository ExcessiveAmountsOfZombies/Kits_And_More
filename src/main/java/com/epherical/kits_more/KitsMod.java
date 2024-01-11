package com.epherical.kits_more;

import com.epherical.kits_more.config.KitConfig;
import com.epherical.kits_more.data.KitData;
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
import java.util.List;

public class KitsMod {

    protected static final Logger LOGGER = LogUtils.getLogger();


    public static final List<Permission> PERMISSIONS = new ArrayList<>();

    public final Permission KIT_CREATION = registerPermission(new Permission(Constants.id("kit.create"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public final Permission KIT_DELETION = registerPermission(new Permission(Constants.id("kit.delete"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public final Permission KIT_USE = registerPermission(new Permission(Constants.id("kit.use"), (stack, player) -> {
        // todo; we won't always assume this
        return getDefaultPerms(stack, player, 0);
    }));

    public UserData userData;
    public KitData kitData = new KitData();


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
        this.userData = new UserData(LevelResource.ROOT, server, "kits_and_more/players");
        this.kitData.loadKitsFromFile();
    }


    public void onPlayerJoin(ServerPlayer player) {
        int value = player.getStats().getValue((Stats.CUSTOM), Stats.PLAYER_KILLS);
        User user = userData.getUser(player);
        if (value > 0 && KitConfig.giveKitsInExistingWorlds && !user.hasReceivedMainKit()) {
            provideKit(player, user, false);
        } else if (value <= 0 && !user.hasReceivedMainKit()) {
            // This happens on first login
            provideKit(player, user, true);
        }

        userData.savePlayer(user);
    }

    public void provideKit(ServerPlayer player, User user, boolean firstLogin) {
        Kit main = kitData.KITS.get("main");
        if (main != null) {
            main.giveKitToPlayer(player, firstLogin);
            user.setReceivedMainKit(true);
        } else {
            LOGGER.debug("Could not provide a kit to player {} {} as it does not exist", player.getUUID(), player.getScoreboardName());
        }
    }

    public void onPlayerQuit(ServerPlayer player) {
        userData.savePlayer(userData.getUser(player));
    }


}
