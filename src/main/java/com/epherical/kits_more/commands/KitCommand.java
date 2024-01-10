package com.epherical.kits_more.commands;

import com.epherical.epherolib.CommonPlatform;
import com.epherical.kits_more.KitsMod;
import com.epherical.kits_more.util.Kit;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.io.IOException;

import static com.epherical.kits_more.KitsMod.*;

public class KitCommand {

    public static final Logger LOGGER = LogUtils.getLogger();


    public static void register(KitsMod mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("kit")
                .then(Commands.argument("type", StringArgumentType.string())
                        .requires(stack -> mod.KIT_USE.getPlatformResolver().resolve(stack, stack.getPlayer()))
                        .executes(KitCommand::useKit)));
        dispatcher.register(Commands.literal("kits")
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string()))
                        .requires(stack -> mod.KIT_DELETION.getPlatformResolver().resolve(stack, stack.getPlayer()))
                        .executes(KitCommand::deleteKit))
                .then(Commands.literal("create")
                        .requires(stack -> mod.KIT_CREATION.getPlatformResolver().resolve(stack, stack.getPlayer()))
                        .then(Commands.literal("overwrite")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            builder.suggest("main");
                                            return builder.buildFuture();
                                        })
                                        .executes(KitCommand::createKitWithOverwrite))
                                .then(Commands.argument("cooldownMins", IntegerArgumentType.integer(0))
                                        .suggests((context, builder) -> {
                                            // oit no work
                                            builder.suggest(1);
                                            builder.suggest(60);
                                            builder.suggest(-1);
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .suggests((context, builder) -> {
                                                    builder.suggest("main");
                                                    return builder.buildFuture();
                                                })
                                                .executes(KitCommand::createKitWithCoolDownAndOverwrite))))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("main");
                                    return builder.buildFuture();
                                })
                                .executes(context -> createKit(context, 60, false))
                                .then(Commands.argument("cooldownMins", IntegerArgumentType.integer(0))
                                        .suggests((context, builder) -> {
                                            builder.suggest(1);
                                            builder.suggest(60);
                                            builder.suggest(-1);
                                            return builder.buildFuture();
                                        })
                                        .executes(KitCommand::createKitWithCoolDown)
                                ))));

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
            LOGGER.debug("Attempted to overwrite a kit without setting the overwrite flag.");
            context.getSource().sendFailure(Component.translatable("Kit could not be overwritten, the overwrite flag was not set."));
            return 0;
        }

        Kit kit = new Kit(kitName, cooldownMinutes, tag);
        KITS.put(kitName, kit);
        writeKitsToFile();

        return 1;
    }

    private static int useKit(CommandContext<CommandSourceStack> context) {
        return 1;
    }

    private static int deleteKit(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        KITS.remove(name);

        writeKitsToFile();

        return 1;
    }


    private static void writeKitsToFile() {
        CompoundTag allKits = new CompoundTag();

        for (Kit value : KITS.values()) {
            allKits.put(value.getName(), value.getItems());
        }
        try {
            NbtIo.write(allKits, CommonPlatform.platform.getRootConfigPath("kits_and_more").toFile());
        } catch (IOException e) {
            LOGGER.warn("Could not write to kit file", e);
        }
    }

}
