package com.foreseer.erate.RatesUtils;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.SQL.CurrencyTableHandler;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CachedExchangeRateStorage {
    private HashMap<String, Double> exchangeRates;

    private static CachedExchangeRateStorage instance;

    private boolean isReady;

    private long lastUpdateTime;
    private boolean startedSynchronization;


    private CachedExchangeRateStorage () {
        exchangeRates = new HashMap<>();
        isReady = false;
        lastUpdateTime = 0;
        startedSynchronization = false;

        CurrencyTableHandler currencyTableHandler = CurrencyTableHandler.getInstance();
        if (currencyTableHandler == null){
            return;
        }
        if (currencyTableHandler.isReady()){
            Observable.defer(() -> Observable.just(updateCachedRates()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public static CachedExchangeRateStorage getInstance(){
        if (instance == null){
            instance = new CachedExchangeRateStorage();
            return instance;
        }
        return instance;
    }

    public boolean updateCachedRates(Map<String, Double> exchangeRates, long lastUpdateTime){
        this.exchangeRates.putAll(exchangeRates);
        this.lastUpdateTime = lastUpdateTime;
        isReady = true;
        return true;
    }

    private boolean updateCachedRates(){
        if (RateChecker.getInstance() != null) {
            if (RateChecker.getInstance().isDone()) {
                return updateCachedRates(RateChecker.getInstance().getRates(), RateChecker.getInstance().getLastUpdateTime());
            }
        }

        CurrencyTableHandler instance = CurrencyTableHandler.getInstance();
        if (instance != null) {
            return updateCachedRates(instance.getAllRates(), instance.getLastUpdateTime());
        }

        return false;
    }

    public double getExchangeRate(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency){
        for (Map.Entry<String, Double> entry : exchangeRates.entrySet()){
            if (entry.getKey().startsWith(firstCurrency.getCurrencyCode())
                    && entry.getKey().endsWith(secondCurrency.getCurrencyCode())){
                return entry.getValue();
            }
        }
        return 0;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isSynchronized(){
        if (CurrencyTableHandler.getInstance() == null){
            return false;
        }
        if (RateChecker.getInstance() != null) {
            if (lastUpdateTime == RateChecker.getInstance().getLastUpdateTime()) {
                return true;
            }
        }

        if (lastUpdateTime == CurrencyTableHandler.getInstance().getLastUpdateTime()){
            return true;
        }

        if (!startedSynchronization) {
            startedSynchronization = true;
            Observable.defer(() -> Observable.just(updateCachedRates()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(result -> startedSynchronization = false);
        }

        return false;
    }
}
