package com.epherical.kits_more.util.bank;

import static com.epherical.kits_more.util.bank.BankPermissions.*;

public enum BankRanks {

    PARTICIPANT(DEPOSIT),
    MANAGER(DEPOSIT, WITHDRAW, INVITE),
    ADMIN(DEPOSIT, WITHDRAW, INVITE, REMOVE, SET_RANK, DELETE);

    BankPermissions[] defaultPermissions;

    BankRanks(BankPermissions... defaultPermissions) {
        this.defaultPermissions = defaultPermissions;
    }

    public BankPermissions[] getDefaultPermissions() {
        return defaultPermissions;
    }
}
