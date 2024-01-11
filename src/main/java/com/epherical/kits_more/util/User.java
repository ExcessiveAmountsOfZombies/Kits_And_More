package com.epherical.kits_more.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private final String name;
    private @Nullable ServerPlayer player;

    private Map<String, Instant> kitCoolDowns = new HashMap<>();

    private boolean receivedMainKit = false;


    public User(UUID uuid, String name) {
        this(uuid, name, null);
    }

    public User(UUID uuid, String name, @Nullable ServerPlayer player) {
        this.uuid = uuid;
        this.name = name;
        this.player = player;
    }


    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", uuid);
        tag.putString("name", name);
        tag.putBoolean("receivedMainKit", receivedMainKit);
        ListTag tags = new ListTag();
        for (Map.Entry<String, Instant> entry : kitCoolDowns.entrySet()) {
            CompoundTag comp = new CompoundTag();
            comp.putString("kitName", entry.getKey());
            comp.putLong("cooldown", entry.getValue().getEpochSecond());
        }
        tag.put("cooldowns", tags);
        return tag;
    }

    public static User load(CompoundTag tag, ServerPlayer player) {
        User user = new User(tag.getUUID("uuid"), tag.getString("name"), player);
        user.receivedMainKit = tag.getBoolean("receivedMainKit");
        Map<String, Instant> coolDowns = new HashMap<>();
        ListTag listTag = (ListTag) tag.get("cooldowns");
        if (listTag != null) {
            for (Tag t : listTag) {
                CompoundTag comp = (CompoundTag) t;
                coolDowns.put(comp.getString("kitName"), Instant.ofEpochSecond(comp.getLong("cooldown")));
            }
        }
        user.kitCoolDowns = coolDowns;
        return user;
    }

    public Instant getCoolDownForKit(Kit kit) {
        return kitCoolDowns.get(kit);
    }

    public void addCoolDownForKit(Kit kit) {
        if (kit.getCooldownMinutes() == -1) {
            kitCoolDowns.put(kit.getName(), Instant.MAX);
        } else {
            kitCoolDowns.put(kit.getName(), Instant.now().plusSeconds(60L * kit.getCooldownMinutes()));
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public @Nullable ServerPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public Map<String, Instant> getKitCoolDowns() {
        return kitCoolDowns;
    }

    public void setKitCoolDowns(Map<String, Instant> kitCoolDowns) {
        this.kitCoolDowns = kitCoolDowns;
    }

    public boolean hasReceivedMainKit() {
        return receivedMainKit;
    }

    public void setReceivedMainKit(boolean receivedMainKit) {
        this.receivedMainKit = receivedMainKit;
    }
}
