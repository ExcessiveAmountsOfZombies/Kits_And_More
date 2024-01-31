package com.epherical.kits_more;

import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.ForgePlatform;
import com.epherical.kits_more.commands.BankCommands;
import com.epherical.kits_more.commands.EconomyCommands;
import com.epherical.kits_more.commands.KitCommand;
import com.epherical.octoecon.api.event.EconomyChangeEvent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
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

import java.io.IOException;
import java.nio.file.Files;

@Mod(Constants.MOD_ID)
public class KitsForgeMod extends KitsMod {

    private static KitsForgeMod mod;


    public KitsForgeMod() {
        mod = this;
        CommonPlatform.create(new ForgePlatform());
        init();
        try {
            Files.createDirectories(CommonPlatform.platform.getRootConfigPath("kits_and_more"));
        } catch (IOException e) {
            LOGGER.warn("could not create config folder for kits_and_more mod.", e);
        }

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonInit);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientInit(FMLClientSetupEvent event) {
        /*DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AModClient::initClient);
        MinecraftForge.EVENT_BUS.register(new AModClient());*/
    }

    private void commonInit(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        KitCommand.register(this, event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        if (config.enableEcon)
            EconomyCommands.register(this, event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        if (config.enableBank)
            BankCommands.register(this, event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }


    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        onPlayerJoin((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        onPlayerQuit((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        onServerStarting(event.getServer());
        MinecraftForge.EVENT_BUS.post(new EconomyChangeEvent(provider));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        onServerStopping(event.getServer());
    }

    @SubscribeEvent
    public void registerPermission(PermissionGatherEvent.Nodes gatherEvent) {
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
