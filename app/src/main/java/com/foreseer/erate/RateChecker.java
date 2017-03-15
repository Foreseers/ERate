package com.foreseer.erate;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.foreseer.erate.SQL.CurrencyTableHandler;
import com.foreseer.erate.Utils.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Foreseer on 09/03/2017.
 */

public class RateChecker {

    // List of all the currencies
    private List<String> currencies;
    // Map of actual exchange rates. String is essentially two currencies, where first is from which currency
    // it's converted
    private HashMap<String, Double> exchangeRates;

    // URL to the API
    private final String API_URL = "http://api.fixer.io/latest?";
    // Whatever that is, helps forming API request I believe
    private final String BASE_PARAM = "base=";

    // True if ratechecker is done updating exchange rates.
    private boolean done;
    // True if ratechecker is for updates, in this case it will call currency DB and update it.
    private boolean forUpdates;

    private boolean interrupted;

    public RateChecker(boolean forUpdates) {
        this.forUpdates = forUpdates;

        currencies = new ArrayList<>();
        exchangeRates = new HashMap<>();

        done = false;
        interrupted = false;

        getCurrencyList();
    }

    /**
     * This method returns the exchangeRates hashmap
     * @return HashMap with exchange rates
     */
    public HashMap<String, Double> getRates(){
        return exchangeRates;
    }

    /**
     * This method starts up the procedure of getting list of currencies.
     */
    private void getCurrencyList() {
        String url = "http://api.fixer.io/latest?base=EUR";
        GetCurrencyListTask getCurrencyListTask = new GetCurrencyListTask();
        getCurrencyListTask.execute(url);
    }

    /**
     * This method starts up the procedure of getting exchange rates
     */
    private void getExchangeRates() {
        GetExchangeRatesTask getExchangeRatesTask = new GetExchangeRatesTask();
        getExchangeRatesTask.execute();
    }

    /**
     * When exchange rates are done updating, this method updates the variable and, if needed, the DB
     * @param exchangeRates
     */
    private void setExchangeRates(HashMap<String, Double> exchangeRates) {
        this.exchangeRates = exchangeRates;
        this.done = true;
        if (forUpdates && !interrupted){
            CurrencyTableHandler instance = CurrencyTableHandler.getInstance();
            if (instance != null){
                instance.updateRates(this);
            }
        }
    }

    /**
     * This method sets currency list
     * @param strings List with currencies
     */
    private void setCurrencies(ArrayList<String> strings) {
        for (String string : strings) {
            currencies.add(string);
        }
    }

    /**
     * @return True if ratechecker is done updating rates.
     */
    public boolean isDone() {
        return done;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    /**
     * Fetches rates from API for a specified currency.
     * @param baseCurrency  Base currency for rate fetch.
     * @return              Exchange rates for base currency.
     */
    private HashMap<String, Double> getRates(String baseCurrency) {
        if (interrupted){
            return null;
        }
        HashMap<String, Double> rates = new HashMap<>();

        try {
            JSONObject rateList = JsonReader.readJsonFromUrl(API_URL + BASE_PARAM + baseCurrency);
            JSONObject rateArray = rateList.getJSONObject("rates");
            Iterator<String> names = rateArray.keys();
            while (names.hasNext()) {
                String name = names.next();
                Double rate = Double.parseDouble(rateArray.getString(name));

                String currencyFormat = baseCurrency + name;
                rates.put(currencyFormat, rate);
            }
        }
        catch (SocketException e){
            Log.e("EXCEPTION", "Network is unreachable!");
            interrupted = true;
            return null;
        }
        catch (IOException | JSONException e) {
            Log.e("EXCEPTION", e.getMessage());
            interrupted = true;
            return null;
        }

        return rates;
    }

    /**
     * This async task fetches list of currencies from internet and updates it.
     * When it's done, starts up a process of updating exchange rates for the currencies.
     */
    private class GetCurrencyListTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String[] params) {
            ArrayList<String> currencyList = new ArrayList<>();
            currencyList.add("EUR");

            try {
                JSONObject rateList = JsonReader.readJsonFromUrl(params[0]);
                JSONObject currArray = rateList.getJSONObject("rates");
                Iterator<String> names = currArray.keys();
                while (names.hasNext()) {
                    currencyList.add(names.next());
                }
            } catch (SocketException e){
                Log.e("EXCEPTION", "Network is unreachable!");
                interrupted = true;
                return null;
            } catch (IOException | JSONException e) {
                Log.e("EXCEPTION", e.getMessage());
                interrupted = true;
                return null;
            }
            return currencyList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            if (strings == null || interrupted){
                done = true;
                return;
            }

            setCurrencies(strings);
            getExchangeRates();
        }
    }

    /**
     * This async task requests exchange rates from API for EVERY currency in the currency list.
     * Calls setExchangeRates when done.
     */
    private class GetExchangeRatesTask extends AsyncTask<Void, Void, HashMap<String, Double>> {

        @Override
        protected HashMap<String, Double> doInBackground(Void... params) {

            HashMap<String, Double> rates = new HashMap<>();

            for (String currency : currencies) {
                Map<String, Double> tempMap = getRates(currency);
                if (interrupted || tempMap == null){
                    return null;
                }
                rates.putAll(tempMap);
            }

            return rates;
        }

        @Override
        protected void onPostExecute(HashMap<String, Double> result) {
            if (result == null || interrupted){
                done = true;
                return;
            }

            setExchangeRates(result);

        }
    }
}
