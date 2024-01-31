package com.epherical.kits_more.util.bank;

import com.epherical.kits_more.util.User;

public class BankUser {

    private User user;
    private BankRank rank;

    public BankUser(User user, BankRank rank) {
        this.user = user;
        this.rank = rank;
    }

    public boolean doesUserHavePermission(BankPermission permission) {
        return this.rank.getPermissions().contains(permission);
    }

    public BankRank getRank() {
        return rank;
    }

    public User getUser() {
        return user;
    }


}
