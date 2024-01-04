package com.epherical.kits_more;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitsMod {

    private static Map<String, Kit> KITS = new HashMap<>();

    public static final List<Permission> PERMISSIONS = new ArrayList<>();

    public static final Permission KIT_CREATION = registerPermission(new Permission(Constants.id("kit.create"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public static final Permission KIT_DELETION = registerPermission(new Permission(Constants.id("kit.delete"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public static final Permission KIT_USE = registerPermission(new Permission(Constants.id("kit.use"), (stack, player) -> {
        // todo; we won't always assume this
        return getDefaultPerms(stack, player, 0);
    }));


    private static boolean getDefaultPerms(CommandSourceStack stack, ServerPlayer player, int level) {
        if (player != null) {
            return player.hasPermissions(level);
        } else {
            return stack.hasPermission(level);
        }
    }



    public static Permission registerPermission(Permission permission) {
        PERMISSIONS.add(permission);
        return permission;
    }



    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("kit")
                .then(Commands.argument("type", StringArgumentType.string())
                                .requires(stack -> KIT_USE.getPlatformResolver().resolve(stack, stack.getPlayer()))
                                .executes(KitsMod::useKit)));
        dispatcher.register(Commands.literal("kits")
                        .then(Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.string()))
                                .requires(stack -> KIT_DELETION.getPlatformResolver().resolve(stack, stack.getPlayer())))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("main");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("cooldownMins", IntegerArgumentType.integer(0))
                                        .executes(KitsMod::createKitWithCoolDown)
                                        .then(Commands.literal("overwrite")
                                                .executes(KitsMod::createKitWithCoolDownAndOverwrite)))
                                .requires(stack -> KIT_CREATION.getPlatformResolver().resolve(stack, stack.getPlayer()))
                                .then(Commands.literal("overwrite")
                                        .executes(KitsMod::createKitWithOverwrite))
                                .executes(context -> createKit(context, 60, false)))));

    }

    private static int createKitWithOverwrite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return createKit(context, 60, true);
    }

    private static int createKitWithCoolDownAndOverwrite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return createKit(context, IntegerArgumentType.getInteger(context, "cooldownMins"), true);
    }

    private static int createKitWithCoolDown(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return createKit(context, IntegerArgumentType.getInteger(context, "cooldownMins"), false);
    }

    private static int createKit(CommandContext<CommandSourceStack> context, int cooldownMinutes, boolean overwrite) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String kitName = StringArgumentType.getString(context, "name");

        CompoundTag tag = new CompoundTag();
        tag.putString("kitName", kitName);
        tag.putInt("cooldownMinutes", cooldownMinutes);
        tag.put("Inventory", player.getInventory().save(new ListTag()));

        if (!overwrite && KITS.containsKey(kitName)) {
            // todo; send msg
            return 0;
        }

        Kit kit = new Kit(kitName, cooldownMinutes, tag);
        KITS.put(kitName, kit);


        JsonElement jsonElement = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
        // write the element

        return 1;
    }

    private static int useKit(CommandContext<CommandSourceStack> context) {

    }

}
