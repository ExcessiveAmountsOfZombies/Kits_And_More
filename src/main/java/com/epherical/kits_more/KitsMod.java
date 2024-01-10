package com.epherical.kits_more;

import com.epherical.kits_more.config.KitConfig;
import com.epherical.kits_more.util.Kit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitsMod {


    public static final Map<String, Kit> KITS = new HashMap<>();

    public static final List<Permission> PERMISSIONS = new ArrayList<>();

    public static final Permission KIT_CREATION = registerPermission(new Permission(Constants.id("kit.create"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public static final Permission KIT_DELETION = registerPermission(new Permission(Constants.id("kit.delete"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public static final Permission KIT_USE = registerPermission(new Permission(Constants.id("kit.use"), (stack, player) -> {
        // todo; we won't always assume this
        return getDefaultPerms(stack, player, 0);
    }));


    private static boolean getDefaultPerms(CommandSourceStack stack, ServerPlayer player, int level) {
        if (player != null) {
            return player.hasPermissions(level);
        } else {
            return stack.hasPermission(level);
        }
    }

    public static Permission registerPermission(Permission permission) {
        PERMISSIONS.add(permission);
        return permission;
    }


    public static void onPlayerJoin(ServerPlayer player) {
        int value = player.getStats().getValue((Stats.CUSTOM), Stats.PLAYER_KILLS);
        System.out.println(value);
        if (value > 0 && KitConfig.giveKitsInExistingWorlds) {

            // player has plaayed before. decide if we want to provide the kit or not.
        }
    }


}
