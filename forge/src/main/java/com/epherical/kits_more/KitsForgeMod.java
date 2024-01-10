package com.epherical.kits_more;

import com.epherical.kits_more.client.AModClient;
import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.ForgePlatform;
import com.epherical.kits_more.commands.KitCommand;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@Mod(Constants.MOD_ID)
public class KitsForgeMod {

    private static KitsForgeMod mod;


    public KitsForgeMod() {
        mod = this;
        CommonPlatform.create(new ForgePlatform());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonInit);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientInit(FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AModClient::initClient);
        MinecraftForge.EVENT_BUS.register(new AModClient());
    }

    private void commonInit(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    private void registerCommands(RegisterCommandsEvent event) {
        KitCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    private void registerPermission(PermissionGatherEvent.Nodes gatherEvent) {
        for (Permission permission : KitsMod.PERMISSIONS) {
            PermissionNode<Boolean> node = new PermissionNode<>(permission.getNode(), PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
                return permission.getDefaultResolver().resolve(null, player);
            });
            permission.setPlatformResolver((stack, player) -> {
                try {
                    if (stack != null) {
                       player = stack.getPlayerOrException();
                    }
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
                return PermissionAPI.getPermission(player, node);
            });
            gatherEvent.addNodes(node);
        }
    }
}
