package com.foreseer.erate;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.foreseer.erate.Utils.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

    private List<String> currencies;
    private HashMap<String, Double> exchangeRates;

    private final String API_URL = "http://api.fixer.io/latest?";
    private final String BASE_PARAM = "base=";

    private boolean done;


    public RateChecker() {

        currencies = new ArrayList<>();
        exchangeRates = new HashMap<>();
        done = false;

        getCurrencyList();
    }

    public HashMap<String, Double> getRates(){
        return exchangeRates;
    }

    private void getCurrencyList() {
        String url = "http://api.fixer.io/latest?base=EUR";
        GetCurrencyListTask getCurrencyListTask = new GetCurrencyListTask();
        getCurrencyListTask.execute(url);
    }

    private void getExchangeRates() {
        GetExchangeRatesTask getExchangeRatesTask = new GetExchangeRatesTask();
        getExchangeRatesTask.execute();
    }

    private void setExchangeRates(HashMap<String, Double> exchangeRates) {
        this.exchangeRates = exchangeRates;
        this.done = true;
    }

    private void setCurrencies(ArrayList<String> strings) {
        for (String string : strings) {
            currencies.add(string);
        }
    }

    public boolean isDone() {
        return done;
    }

    private HashMap<String, Double> getRates(String baseCurrency) {
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
        } catch (IOException | JSONException e) {
            Log.e("EXCEPTION", e.getMessage());
        }

        return rates;
    }

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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return currencyList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            setCurrencies(strings);
            getExchangeRates();
        }
    }

    private class GetExchangeRatesTask extends AsyncTask<Void, Void, HashMap<String, Double>> {

        @Override
        protected HashMap<String, Double> doInBackground(Void... params) {
            HashMap<String, Double> rates = new HashMap<>();

            for (String currency : currencies) {
                Map<String, Double> tempMap = getRates(currency);
                rates.putAll(tempMap);
            }

            return rates;
        }

        @Override
        protected void onPostExecute(HashMap<String, Double> result) {
            setExchangeRates(result);

        }
    }
}
