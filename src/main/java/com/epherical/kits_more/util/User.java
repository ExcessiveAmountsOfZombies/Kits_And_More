package com.epherical.kits_more.util;

import com.epherical.kits_more.util.econ.EconomyUser;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.user.UniqueUser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User extends EconomyUser implements UniqueUser {

    private final UUID uuid;
    private @Nullable ServerPlayer player;

    private Map<String, Instant> kitCoolDowns = new HashMap<>();

    private boolean receivedFirstLoginBenefits = false;


    public User(UUID uuid, String name) {
        this(uuid, name, null);
    }

    public User(UUID uuid, String name, @Nullable ServerPlayer player) {
        super(name);
        this.uuid = uuid;
        this.player = player;
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        receivedFirstLoginBenefits = tag.getBoolean("receivedFirstLoginBenefits");
        Map<String, Instant> coolDowns = new HashMap<>();
        ListTag listTag = (ListTag) tag.get("cooldowns");
        if (listTag != null) {
            for (Tag t : listTag) {
                CompoundTag comp = (CompoundTag) t;
                coolDowns.put(comp.getString("kitName"), Instant.ofEpochSecond(comp.getLong("cooldown")));
            }
        }
        kitCoolDowns = coolDowns;
    }


    public CompoundTag save() {
        CompoundTag tag = super.save();
        tag.putUUID("uuid", uuid);
        tag.putBoolean("receivedFirstLoginBenefits", receivedFirstLoginBenefits);
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
        user.load(tag);

        return user;
    }

    public Instant getCoolDownForKit(Kit kit) {
        return kitCoolDowns.get(kit.getName());
    }

    public void addCoolDownForKit(Kit kit) {
        if (kit.getCooldownMinutes() == -1) {
            kitCoolDowns.put(kit.getName(), Instant.MAX);
        } else {
            kitCoolDowns.put(kit.getName(), Instant.now().plusSeconds(60L * kit.getCooldownMinutes()));
        }
    }

    public String getName() {
        return getIdentity();
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

    public boolean hasReceivedFirstLoginBenefits() {
        return receivedFirstLoginBenefits;
    }

    public void setReceivedFirstLoginBenefits(boolean receivedFirstLoginBenefits) {
        this.receivedFirstLoginBenefits = receivedFirstLoginBenefits;
    }
    @Override
    public UUID getUserID() {
        return uuid;
    }
}
