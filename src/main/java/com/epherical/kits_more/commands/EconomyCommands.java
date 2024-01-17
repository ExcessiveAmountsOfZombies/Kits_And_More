package com.epherical.kits_more.commands;

import com.epherical.kits_more.Constants;
import com.epherical.kits_more.KitsMod;
import com.epherical.kits_more.Permission;
import com.epherical.kits_more.config.Translations;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.user.UniqueUser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.function.Predicate;

public class EconomyCommands {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static KitsMod instance;


    public static void register(KitsMod mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        instance = mod;
        LiteralCommandNode<CommandSourceStack> mainCommand = dispatcher.register(Commands.literal("bal")
                .requires(stack -> require(stack, mod.CHECK))
                .executes(context -> checkBalance(context, context.getSource().getPlayerOrException()))
                .then(Commands.literal("add")
                        .requires(stack -> require(stack, mod.ADD))
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::addMoney))))
                .then(Commands.literal("remove")
                        .requires(stack -> require(stack, mod.REMOVE))
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::removeMoney))))
                .then(Commands.literal("set")
                        .requires(stack -> require(stack, mod.SET))
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::setMoney))))
                .then(Commands.literal("pay")
                        .requires(stack -> require(stack, mod.PAY))
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::payMoney))))
                .then(Commands.argument("player", EntityArgument.players())
                        .requires(stack -> require(stack, mod.CHECK_OTHER))
                        .executes(context -> checkBalance(context, EntityArgument.getPlayer(context, "player")))));
        dispatcher.register(Commands.literal("balance").redirect(mainCommand));
        dispatcher.register(Commands.literal("money").redirect(mainCommand));
    }

    private static boolean require(CommandSourceStack stack, Permission node) {
        return node.getPlatformResolver().resolve(stack, stack.getPlayer());
    }

    protected static int checkBalance(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        UniqueUser user = provider.getOrCreatePlayerAccount(player.getUUID());
        if (user != null) {
            Currency currency = provider.getDefaultCurrency();
            double balance = user.getBalance(currency);
            Component text = currency.format(balance, 2);
            Component playerName = Component.literal(player.getScoreboardName()).setStyle(Constants.VARIABLE_STYLE);

            Component actualMessage = instance.translations.createTranslation(player, "econ.player.has", "%s has %s.", playerName, text).setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> actualMessage, true);
        }

        return 1;
    }

    protected static int addMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser user = provider.getOrCreatePlayerAccount(player.getUUID());
        if (user != null) {
            Currency currency = provider.getDefaultCurrency();
            user.depositMoney(currency, amount, "command");
            Component playerName = pluralize(player.getScoreboardName(), Constants.VARIABLE_STYLE);
            Component component = Component.translatable("Added %s to %s account.", currency.format(amount, 2), playerName)
                    .setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> component, false);
        }

        return 1;
    }

    protected static int removeMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser user = provider.getOrCreatePlayerAccount(player.getUUID());
        if (user != null) {
            Currency currency = provider.getDefaultCurrency();
            user.withdrawMoney(currency, amount, "command");
            Component playerName = pluralize(player.getScoreboardName(), Constants.VARIABLE_STYLE);
            Component component = Component.translatable("Removed %s from %s account.", currency.format(amount, 2), playerName)
                    .setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> component, false);
        }

        return 1;
    }

    protected static int setMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser user = provider.getOrCreatePlayerAccount(player.getUUID());
        if (user != null) {
            Currency currency = provider.getDefaultCurrency();
            user.setBalance(currency, amount);
            Component playerName = pluralize(player.getScoreboardName(), Constants.VARIABLE_STYLE);
            Component component = Component.translatable("Set money to %s in %s account.", currency.format(amount, 2), playerName)
                    .setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> component, false);
        }

        return 1;
    }

    protected static int payMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer source = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser sourceUser = provider.getOrCreatePlayerAccount(source.getUUID());
        UniqueUser targetUser = provider.getOrCreatePlayerAccount(target.getUUID());
        if (sourceUser != null && targetUser != null) {
            if (source.equals(target)) {
                Component message = Component.literal("You can't send money to yourself!").setStyle(Constants.ERROR_STYLE);
                context.getSource().sendFailure(message);
                return 0;
            }

            Currency currency = provider.getDefaultCurrency();
            if (sourceUser.hasAmount(currency, amount)) {
                sourceUser.sendTo(targetUser, currency, amount);
                Component targetName = Component.literal(target.getScoreboardName()).withStyle(Constants.VARIABLE_STYLE);
                Component sourceName = Component.literal(source.getScoreboardName()).withStyle(Constants.VARIABLE_STYLE);
                Component sourceMessage = Component.translatable("You have sent %s to %s!", currency.format(amount, 2), targetName)
                        .setStyle(Constants.APPROVAL_STYLE);
                Component targetMessage = Component.translatable("You have received %s from %s!", currency.format(amount, 2), sourceName)
                        .setStyle(Constants.APPROVAL_STYLE);
                context.getSource().sendSuccess(() -> sourceMessage, true);
                target.sendSystemMessage(targetMessage);
            }
        }
        return 1;
    }

    private static Component pluralize(String name, Style style) {
        name = name.endsWith("s") ? name + "'" : name + "'s";
        return Component.literal(name).setStyle(style);
    }
}
