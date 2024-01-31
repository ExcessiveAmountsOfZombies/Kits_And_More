package com.epherical.kits_more.commands;

import com.epherical.kits_more.Constants;
import com.epherical.kits_more.KitsMod;
import com.epherical.kits_more.Permission;
import com.epherical.kits_more.util.User;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.user.UniqueUser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EconomyCommands {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static KitsMod instance;


    public static void register(KitsMod mod, CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        instance = mod;
        dispatcher.register(Commands.literal("baltop")
                .requires(stack -> require(stack, mod.BALTOP))
                .executes(EconomyCommands::runBaltop)
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(EconomyCommands::runBaltop)));
        LiteralCommandNode<CommandSourceStack> mainCommand = dispatcher.register(Commands.literal("bal")
                .requires(stack -> require(stack, mod.CHECK))
                .executes(context -> checkBalance(context, context.getSource().getTextName()))
                .then(Commands.literal("add")
                        .requires(stack -> require(stack, mod.ADD))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(EconomyCommands::createPlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::addMoney))))
                .then(Commands.literal("remove")
                        .requires(stack -> require(stack, mod.REMOVE))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(EconomyCommands::createPlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::removeMoney))))
                .then(Commands.literal("set")
                        .requires(stack -> require(stack, mod.SET))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(EconomyCommands::createPlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::setMoney))))
                .then(Commands.literal("pay")
                        .requires(stack -> require(stack, mod.PAY))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(EconomyCommands::createPlayerSuggestions)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(EconomyCommands::payMoney))))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(EconomyCommands::createPlayerSuggestions)
                        .requires(stack -> require(stack, mod.CHECK_OTHER))
                        .executes(context -> checkBalance(context, StringArgumentType.getString(context, "player")))));
        dispatcher.register(Commands.literal("balance").redirect(mainCommand));
        dispatcher.register(Commands.literal("money").redirect(mainCommand));
    }

    private static boolean require(CommandSourceStack stack, Permission node) {
        return node.getPlatformResolver().resolve(stack, stack.getPlayer());
    }
    protected static int checkBalance(CommandContext<CommandSourceStack> context, String player) {
        User user = (User) instance.provider.getPlayerAccountByName(player);
        if (user != null) {
            Currency currency = instance.provider.getDefaultCurrency();
            double balance = user.getBalance(currency);
            Component text = currency.format(balance, 2);
            Component playerName = Component.literal(player).setStyle(Constants.VARIABLE_STYLE);
            Component actualMessage = instance.translations.createTranslation(context.getSource().getPlayer(), "econ.player.has", "%s has %s.", playerName, text).setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> actualMessage, true);
        }

        return 1;
    }

    protected static int addMoney(CommandContext<CommandSourceStack> context) {
        String pString = StringArgumentType.getString(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser user = instance.provider.getPlayerAccountByName(pString);
        if (user != null) {
            Currency currency = instance.provider.getDefaultCurrency();
            user.depositMoney(currency, amount, "command");
            Component playerName = pluralize(user.getIdentity(), Constants.VARIABLE_STYLE);
            Component component = instance.translations.createTranslation(context.getSource().getPlayer(), "econ.player.added", currency.format(amount, 2), playerName)
                    .setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> component, false);
        }

        return 1;
    }

    protected static int removeMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String player = StringArgumentType.getString(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser user = instance.provider.getPlayerAccountByName(player);
        if (user != null) {
            Currency currency = instance.provider.getDefaultCurrency();
            user.withdrawMoney(currency, amount, "command");
            Component playerName = pluralize(player, Constants.VARIABLE_STYLE);
            Component component = instance.translations.createTranslation(context.getSource().getPlayer(), "econ.player.removed", currency.format(amount, 2), playerName)
                    .setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> component, false);
        }

        return 1;
    }

    protected static int setMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String player = StringArgumentType.getString(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser user = instance.provider.getPlayerAccountByName(player);
        if (user != null) {
            Currency currency = instance.provider.getDefaultCurrency();
            user.setBalance(currency, amount);
            Component playerName = pluralize(user.getIdentity(), Constants.VARIABLE_STYLE);
            Component component = instance.translations.createTranslation(context.getSource().getPlayer(), "econ.player.set", currency.format(amount, 2), playerName)
                    .setStyle(Constants.APPROVAL_STYLE);
            context.getSource().sendSuccess(() -> component, false);
        }

        return 1;
    }

    protected static int payMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer source = context.getSource().getPlayerOrException();
        String target = StringArgumentType.getString(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        UniqueUser sourceUser = instance.provider.getOrCreatePlayerAccount(source.getUUID());
        UniqueUser targetUser = instance.provider.getPlayerAccountByName(target);
        if (sourceUser != null && targetUser != null) {
            if (sourceUser.equals(targetUser)) {
                Component message = instance.translations.createTranslation(source, "econ.player.error.send_money_to_self").setStyle(Constants.ERROR_STYLE);
                context.getSource().sendFailure(message);
                return 0;
            }

            Currency currency = instance.provider.getDefaultCurrency();
            if (sourceUser.hasAmount(currency, amount)) {
                sourceUser.sendTo(targetUser, currency, amount);
                Component targetName = Component.literal(targetUser.getIdentity()).withStyle(Constants.VARIABLE_STYLE);
                Component sourceName = Component.literal(source.getScoreboardName()).withStyle(Constants.VARIABLE_STYLE);
                Component sourceMessage = instance.translations.createTranslation(source, "econ.player.success.send_money", currency.format(amount, 2), targetName)
                        .setStyle(Constants.APPROVAL_STYLE);

                context.getSource().sendSuccess(() -> sourceMessage, true);
                ServerPlayer targetPlayer = ((User) targetUser).getPlayer();
                if (targetPlayer != null) {
                    Component targetMessage = instance.translations.createTranslation(targetPlayer, "econ.player.success.receive_money", currency.format(amount, 2), sourceName)
                            .setStyle(Constants.APPROVAL_STYLE);
                    targetPlayer.sendSystemMessage(targetMessage);
                }
                instance.userData.savePlayer((User) targetUser);

            }
        }
        return 1;
    }

    private static int runBaltop(CommandContext<CommandSourceStack> stack) {
        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(stack, "page");
        } catch (IllegalArgumentException ignored) {
        }
        List<UniqueUser> users = new ArrayList<>(instance.userData.getUsers().values());
        Comparator<UniqueUser> userComparator = Comparator.comparingDouble(value -> value.getBalance(instance.provider.getDefaultCurrency()));
        users.sort(userComparator.reversed());
        page = Math.max(page, 1);
        int maxPage = Math.max(users.size() / 10, 1);
        maxPage = users.size() % 10 != 0 ? maxPage + 1 : maxPage;
        int counter = page == 1 ? 1 : ((page - 1) * 10) + 1;

        int begin = page == 1 ? 0 : Math.min(users.size(), ((page - 1) * 10));
        int end = page == 1 ? Math.min(users.size(), 10) : Math.min(users.size(), (page * 10));


        List<UniqueUser> sublist = users.subList(begin, end);
        if (sublist.isEmpty()) {
            stack.getSource().sendSuccess(() -> Component.translatable("No Entries"), false);
        } else {
            Component component = Component.literal("-=- ").setStyle(Constants.CONSTANTS_STYLE)
                    .append(Component.literal("Page ").setStyle(Constants.CONSTANTS_STYLE))
                    .append(Component.literal("" + page).setStyle(Constants.VARIABLE_STYLE))
                    .append(Component.literal("/").setStyle(Constants.CONSTANTS_STYLE))
                    .append(Component.literal("" + maxPage).setStyle(Constants.VARIABLE_STYLE))
                    .append(Component.literal(" -=- Top Balances -=-=-").setStyle(Constants.CONSTANTS_STYLE));
            stack.getSource().sendSuccess(() -> component, false);
            for (UniqueUser user : users.subList(begin, end)) {
                Component money = instance.provider.getDefaultCurrency().format(user.getBalance(instance.provider.getDefaultCurrency()));
                Component row = Component.literal(counter + ". ").setStyle(Constants.VARIABLE_STYLE)
                        .append(user.getDisplayName())
                        .append(" ")
                        .append(money);
                stack.getSource().sendSuccess(() ->  row, false);
                counter++;
            }
        }


        return 1;
    }

    private static Component pluralize(String name, Style style) {
        name = name.endsWith("s") ? name + "'" : name + "'s";
        return Component.literal(name).setStyle(style);
    }

    private static CompletableFuture<Suggestions> createPlayerSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String s : context.getSource().getServer().getPlayerList().getPlayerNamesArray()) {
            builder.suggest(s);
        }
        return builder.buildFuture();
    }
}
