package com.foreseer.erate;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.Fragments.AbstractRateFragment;
import com.foreseer.erate.Fragments.RateFragment;
import com.foreseer.erate.SQL.CurrencyTableHandler;
import com.foreseer.erate.SQL.FragmentTableHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RateFragment.OnFragmentInteractionListener {

    //Handler of the fragment SQL table, which stores all the active fragments
    private FragmentTableHandler fragmentTableHandler;
    //Handler of the currency SQL table, which stores actual exchange rates for currencies
    private CurrencyTableHandler currencyTableHandler;

    //Array list with all active fragments, in case if rates need to be updated
    private List<RateFragment> activeFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activeFragments = new ArrayList<>();


        fragmentTableHandler = FragmentTableHandler.getInstance(getApplicationContext());
        //Shared DB
        currencyTableHandler = CurrencyTableHandler.getInstance(getApplicationContext(), fragmentTableHandler.getDbWritable(), this, false);
        currencyTableHandler.startup(this, false);

        //Find and set the "add fragment" button unclickable unless we have exchange rates ready
        Button button = (Button) findViewById(R.id.buttonAddFragment);
        button.setClickable(false);

        if (currencyTableHandler.isReady()) {
            button.setClickable(true);
        } else {
            //In case if we update the currency table(first launch), animate the update button
            ImageButton updateButton = (ImageButton) findViewById(R.id.imageButton_updateRates);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.refreshbuttonrotate);
            updateButton.startAnimation(animation);
            updateButton.setClickable(false);
        }
    }

    /**
     * This method is either used to update existing visible fragments in the activity (if they were already loaded),
     * or load fragments from the database into the activity.
     */
    public void updateExistingFragments() {
        //If there already are visible loaded fragments
        if (activeFragments.size() != 0) {
            for (RateFragment fragment : activeFragments) {
                fragment.updateRate(currencyTableHandler.getExchangeRate(fragment.getFirstCurrency(), fragment.getSecondCurrency()));
            }
            return;
        }

        //Get list of fragments from the DB
        ArrayList<AbstractRateFragment> fragments = fragmentTableHandler.getExistingFragments();

        //If there are no fragments(first start of the app), we don't do anything
        if (fragments == null) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        //If there are fragments, add all of them onto the activity
        for (AbstractRateFragment fragment : fragments) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            RateFragment rateFragment = new RateFragment();
            rateFragment.setSqlID(fragment.getId());
            fragmentTransaction.add(R.id.linearLayout, rateFragment, "RateFragment" + fragment.getId());
            fragmentTransaction.commit();

            rateFragment.setCurrency(fragment.getFirstCurrency(), fragment.getSecondCurrency(),
                    currencyTableHandler.getExchangeRate(fragment.getFirstCurrency(), fragment.getSecondCurrency()));
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        //Every time a fragment is attached, if it's a RateFragment, add into the active fragments list
        if (fragment instanceof RateFragment) {
            activeFragments.add((RateFragment) fragment);
        }
        super.onAttachFragment(fragment);
    }

    /**
     * This method is called when the "add fragment" button is pressed.
     * @param view View which called the method
     */
    public void addFragment(View view) {
        //If currency DB isn't ready yet, adding fragments isn't permitted.
        if (currencyTableHandler.isReady()) {
            addFragment(CurrencyHelper.getCurrency("NOK"), CurrencyHelper.getCurrency("RUB"),
                    currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency("NOK"), CurrencyHelper.getCurrency("RUB")));
            addFragment(CurrencyHelper.getCurrency("USD"), CurrencyHelper.getCurrency("EUR"),
                    currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency("USD"), CurrencyHelper.getCurrency("EUR")));
        }
    }

    /**
     * This method is called when the update button is pressed.
     * Essentially it creates a new RateChecker instance, which updates the rates and updates the DB when it's done.
     * @param view View which called the method
     */
    public void updateRates(View view) {
        //New ratechecker with "forUpdates" parametre set to true.
        new RateChecker(true);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.refreshbuttonrotate);
        view.startAnimation(animation);
        view.setClickable(false);
    }

    /**
     * This method actually adds the fragment onto the activity.
     * @param firstCurrency     First currency of the fragment
     * @param secondCurrency    Second currency of the fragment
     * @param rate              Exchange rate
     */
    private void addFragment(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency, double rate) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RateFragment rateFragment = new RateFragment();
        rateFragment.setCurrency(firstCurrency, secondCurrency,
                currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency(firstCurrency.getCurrencyCode()),
                        CurrencyHelper.getCurrency(secondCurrency.getCurrencyCode())));

        fragmentTableHandler.createFragment(firstCurrency.getCurrencyCode(), secondCurrency.getCurrencyCode());
        int availableSQLID = fragmentTableHandler.getCount() + 1;

        fragmentTransaction.add(R.id.linearLayout, rateFragment, "RateFragment" + availableSQLID);
        fragmentTransaction.commit();

        rateFragment.setSqlID(availableSQLID);
    }

    /**
     * This method is called when the "delete" button is pressed inside the fragment.
     * Probably will be called by swiping in future and whatnot.
     * @param id SqlID of the fragment.
     */
    @Override
    public void onFragmentDeletion(int id) {
        fragmentTableHandler.removeFragment(id);
        for (RateFragment fragment : activeFragments) {
            if (fragment.getSqlID() == id) {
                activeFragments.remove(fragment);
                break;
            }
        }
        RateFragment fragment = (RateFragment) getSupportFragmentManager().findFragmentByTag("RateFragment" + id);
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    @Override
    protected void onDestroy() {
        fragmentTableHandler.close();
        super.onDestroy();
    }

    /**
     * When update of rates is finished, this method shows a toast, clears animations and, if needed, makes buttons clickable.
     */
    public void onRatesUpdateFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence text = "Rates updated!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();

                ImageButton button = (ImageButton) findViewById(R.id.imageButton_updateRates);
                button.clearAnimation();
                button.setClickable(true);

                //If the "add fragment" button is unclickable, set it clickable.
                Button addButton = (Button) findViewById(R.id.buttonAddFragment);
                if (!addButton.isClickable()) {
                    button.setClickable(true);
                }
            }
        });
    }

    /**
     * This method updates the "last update time" text view.
     * @param lastTime  Last time of update, in milliseconds.
     */
    public void updateLastUpdateTime(final long lastTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView lastUpdateTV = (TextView) findViewById(R.id.textView_lastUpdateTime);

                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
                Date resultdate = new Date(lastTime);

                lastUpdateTV.setText("Rates were last updated at: " + sdf.format(resultdate));
            }
        });

    }
}