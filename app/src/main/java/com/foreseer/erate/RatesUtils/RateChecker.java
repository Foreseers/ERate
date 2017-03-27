package com.foreseer.erate.RatesUtils;

import com.foreseer.erate.Activities.MainActivity;
import com.foreseer.erate.SQL.CurrencyTableHandler;
import com.foreseer.erate.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static RateChecker instance = null;
    private long lastUpdateTime = 0;

    private RateChecker(MainActivity activity) {
        currencies = new ArrayList<>();
        exchangeRates = new HashMap<>();

        done = false;
        interrupted = false;

        this.activity = activity;

        Map<String, Double> asyncRates = new HashMap<>();

        Observable.defer(() -> createConnection().getCurrency())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(model -> model.getRates())
                .map(rates -> rates.keySet())
                .map(set -> {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.addAll(set);
                    return arrayList;
                })
                .map(list -> {
                    list.add(FixerIOApi.BASE_CURRENCY);
                    return list;
                })
                .flatMap(list -> Observable.fromIterable(list))
                .flatMap(currency -> Observable.defer(() -> createConnection().getCurrencyByBase(currency)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())))
                .map(currency -> {
                    HashMap<String, Double> rates = currency.getRates();
                    String baseCurrency = currency.getBase();
                    return Utils.addCurrenciesWithBase(rates, baseCurrency);
                })
                .subscribe(hashMap -> asyncRates.putAll(hashMap), e -> activity.errorOccurred(e.getMessage()), () -> {
                    finishUpdate(asyncRates);
                });
    }

    public static RateChecker getInstance(){
        return instance;
    }

    public static RateChecker newInstance(MainActivity activity){
        instance = new RateChecker(activity);
        return instance;
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
        lastUpdateTime = System.currentTimeMillis();
        exchangeRates = rates;
        done = true;

        CurrencyTableHandler.getInstance().updateRates(rates);

        Observable.defer(() -> Observable.just(CachedExchangeRateStorage.getInstance().updateCachedRates(rates, lastUpdateTime)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {}, e -> activity.errorOccurred(e.getMessage()), () -> {
                    activity.onRatesUpdateFinished();
                    activity.updateLastUpdateTime(getLastUpdateTime());
                    activity.updateExistingFragments();
                });

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

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
