package com.epherical.kits_more.config;

import com.epherical.epherolib.config.CommonConfig;
import com.epherical.epherolib.libs.org.spongepowered.configurate.CommentedConfigurationNode;
import com.epherical.epherolib.libs.org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.SerializationException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class KitConfig extends CommonConfig {

    private static final Logger LOGGER = LogUtils.getLogger();


    public boolean giveKitsInExistingWorlds = false;

    public KitConfig(AbstractConfigurationLoader.Builder<?, ?> loaderBuilder, String configName) {
        super(loaderBuilder, configName);
    }

    @Override
    public void parseConfig(CommentedConfigurationNode node) {
       configVersion = node.node("version").getInt(configVersion);
       giveKitsInExistingWorlds = node.node("giveKitsInExistingWorlds").getBoolean(giveKitsInExistingWorlds);


    }

    @Override
    public CommentedConfigurationNode generateConfig(CommentedConfigurationNode node) {
        try {
            node.node("version").set(configVersion).comment("Config Version do not edit");
            node.node("giveKitsInExistingWorlds").set(giveKitsInExistingWorlds).comment("If you have an existing world/server, and you would like players to receive a 'main' kit when they log in," +
                    "you can set this to true and they will receive the items, dropped at their feet if they have no room in their inventory.");

        } catch (SerializationException e) {
            LOGGER.warn("Could not serialize value to config!", e);
        }
        return node;
    }
}
