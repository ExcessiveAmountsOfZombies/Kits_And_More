package com.epherical.kits_more.data;

import com.epherical.epherolib.data.WorldBasedStorage;
import com.epherical.kits_more.util.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class UserData extends WorldBasedStorage {

    private static final Logger LOGGER = LogUtils.getLogger();


    public UserData(LevelResource resource, MinecraftServer server, String path) {
        super(resource, server, path);
    }

    @Override
    protected Gson buildGson(GsonBuilder gsonBuilder) {
        return gsonBuilder.create();
    }

    public Path resolve(UUID uuid) {
        return basePath.resolve(uuid.toString() + ".json");
    }

    public void savePlayer(User user) {
        try {
            writeTagToFile(user.save(), resolve(user.getUuid()));
        } catch (IOException e) {
            LOGGER.warn("Could not save user {}", user.getName(), e);
        }
    }

    public User load(ServerPlayer player) {
        Path path = resolve(player.getUUID());
        Tag tag;
        try {
            tag = readTagFromFile(path);
            return User.load((CompoundTag) tag, player);
        } catch (IOException ignored) {
            LOGGER.debug("Creating new User for {}, {} as they do not exist currently.", player.getScoreboardName(), player.getUUID());
        }
        return new User(player.getUUID(), player.getScoreboardName(), player);

    }
}
