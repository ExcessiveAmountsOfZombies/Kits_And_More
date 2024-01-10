package com.epherical.kits_more.util;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public class Kit {

    private String name;
    private int cooldownMinutes;
    private CompoundTag items;

    public Kit(String name, int cooldownMinutes, CompoundTag items) {
        this.name = name;
        this.cooldownMinutes = cooldownMinutes;
        this.items = items;
    }

    public CompoundTag getItems() {
        return items;
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Kit kit = (Kit) o;

        if (cooldownMinutes != kit.cooldownMinutes) return false;
        if (!Objects.equals(name, kit.name)) return false;
        return Objects.equals(items, kit.items);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + cooldownMinutes;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }
}
