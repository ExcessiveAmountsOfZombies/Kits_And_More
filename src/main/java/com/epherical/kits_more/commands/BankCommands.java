package com.epherical.kits_more.commands;

import com.epherical.kits_more.KitsMod;
import com.epherical.kits_more.Permission;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import org.slf4j.Logger;

public class BankCommands {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static KitsMod instance;


    public static void register(KitsMod mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        instance = mod;

        dispatcher.register(Commands.literal("bank")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(stack -> require(stack, mod.CREATE_BANK))
                                .executes()))
                .then(Commands.literal("balance")
                        .requires(stack -> require(stack, mod.BALANCE_BANK))
                        .executes())
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.players())
                                .requires(stack -> require(stack, mod.INVITE_BANK))
                                .executes()))
                .then(Commands.literal("remove")
                        .then(Commands.argument("knownPlayer", StringArgumentType.string())
                                .requires(stack -> require(stack, mod.REMOVE_PLAYER_BANK))
                                .executes()))
                .then(Commands.literal("deposit")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0d))
                                .requires(stack -> require(stack, mod.DEPOSIT_PLAYER_BANK))
                                .executes()))
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0d))
                                .requires(stack -> require(stack, mod.WITHDRAW_PLAYER_BANK))
                                .executes()))
                .then(Commands.literal("setrank")
                        .then(Commands.argument("rank", StringArgumentType.string())
                                .requires(stack -> require(stack, mod.SET_RANK_PLAYER_BANK))
                                .executes()))
                .then());
    }

    private static boolean require(CommandSourceStack stack, Permission node) {
        return node.getPlatformResolver().resolve(stack, stack.getPlayer());
    }



}
