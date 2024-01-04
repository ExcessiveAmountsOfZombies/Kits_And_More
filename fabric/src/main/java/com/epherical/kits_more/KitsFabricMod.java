package com.epherical.kits_more;

import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.FabricPlatform;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class KitsFabricMod implements ModInitializer {



    @Override
    public void onInitialize() {
        CommonPlatform.create(new FabricPlatform());

        CommandRegistrationCallback.EVENT.register(KitsMod::register);
        for (Permission permission : KitsMod.PERMISSIONS) {
            permission.setPlatformResolver((stack, player) -> {
                if (stack != null) {
                    return Permissions.check(stack, permission.getNode().getNamespace() + "." + permission.getNode().getPath());
                } else {
                    return Permissions.check(player, permission.getNode().getNamespace() + "." + permission.getNode().getPath());
                }
            });
        }
    }
}
