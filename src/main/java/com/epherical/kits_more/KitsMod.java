package com.epherical.kits_more;

import com.epherical.epherolib.libs.org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import com.epherical.kits_more.config.Config;
import com.epherical.kits_more.config.Translations;
import com.epherical.kits_more.data.KitData;
import com.epherical.kits_more.data.UserData;
import com.epherical.kits_more.util.Kit;
import com.epherical.kits_more.util.User;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KitsMod {

    protected static final Logger LOGGER = LogUtils.getLogger();

    protected ScheduledExecutorService saveSchedule = Executors.newSingleThreadScheduledExecutor();


    public static final List<Permission> PERMISSIONS = new ArrayList<>();

    public final Permission KIT_CREATION = registerPermission(new Permission(Constants.id("kit.create"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public final Permission KIT_DELETION = registerPermission(new Permission(Constants.id("kit.delete"), (stack, player) -> getDefaultPerms(stack, player, 4)));
    public final Permission KIT_USE = registerPermission(new Permission(Constants.id("kit.use"), (stack, player) -> {
        // todo; we won't always assume this
        return getDefaultPerms(stack, player, 0);
    }));

    public final Permission CHECK = registerPermission(new Permission(Constants.id("command.balance.check"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission CHECK_OTHER = registerPermission(new Permission(Constants.id("command.balance.check.other"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission ADD = registerPermission(new Permission(Constants.id("command.balance.add"), (stack, player) -> getDefaultPerms(stack, player, 2)));
    public final Permission REMOVE = registerPermission(new Permission(Constants.id("command.balance.remove"), (stack, player) -> getDefaultPerms(stack, player, 2)));
    public final Permission SET = registerPermission(new Permission(Constants.id("command.balance.set"), (stack, player) -> getDefaultPerms(stack, player, 2)));
    public final Permission PAY = registerPermission(new Permission(Constants.id("command.balance.pay"), (stack, player) -> getDefaultPerms(stack, player, 0)));

    public final Permission BALTOP = registerPermission(new Permission(Constants.id("command.baltop.check"), (stack, player) -> getDefaultPerms(stack, player, 0)));

    public final Permission CREATE_BANK = registerPermission(new Permission(Constants.id("command.bank.create"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission BALANCE_BANK = registerPermission(new Permission(Constants.id("command.bank.balance.check"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission INVITE_BANK = registerPermission(new Permission(Constants.id("command.bank.invite"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission REMOVE_PLAYER_BANK = registerPermission(new Permission(Constants.id("command.bank.remove.player"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission DEPOSIT_PLAYER_BANK = registerPermission(new Permission(Constants.id("command.bank.deposit.player"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission WITHDRAW_PLAYER_BANK = registerPermission(new Permission(Constants.id("command.bank.withdraw.player"), (stack, player) -> getDefaultPerms(stack, player, 0)));
    public final Permission SET_RANK_PLAYER_BANK = registerPermission(new Permission(Constants.id("command.bank.set.rank.player"), (stack, player) -> getDefaultPerms(stack, player, 0)));



    public UserData userData;
    public KitData kitData = new KitData();
    public Config config;
    public EconomyProvider provider;

    public Translations translations;

    public KitsMod() {}

    public void init() {
        config = new Config(HoconConfigurationLoader.builder(), "kits_and_more.conf");
        config.loadConfig("kits_and_more");


        provider = new EconomyProvider(this, new ArrayList<>());

        translations = new Translations("translations");
        translations.loadTranslations("kits_and_more", "en_us");
    }


    private boolean getDefaultPerms(CommandSourceStack stack, ServerPlayer player, int level) {
        if (player != null) {
            return player.hasPermissions(level);
        } else {
            return stack.hasPermission(level);
        }
    }

    public Permission registerPermission(Permission permission) {
        PERMISSIONS.add(permission);
        return permission;
    }


    public void onServerStarting(MinecraftServer server) {
        this.userData = new UserData(LevelResource.ROOT, server, "kits_and_more/players");
        this.userData.load();
        this.kitData.loadKitsFromFile();

        if (config.useSaveThread) {
            saveSchedule.scheduleAtFixedRate(userData::savePlayers, 2L, 1L, TimeUnit.MINUTES);
        }
    }

    public void onServerStopping(MinecraftServer server) {
        if (config.useSaveThread) {
            saveSchedule.shutdown();
        }
        userData.savePlayers();
    }


    public void onPlayerJoin(ServerPlayer player) {
        int value = player.getStats().getValue((Stats.CUSTOM), Stats.PLAYER_KILLS);
        User user = userData.userJoin(player);
        if (value > 0 && config.giveKitsInExistingWorlds && !user.hasReceivedFirstLoginBenefits()) {
            provideKit(player, user, false);
            user.setBalance(provider.getDefaultCurrency(), config.moneyGivenOnFirstLogin);
        } else if (value <= 0 && !user.hasReceivedFirstLoginBenefits()) {
            // This happens on first login
            provideKit(player, user, true);
            user.setBalance(provider.getDefaultCurrency(), config.moneyGivenOnFirstLogin);
        }

        userData.savePlayer(user);
    }

    public void provideKit(ServerPlayer player, User user, boolean firstLogin) {
        Kit main = kitData.KITS.get("main");
        if (main != null) {
            main.giveKitToPlayer(player, firstLogin);
            user.setReceivedFirstLoginBenefits(true);
        } else {
            LOGGER.debug("Could not provide a kit to player {} {} as it does not exist", player.getUUID(), player.getScoreboardName());
        }
    }

    public void onPlayerQuit(ServerPlayer player) {
        userData.userQuit(player);
    }


}
