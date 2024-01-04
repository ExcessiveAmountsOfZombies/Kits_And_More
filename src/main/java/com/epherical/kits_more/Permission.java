package com.epherical.kits_more;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Permission {
    private final ResourceLocation node;
    private final DefaultResolver defaultResolver;
    private PlatformResolver platformResolver;

    public Permission(ResourceLocation node, DefaultResolver resolver) {
        this.node = node;
        this.defaultResolver = resolver;
    }


    public ResourceLocation getNode() {
        return node;
    }

    public DefaultResolver getDefaultResolver() {
        return defaultResolver;
    }

    public void setPlatformResolver(PlatformResolver platformResolver) {
        this.platformResolver = platformResolver;
    }

    public PlatformResolver getPlatformResolver() {
        return platformResolver;
    }

    @FunctionalInterface
    public interface DefaultResolver {
        /**
         * why.
         */
        boolean resolve(CommandSourceStack stack, @Nullable ServerPlayer player);
    }


    /**
     * Example:
     * Forge has their Permission API, so we set the PlatformResolver to use the forge permission API,
     * the forge permission node will fall back to our {@link DefaultResolver} which will use the basic "hasPermission" system with numbers
     */
    public interface PlatformResolver {
        boolean resolve(CommandSourceStack stack, @Nullable ServerPlayer player);
    }

}
