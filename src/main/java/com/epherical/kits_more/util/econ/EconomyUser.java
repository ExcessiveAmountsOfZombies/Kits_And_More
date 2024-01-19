package com.epherical.kits_more.util.econ;

import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.transaction.Transaction;
import com.epherical.octoecon.api.user.User;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;

import java.util.Map;

import static com.epherical.octoecon.api.transaction.Transaction.Response.SUCCESS;
import static com.epherical.octoecon.api.transaction.Transaction.Type.*;

public abstract class EconomyUser implements User {
    protected final String identifier;
    protected boolean dirty = false;
    protected double balance = 0.0D;


    public EconomyUser(String name) {
        this.identifier = name;
    }

    public void load(CompoundTag tag) {
        balance = tag.getDouble("balance");
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", identifier);
        tag.putDouble("balance", balance);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return Component.nullToEmpty(identifier);
    }

    @Override
    public double getBalance(Currency currency) {
        return balance;
    }

    @Override
    public Map<Currency, Double> getAllBalances() {
        return Maps.newHashMap();
    }

    @Override
    public boolean hasAmount(Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }

    @Override
    public Transaction resetBalance(Currency currency) {
        if (currency.balanceProvider() != null) {
            return currency.balanceProvider().setBalance(this, 0, currency);
        }
        double currentValue = balance;
        balance = 0;
        dirty = true;
        return new BasicTransaction(currentValue, currency, this, "Reset balance of User", SUCCESS, currentValue <= 0 ? DEPOSIT : WITHDRAW);
    }

    @Override
    public Map<Currency, Transaction> resetAllBalances() {
        dirty = true;
        return Maps.newHashMap();
    }

    @Override
    public Transaction setBalance(Currency currency, double amount) {
        balance = amount;
        dirty = true;
        return new BasicTransaction(amount, currency, this, "Set balance of user", SUCCESS, SET);
    }

    @Override
    public Transaction sendTo(User user, Currency currency, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        Transaction transaction = withdrawMoney(currency, amount, "Sending money from " + this.getIdentity() + " to " + user.getIdentity() + ".");
        user.depositMoney(currency, amount, user.getIdentity() + " received money from " + this.getIdentity() + ".");
        dirty = true;
        return transaction;
    }

    @Override
    public Transaction depositMoney(Currency currency, double amount, String reason) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        Transaction transaction = new BasicTransaction(amount, currency, this, reason, SUCCESS, DEPOSIT);
        this.addTransaction(transaction);
        dirty = true;
        return transaction;
    }

    @Override
    public Transaction withdrawMoney(Currency currency, double amount, String reason) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        Transaction transaction = new BasicTransaction(amount, currency, this, reason, SUCCESS, WITHDRAW);
        this.addTransaction(transaction);
        dirty = true;
        return transaction;
    }

    @Override
    public String getIdentity() {
        return identifier;
    }

    public void addTransaction(Transaction transaction) {
        balance = transactionBalance(transaction);
    }

    public double transactionBalance(Transaction transaction) {
        double sum = balance;
        if (transaction.getTransactionType().equals(DEPOSIT)) {
            sum += transaction.getTransactionDelta();
        } else if (transaction.getTransactionType().equals(WITHDRAW)) {
            sum -= transaction.getTransactionDelta();
        } else if (transaction.getTransactionType().equals(SET)) {
            sum = transaction.getTransactionDelta();
        }
        return sum;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EconomyUser that = (EconomyUser) o;

        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
