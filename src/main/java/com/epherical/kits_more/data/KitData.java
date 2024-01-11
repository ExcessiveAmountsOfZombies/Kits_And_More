package com.epherical.kits_more.data;

import com.epherical.epherolib.CommonPlatform;
import com.epherical.kits_more.util.Kit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class KitData {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final Map<String, Kit> KITS = new HashMap<>();


    public void loadKitsFromFile() {
        try (JsonReader reader = new JsonReader(new FileReader(getPathForConfig().toFile()))) {
            JsonElement kits = GSON.fromJson(reader, JsonElement.class);
            CompoundTag tag = (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, kits);
            for (String key : tag.getAllKeys()) {
                CompoundTag reg = tag.getCompound(key);
                KITS.put(key, new Kit(reg.getString("kitName"), reg.getInt("cooldownMinutes"), reg));
            }

        } catch (IOException e) {
            LOGGER.warn("Could not find the kit file to be read! missing {}", "kits_and_more/kits.json from the config folder.");
        }

    }

    public Kit deleteKitAndSave(String kitName) {
        Kit remove = KITS.remove(kitName);
        writeKitsToFile();
        return remove;
    }

    public void saveKitsToFile(Kit kit) {
        KITS.put(kit.getName(), kit);
        writeKitsToFile();
    }

    public void saveKitsToFile() {
        writeKitsToFile();
    }


    private void writeKitsToFile() {
        CompoundTag allKits = new CompoundTag();

        for (Kit value : KITS.values()) {
            allKits.put(value.getName(), value.getItems());
        }
        try (FileWriter writer = new FileWriter(getPathForConfig().toFile())) {
            JsonElement jsonElement = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, allKits);
            GSON.toJson(jsonElement, writer);
        } catch (IOException e) {
            LOGGER.warn("Could not write to kit file", e);
        }
    }

    private Path getPathForConfig() {
        return CommonPlatform.platform.getRootConfigPath("kits_and_more/kits.json");
    }


}
