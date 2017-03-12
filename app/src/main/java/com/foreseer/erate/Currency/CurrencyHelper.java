package com.foreseer.erate.Currency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Foreseer on 12/03/2017.
 */

public class CurrencyHelper {
    private static List<AbstractCurrency> currencyList = new ArrayList<>();

    static {
        loadCurrencies();
    }

    private CurrencyHelper(){ }

    private static void loadCurrencies(){
        currencyList.add(new AbstractCurrency("EUR", "Euro", "europeanunion"));
        currencyList.add(new AbstractCurrency("USD", "U.S. Dollar", "unitedstates"));
        currencyList.add(new AbstractCurrency("RUB", "Russian Rouble", "russia"));
        currencyList.add(new AbstractCurrency("NOK", "Norwegian Krone", "norway"));
    }

    public static AbstractCurrency getCurrency(String currencyCode){
        for (AbstractCurrency currency : currencyList){
            if (currency.getCurrencyCode().equals(currencyCode)){
                return currency;
            }
        }
        return null;
    }
}
