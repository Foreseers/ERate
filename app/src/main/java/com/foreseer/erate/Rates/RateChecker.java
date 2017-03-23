package com.foreseer.erate.Rates;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.foreseer.erate.MainActivity;
import com.foreseer.erate.SQL.CurrencyTableHandler;
import com.foreseer.erate.Utils.JsonReader;
import com.foreseer.erate.Utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Foreseer on 09/03/2017.
 */

public class RateChecker {

    // List of all the currencies
    private List<String> currencies;
    // Map of actual exchange rates. String is essentially two currencies, where first is from which currency
    // it's converted
    private Map<String, Double> exchangeRates;

    // URL to the API
    private final String API_URL = "http://api.fixer.io/latest?";
    // Whatever that is, helps forming API request I believe
    private final String BASE_PARAM = "base=";

    // True if ratechecker is done updating exchange rates.
    private boolean done;

    private boolean interrupted;

    private FixerIOApi fixerIOApi;

    private MainActivity activity;

    public RateChecker(MainActivity activity) {

        currencies = new ArrayList<>();
        exchangeRates = new HashMap<>();

        done = false;
        interrupted = false;

        this.activity = activity;

        Observable.defer(() -> createConnection().getCurrency())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(model -> model.getRates())
                .map(rates -> rates.keySet())
                .subscribe(curr -> {
                    currencies = new ArrayList<>(curr);
                    currencies.add(FixerIOApi.BASE_CURRENCY);
                    fetchAllRates();
                }, error -> activity.errorOccurred(error.getMessage()));
    }

    /**
     * This method returns the exchangeRates hashmap
     * @return HashMap with exchange rates
     */
    public Map<String, Double> getRates(){
        return exchangeRates;
    }

    private FixerIOApi createConnection(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        fixerIOApi = new Retrofit.Builder()
                .baseUrl(FixerIOApi.SERVICE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()
                .create(FixerIOApi.class);
        return fixerIOApi;
    }

    private void finishUpdate(Map<String, Double> rates){
        exchangeRates = rates;
        done = true;
        CurrencyTableHandler.getInstance().updateRates(rates);
    }

    private boolean putNewRate(HashMap<String, Double> map, Map.Entry<String, Double> entry, String baseCurrency){
        map.put(baseCurrency + entry.getKey(), entry.getValue());
        if (map.size() == currencies.size() * (currencies.size() - 1)){
            return true;
        }
        return false;
    }

    private void fetchAllRates(){
        HashMap<String, Double> allRates = new HashMap<>();
        for (String currency : currencies){
            fixerIOApi.getCurrencyByBase(currency)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(rates -> {
                        for (Map.Entry<String, Double> rate : rates.getRates().entrySet()){
                            if (putNewRate(allRates, rate, currency)){
                                finishUpdate(allRates);
                            }
                        }
                    }, error -> activity.errorOccurred(error.getMessage()));
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

}
