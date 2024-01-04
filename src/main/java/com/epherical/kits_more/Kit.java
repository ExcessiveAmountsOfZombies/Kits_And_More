package com.epherical.kits_more;

import net.minecraft.nbt.CompoundTag;

public class Kit {

    private String name;
    private int cooldownMinutes;

    private CompoundTag items;

    public Kit(String name, int cooldownMinutes, CompoundTag items) {
        this.name = name;
        this.cooldownMinutes = cooldownMinutes;
        this.items = items;
    }


}
