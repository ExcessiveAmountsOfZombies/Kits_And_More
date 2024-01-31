package com.epherical.kits_more.util.bank;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.epherical.kits_more.util.bank.BankPermission.*;

public class BankRank {

    public static final BankRank PARTICIPANT = def("participant", DEPOSIT);
    public static final BankRank MANAGER = def("manager", DEPOSIT, WITHDRAW, INVITE);
    public static final BankRank ADMIN = def("admin", DEPOSIT, WITHDRAW, INVITE, REMOVE, SET_RANK, DELETE);


    private String name;
    private Set<BankPermission> permissions;


    public BankRank(String name, BankPermission... defaultPermissions) {
        this.name = name;
        this.permissions = new HashSet<>(Arrays.asList(defaultPermissions));
    }

    public Set<BankPermission> getPermissions() {
        return permissions;
    }

    public String getName() {
        return name;
    }

    private static BankRank def(String name, BankPermission... perms) {
        return new BankRank(name, perms);
    }
}
