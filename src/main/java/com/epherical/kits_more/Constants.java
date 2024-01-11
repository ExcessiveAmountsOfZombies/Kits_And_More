package com.epherical.kits_more;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Constants {
    public static final String MOD_ID = "kits_and_more";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }


    public static Component sendFailureMessage(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }

    public static Component sendFailureMessage(String text, Object... objs) {
        return Component.translatable(text, objs).withStyle(ChatFormatting.RED);
    }

    public static Component sendSuccessMessage(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GREEN);
    }

    public static Component sendSuccessMessage(String text, Object... objs) {
        return Component.translatable(text, objs).withStyle(ChatFormatting.GREEN);
    }
}
