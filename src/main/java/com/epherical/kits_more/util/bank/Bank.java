package com.epherical.kits_more.util.bank;

import com.epherical.kits_more.util.econ.EconomyUser;
import com.epherical.octoecon.api.user.FakeUser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bank extends EconomyUser implements FakeUser {

    private final ResourceLocation bankID;

    private List<BankRank> ranks;
    private List<BankUser> users;


    public Bank(String name) {
        super(name);
        bankID = new ResourceLocation("kam", name);
        ranks = Arrays.asList(BankRank.PARTICIPANT, BankRank.MANAGER, BankRank.ADMIN);
        users = new ArrayList<>();
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

    public List<BankUser> getUsers() {
        return users;
    }
}
