package com.epherical.kits_more.util;

import com.epherical.kits_more.mixin.InventoryAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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

    public void giveKitToPlayer(ServerPlayer player, boolean firstLogin) {
        if (firstLogin) {
            player.getInventory().load((ListTag) items.get("Inventory"));
        } else {
            Inventory inventory = new Inventory(player);
            inventory.load((ListTag) items.get("Inventory"));
            InventoryAccessor accessor = (InventoryAccessor) inventory;
            for (NonNullList<ItemStack> compartment : accessor.getCompartments()) {
                for (ItemStack itemStack : compartment) {
                    if (!itemStack.isEmpty()) {
                        ItemEntity drop = player.drop(itemStack, false, false);
                        if (drop != null) {
                            drop.setPickUpDelay(0);
                        }
                    }
                }
            }
        }

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
