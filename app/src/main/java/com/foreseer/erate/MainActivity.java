package com.foreseer.erate;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.Fragments.AbstractRateFragment;
import com.foreseer.erate.Fragments.RateFragment;
import com.foreseer.erate.SQL.CurrencyTableHandler;
import com.foreseer.erate.SQL.FragmentTableHandler;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RateFragment.OnFragmentInteractionListener{

    private int count;
    private FragmentTableHandler fragmentTableHandler;
    private CurrencyTableHandler currencyTableHandler;

    private boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentTableHandler = FragmentTableHandler.getInstance(getApplicationContext());
        count = fragmentTableHandler.getCount();
        currencyTableHandler = CurrencyTableHandler.getInstance(getApplicationContext(), fragmentTableHandler.getDbWritable(), this, false);
        currencyTableHandler.startup(this, false);

    }

    public void addItem(View view){

    }

    public void updateExistingFragments(){
        ArrayList<AbstractRateFragment> fragments = fragmentTableHandler.getExistingFragments();

        if (fragments == null){
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        for (AbstractRateFragment fragment : fragments){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            RateFragment rateFragment = new RateFragment();
            rateFragment.setSqlID(fragment.getId());
            fragmentTransaction.add(R.id.linearLayout, rateFragment, "RateFragment" + fragment.getId());
            fragmentTransaction.commit();

            rateFragment.setCurrency(fragment.getFirstCurrency(), fragment.getSecondCurrency(),
                    currencyTableHandler.getExchangeRate(fragment.getFirstCurrency(), fragment.getSecondCurrency()));
        }
    }

    public void addFragment(View view){
        addFragment(CurrencyHelper.getCurrency("NOK"), CurrencyHelper.getCurrency("RUB"),
                currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency("NOK"), CurrencyHelper.getCurrency("RUB")));
        addFragment(CurrencyHelper.getCurrency("USD"), CurrencyHelper.getCurrency("EUR"),
                currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency("USD"), CurrencyHelper.getCurrency("EUR")));
    }

    private void addFragment(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency, double rate){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RateFragment rateFragment = new RateFragment();
        rateFragment.setCurrency(firstCurrency, secondCurrency,
                currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency(firstCurrency.getCurrencyCode()), CurrencyHelper.getCurrency(secondCurrency.getCurrencyCode())));

        fragmentTableHandler.createFragment(firstCurrency.getCurrencyCode(), secondCurrency.getCurrencyCode());
        count = fragmentTableHandler.getCount();

        fragmentTransaction.add(R.id.linearLayout, rateFragment, "RateFragment" + count);
        fragmentTransaction.commit();

        rateFragment.setSqlID(count);
    }

    @Override
    public void onFragmentDeletion(int id) {
        fragmentTableHandler.removeFragment(id);
        RateFragment fragment = (RateFragment) getSupportFragmentManager().findFragmentByTag("RateFragment" + id);
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    @Override
    protected void onDestroy() {
        fragmentTableHandler.close();
        super.onDestroy();
    }
}
