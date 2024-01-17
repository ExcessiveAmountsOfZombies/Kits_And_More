package com.epherical.kits_more.config;

import com.epherical.epherolib.config.LanguageConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class Translations extends LanguageConfig {

    public Translations(String folderName) {
        super(folderName);
    }


    public String getText(ServerPlayer player, String key, String fallback) {
        return Translations.getLanguage(player).getOrDefault(key, fallback);
    }

    public MutableComponent createTranslation(ServerPlayer player, String key, String fallback, Object... values) {
        return Component.translatable(Translations.getLanguage(player).getOrDefault(key, fallback), values);
    }
}
