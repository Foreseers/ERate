package com.foreseer.erate.Rates;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Foreseer on 22/03/2017.
 */

public interface FixerIOApi {
    String SERVICE_ENDPOINT = "http://api.fixer.io/";

    String BASE_CURRENCY = "EUR";

    @GET("/latest")
    Observable<CurrencyJsonModel> getCurrency();

    @GET("/latest")
    Observable<CurrencyJsonModel> getCurrencyByBase(@Query("base") String baseCurrency);

}
