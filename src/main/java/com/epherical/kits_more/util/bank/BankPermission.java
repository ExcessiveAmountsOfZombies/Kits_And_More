package com.epherical.kits_more.util.bank;


public enum BankPermission {

    DEPOSIT("bank.permission.deposit"),
    WITHDRAW("bank.permission.withdraw"),
    INVITE("bank.permission.invite"),
    REMOVE("bank.permission.remove"),
    BALANCE("bank.permission.balance"),
    SET_RANK("bank.permission.set_rank"),
    DELETE("bank.permission.delete");


    String permissionTranslationKey;

    BankPermission(String s) {
        this.permissionTranslationKey = s;
    }

    public String getPermissionTranslationKey() {
        return permissionTranslationKey;
    }
}
