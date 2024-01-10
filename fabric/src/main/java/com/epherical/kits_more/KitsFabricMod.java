package com.epherical.kits_more;

import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.FabricPlatform;
import com.epherical.kits_more.commands.KitCommand;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class KitsFabricMod implements ModInitializer {



    @Override
    public void onInitialize() {
        CommonPlatform.create(new FabricPlatform());

        CommandRegistrationCallback.EVENT.register(KitCommand::register);
        for (Permission permission : KitsMod.PERMISSIONS) {
            permission.setPlatformResolver((stack, player) -> {
                if (stack != null) {
                    return Permissions.check(stack, permission.getNode().getNamespace() + "." + permission.getNode().getPath(), permission.getDefaultResolver().resolve(stack, player));
                } else {
                    return Permissions.check(player, permission.getNode().getNamespace() + "." + permission.getNode().getPath(), permission.getDefaultResolver().resolve(stack, player));
                }
            });
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            KitsMod.onPlayerJoin(handler.player);
        });
    }
}
