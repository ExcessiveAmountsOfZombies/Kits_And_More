package com.epherical.kits_more.data;

import com.epherical.epherolib.data.WorldBasedStorage;
import com.epherical.kits_more.exception.EconomyException;
import com.epherical.kits_more.util.User;
import com.epherical.octoecon.api.user.UniqueUser;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserData extends WorldBasedStorage {

    private final Map<UUID, UniqueUser> LOADED_USERS = new HashMap<>();

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
            writeTagToFile(user.save(), resolve(user.getUserID()));
        } catch (IOException e) {
            LOGGER.warn("Could not save user {}", user.getName(), e);
        }
    }

    public User load(ServerPlayer player) {
        Path path = resolve(player.getUUID());
        Tag tag;
        try {
            tag = readTagFromFile(path);
            User load = User.load((CompoundTag) tag, player);
            LOADED_USERS.put(player.getUUID(), load);
            return load;
        } catch (IOException ignored) {
            LOGGER.debug("Creating new User for {}, {} as they do not exist currently.", player.getScoreboardName(), player.getUUID());
        }
        return new User(player.getUUID(), player.getScoreboardName(), player);
    }

    public void savePlayers() {
        synchronized (LOADED_USERS) {
            for (UniqueUser value : LOADED_USERS.values()) {
                User user = (User) value;
                savePlayer(user);
            }
        }
    }

    public User getUser(ServerPlayer player) {
        if (LOADED_USERS.containsKey(player.getUUID())) {
            return (User) LOADED_USERS.get(player.getUUID());
        } else {
            return load(player);
        }
    }

    public User getUser(UUID uuid) {
        if (LOADED_USERS.containsKey(uuid)) {
            return (User) LOADED_USERS.get(uuid);
        } else {
            return null;
        }
    }

    public User userJoin(ServerPlayer player) {
        User user = getUser(player);
        user.setPlayer(player);
        return user;
    }

    public User userQuit(ServerPlayer player) {
        User user = getUser(player);
        savePlayer(user);
        user.setPlayer(null);
        return user;
    }

    public boolean userExists(UUID uuid) {
        return LOADED_USERS.containsKey(uuid);
    }

    public Map<UUID, UniqueUser> getUsers() {
        return LOADED_USERS;
    }
}
