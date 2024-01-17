package com.epherical.kits_more.data.econ;

import com.epherical.kits_more.util.econ.BasicCurrency;
import com.epherical.kits_more.util.econ.NPCUser;
import com.epherical.octoecon.api.Currency;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Map;

public class NPCSerializer implements JsonSerializer<NPCUser>, JsonDeserializer<NPCUser> {

    @Override
    public NPCUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String username = object.getAsJsonPrimitive("name").getAsString();
        Map<Currency, Double> money = context.deserialize(object.get("currencies"), BasicCurrency.class);
        return new NPCUser(new ResourceLocation(username), money);
    }

    @Override
    public JsonElement serialize(NPCUser src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("name", src.getIdentity());
        object.add("currencies", context.serialize(src.getAllBalances()));
        return object;
    }
}
