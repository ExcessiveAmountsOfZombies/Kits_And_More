package com.epherical.kits_more.commands;

import com.epherical.kits_more.Constants;
import com.epherical.kits_more.KitsMod;
import com.epherical.kits_more.util.Kit;
import com.epherical.kits_more.util.User;
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
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class KitCommand {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static KitsMod instance;


    public static void register(KitsMod mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        instance = mod;
        dispatcher.register(Commands.literal("kit")
                .then(Commands.argument("name", StringArgumentType.string())
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
                                .then(Commands.argument("cooldownMins", IntegerArgumentType.integer(-1))
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
                                .then(Commands.argument("cooldownMins", IntegerArgumentType.integer(-1))
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

        if (!overwrite && instance.kitData.KITS.containsKey(kitName)) {
            LOGGER.debug("Attempted to overwrite a kit without setting the overwrite flag.");
            player.sendSystemMessage(Constants.sendFailureMessage("Kit %s could not be overwritten, the overwrite flag was not set!", kitName));
            return 0;
        }

        Kit kit = new Kit(kitName, cooldownMinutes, tag);
        instance.kitData.saveKitsToFile(kit);
        player.sendSystemMessage(Constants.sendSuccessMessage("Kit %s was successfully created with a cooldown of %s minutes.", kitName, cooldownMinutes));
        return 1;
    }

    private static int useKit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        User user = instance.userData.getUser(player);
        String name = StringArgumentType.getString(context, "name");
        Kit kit = instance.kitData.KITS.get(name);
        Instant coolDownForKit = user.getCoolDownForKit(kit);
        if (coolDownForKit == null || coolDownForKit.isBefore(Instant.now())) {
            // We can provide the kit to the player
            kit.giveKitToPlayer(player, false);
            user.addCoolDownForKit(kit);
            player.sendSystemMessage(Constants.sendSuccessMessage("Here is your kit!"));
        } else {
            long until = coolDownForKit.until(Instant.now(), ChronoUnit.MINUTES);
            player.sendSystemMessage(Constants.sendFailureMessage("Your kit is currently on cooldown, you cannot claim it. Claimable in %s Minutes", until));
        }


        return 1;
    }

    private static int deleteKit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "name");
        Kit kit = instance.kitData.deleteKitAndSave(name);
        if (kit != null) {
            player.sendSystemMessage(Constants.sendSuccessMessage("The kit %s was deleted.", kit.getName()));
        } else {
            player.sendSystemMessage(Constants.sendFailureMessage("The kit %s does not exist!", name));
        }

        return 1;
    }


}
