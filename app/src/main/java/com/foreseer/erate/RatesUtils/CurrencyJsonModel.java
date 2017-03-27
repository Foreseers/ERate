package com.foreseer.erate.RatesUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class CurrencyJsonModel {

@SerializedName("base")
@Expose
private String base;
@SerializedName("date")
@Expose
private String date;

@SerializedName("rates")
@Expose
private HashMap<String, Double> rates;

public String getBase() {
return base;
}

public void setBase(String base) {
this.base = base;
}

public String getDate() {
return date;
}

public void setDate(String date) {
this.date = date;
}

public HashMap<String, Double> getRates() {
return rates;
}

public void setRates(HashMap rates) {
this.rates = rates;
}

}