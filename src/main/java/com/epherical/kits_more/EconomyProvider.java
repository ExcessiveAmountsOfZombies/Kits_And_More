package com.epherical.kits_more;

import com.epherical.kits_more.util.econ.BasicCurrency;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.Economy;
import com.epherical.octoecon.api.user.FakeUser;
import com.epherical.octoecon.api.user.UniqueUser;
import com.epherical.octoecon.api.user.User;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EconomyProvider implements Economy {

    private final KitsMod mod;
    public final Map<ResourceLocation, Currency> currencyMap = Maps.newHashMap();
    public final Map<ResourceLocation, FakeUser> fakeUsers = Maps.newHashMap();

    private final ResourceLocation currencyName = new ResourceLocation("eights_economy", "dollars");


    public EconomyProvider(KitsMod mod, List<Currency> currencyList) {
        this.mod = mod;
        currencyMap.put(currencyName, new BasicCurrency(currencyName));
        for (Currency currency : currencyList) {
            currencyMap.put(new ResourceLocation(currency.getIdentity()), currency);
        }
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public Collection<Currency> getCurrencies() {
        return currencyMap.values();
    }

    @Override
    public Currency getDefaultCurrency() {
        return currencyMap.get(currencyName);
    }

    @Override
    public Currency getCurrency(ResourceLocation identifier) {
        return currencyMap.get(identifier);
    }

    @Override
    public FakeUser getOrCreateAccount(ResourceLocation identifier) {
        return fakeUsers.get(identifier);
    }

    @Override
    public UniqueUser getOrCreatePlayerAccount(UUID identifier) {
        UniqueUser user = mod.userData.getUser(identifier);
        if (user == null) {
            /*try { // todo; may change this
                PlayerUser user1 = data.loadUser(identifier);
                cachePlayer(user1);
                return user1;
            } catch (Exception e) {
                if (server != null) {
                    Optional<GameProfile> profile = server.getProfileCache().get(identifier);
                    GameProfile gameProfile = profile.orElse(null);
                    if (gameProfile != null) {
                        user = new PlayerUser(identifier, gameProfile.getName(), createAccount(Maps.newHashMap()));
                        try {
                            data.saveUser((PlayerUser) user);
                        } catch (EconomyException economyException) {
                            economyException.printStackTrace();
                        }
                        return user;
                    } else if (identifier.equals(Util.NIL_UUID)) {
                        user = new PlayerUser(identifier, "admin", createAccount(Maps.newHashMap()));
                        try {
                            data.saveUser((PlayerUser) user);
                        } catch (EconomyException economyException) {
                            economyException.printStackTrace();
                        }
                        return user;
                    }
                }
            }*/
        }
        return user;
    }

    @Override
    public @Nullable UniqueUser getPlayerAccountByName(String s) {
        return null;
    }

    @Override
    public Collection<UniqueUser> getUniqueUsers() {
        return mod.userData.getUsers().values();
    }

    @Override
    public Collection<User> getAllUsers() {
        return Stream.concat(getFakeUsers().stream(), getUniqueUsers().stream()).collect(Collectors.toList());
    }

    @Override
    public Collection<FakeUser> getFakeUsers() {
        return fakeUsers.values();
    }

    @Override
    public boolean hasAccount(UUID identifier) {
        return mod.userData.userExists(identifier);
    }

    @Override
    public boolean hasAccount(ResourceLocation identifier) {
        // todo; when we create banks?
        return false;
    }

    @Override
    public boolean deleteAccount(UUID identifier) {
        return false;
    }

    @Override
    public boolean deleteAccount(ResourceLocation identifier) {
        return false;
    }

}
