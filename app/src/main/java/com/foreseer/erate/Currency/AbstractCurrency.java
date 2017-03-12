package com.foreseer.erate.Currency;

/**
 * Created by Foreseer on 12/03/2017.
 */

public class AbstractCurrency {
    private String currencyCode;
    private String currencyName;
    private String currencyImageName;

    public AbstractCurrency(String currencyCode, String currencyName, String currencyImageName) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.currencyImageName = currencyImageName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyImageName() {
        return currencyImageName;
    }

    public void setCurrencyImageName(String currencyImageName) {
        this.currencyImageName = currencyImageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractCurrency that = (AbstractCurrency) o;

        return currencyCode.equals(that.currencyCode);

    }

    @Override
    public int hashCode() {
        return currencyCode.hashCode();
    }
}
