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
        currencyList.add(new AbstractCurrency("AUD", "Australian Dollar", "australia"));
        currencyList.add(new AbstractCurrency("BGN", "Bulgarian Lev", "bulgaria"));
        currencyList.add(new AbstractCurrency("BRL", "Brazilian Real", "brazil"));
        currencyList.add(new AbstractCurrency("CAD", "Canadian Dollar", "canada"));
        currencyList.add(new AbstractCurrency("CHF", "Swiss Franc", "switzerland"));
        currencyList.add(new AbstractCurrency("CNY", "Chinese Yuan", "china"));
        currencyList.add(new AbstractCurrency("CZK", "Czech Republic Koruna", "czechrepublic"));
        currencyList.add(new AbstractCurrency("DKK", "Danish Krone", "denmark"));
        currencyList.add(new AbstractCurrency("GBP", "British Pound", "unitedkingdom"));
        currencyList.add(new AbstractCurrency("HKD", "Hong Kong Dollar", "hongkong"));
        currencyList.add(new AbstractCurrency("HRK", "Croatian Kuna", "croatia"));
        currencyList.add(new AbstractCurrency("HUF", "Hungarian Forint", "hungary"));
        currencyList.add(new AbstractCurrency("IDR", "Indonesian Rupiah", "indonesia"));
        currencyList.add(new AbstractCurrency("ILS", "Israeli New Shekel", "israel"));
        currencyList.add(new AbstractCurrency("INR", "Indian Rupee", "india"));
        currencyList.add(new AbstractCurrency("JPY", "Japanese Yen", "japan"));
        currencyList.add(new AbstractCurrency("KRW", "Korean Won", "southkorea"));
        currencyList.add(new AbstractCurrency("MXN", "Mexican Peso", "mexico"));
        currencyList.add(new AbstractCurrency("MYR", "Malaysian Ringgit", "malasya"));
        currencyList.add(new AbstractCurrency("NOK", "Norwegian Krone", "norway"));
        currencyList.add(new AbstractCurrency("NZD", "New Zealand Dollar", "newzealand"));
        currencyList.add(new AbstractCurrency("PHP", "Philippine Peso", "philippines"));
        currencyList.add(new AbstractCurrency("PLN", "Polish Zlony", "poland"));
        currencyList.add(new AbstractCurrency("RON", "Romanian Leu", "romania"));
        currencyList.add(new AbstractCurrency("SEK", "Swedish Krone", "sweden"));
        currencyList.add(new AbstractCurrency("SGD", "Singapore Dollar", "singapore"));
        currencyList.add(new AbstractCurrency("THB", "Thai Baht", "Thailand"));
        currencyList.add(new AbstractCurrency("TRY", "Turkish Lira", "turkey"));
        currencyList.add(new AbstractCurrency("ZAR", "South African Rand", "southafrica"));
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
