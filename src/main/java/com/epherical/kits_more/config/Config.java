package com.epherical.kits_more.config;

import com.epherical.epherolib.config.CommonConfig;
import com.epherical.epherolib.libs.org.spongepowered.configurate.CommentedConfigurationNode;
import com.epherical.epherolib.libs.org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.SerializationException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class Config extends CommonConfig {

    private static final Logger LOGGER = LogUtils.getLogger();


    public boolean giveKitsInExistingWorlds = false;

    public boolean useSaveThread = false;
    public double moneyGivenOnFirstLogin = 0D;


    // Enable/Disable for modules
    public boolean enableEcon = true;
    public boolean enableBank = true;

    public Config(AbstractConfigurationLoader.Builder<?, ?> loaderBuilder, String configName) {
        super(loaderBuilder, configName);
        configVersion = 3;
    }

    @Override
    public void parseConfig(CommentedConfigurationNode node) {
       configVersion = node.node("version").getInt(configVersion);
       giveKitsInExistingWorlds = node.node("giveKitsInExistingWorlds").getBoolean(giveKitsInExistingWorlds);
       useSaveThread = node.node("useSaveThread").getBoolean(useSaveThread);
       moneyGivenOnFirstLogin = node.node("moneyGivenOnFirstLogin").getDouble(moneyGivenOnFirstLogin);

       enableEcon = node.node("modules").node("economy").getBoolean(enableEcon);
       enableBank = node.node("modules").node("banks").getBoolean(enableBank);


    }

    @Override
    public CommentedConfigurationNode generateConfig(CommentedConfigurationNode node) {
        try {
            node.node("version").set(configVersion).comment("Config Version do not edit");
            node.node("giveKitsInExistingWorlds").set(giveKitsInExistingWorlds).comment("If you have an existing world/server, and you would like players to receive any of the" +
                    " first login benefits, you can set this to true. For kits, full inventories will cause items to drop at their feet.");
            node.node("useSaveThread").set(useSaveThread)
                    .comment("This will create a separate thread for when the user is being saved to a file");
            node.node("moneyGivenOnFirstLogin").set(moneyGivenOnFirstLogin)
                    .comment("When the player has their player file first created, they will be given a set amount of money.");


            node.node("modules").node("economy")
                    .set(enableEcon).comment("Default true, adds an economy system to the server, set to false to disable the functionality");
            node.node("modules").node("banks")
                    .set(enableBank).comment("Default true, adds a bank system to the server, set to false to disable the functionality");

        } catch (SerializationException e) {
            LOGGER.warn("Could not serialize value to config!", e);
        }
        return node;
    }
}
