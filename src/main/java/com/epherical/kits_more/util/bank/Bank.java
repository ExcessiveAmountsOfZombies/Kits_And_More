package com.epherical.kits_more.util.bank;

import com.epherical.kits_more.util.econ.EconomyUser;
import com.epherical.octoecon.api.user.FakeUser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class Bank extends EconomyUser implements FakeUser {

    private final ResourceLocation bankID;

    private final

    public Bank(String name) {
        super(name);
        bankID = new ResourceLocation("kam", name);
    }


    public void load(CompoundTag tag) {
        super.load(tag);
    }

    public CompoundTag save() {
        return super.save();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return bankID;
    }
}
