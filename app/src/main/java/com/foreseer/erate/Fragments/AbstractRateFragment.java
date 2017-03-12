package com.foreseer.erate.Fragments;

import com.foreseer.erate.Currency.AbstractCurrency;

/**
 * Created by Foreseer on 11/03/2017.
 */

public class AbstractRateFragment {
    private int id;
    private AbstractCurrency firstCurrency;
    private AbstractCurrency secondCurrency;

    public AbstractRateFragment(int id, AbstractCurrency firstCurrency, AbstractCurrency secondCurrency) {
        this.id = id;
        this.firstCurrency = firstCurrency;
        this.secondCurrency = secondCurrency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AbstractCurrency getFirstCurrency() {
        return firstCurrency;
    }

    public void setFirstCurrency(AbstractCurrency firstCurrency) {
        this.firstCurrency = firstCurrency;
    }

    public AbstractCurrency getSecondCurrency() {
        return secondCurrency;
    }

    public void setSecondCurrency(AbstractCurrency secondCurrency) {
        this.secondCurrency = secondCurrency;
    }
}
