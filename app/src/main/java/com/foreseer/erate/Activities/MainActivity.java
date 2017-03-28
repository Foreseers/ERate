package com.foreseer.erate.Activities;

import android.animation.LayoutTransition;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.Fragments.AbstractRateFragment;
import com.foreseer.erate.Fragments.AddDialog;
import com.foreseer.erate.Fragments.CurrencyFragment;
import com.foreseer.erate.Fragments.RateFragment;
import com.foreseer.erate.R;
import com.foreseer.erate.RatesUtils.CachedExchangeRateStorage;
import com.foreseer.erate.RatesUtils.RateChecker;
import com.foreseer.erate.SQL.CurrencyTableHandler;
import com.foreseer.erate.SQL.FragmentTableHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements RateFragment.OnFragmentInteractionListener, AddDialog.onDialogInteractionListener,
        CurrencyFragment.OnCurrencySelection {

    //Handler of the fragment SQL table, which stores all the active fragments
    private FragmentTableHandler fragmentTableHandler;
    //Handler of the currency SQL table, which stores actual exchange rates for currencies
    private CurrencyTableHandler currencyTableHandler;

    //Array list with all active fragments, in case if rates need to be updated
    private List<RateFragment> activeFragments;

    private CachedExchangeRateStorage cachedExchangeRateStorage;

    private DialogFragment addDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activeFragments = new ArrayList<>();

        addDialog = new AddDialog();

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.linearLayout);
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(200);
        viewGroup.setLayoutTransition(transition);

        cachedExchangeRateStorage = CachedExchangeRateStorage.getInstance();

        fragmentTableHandler = FragmentTableHandler.getInstance(getApplicationContext());
        //Shared DB
        currencyTableHandler = CurrencyTableHandler.getInstance(fragmentTableHandler.getDbWritable(), this);
        currencyTableHandler.startup(this, false);


        //Find and set the "add fragment" button unclickable unless we have exchange rates ready
        Button button = (Button) findViewById(R.id.buttonAddFragment);
        button.setEnabled(false);

        if (currencyTableHandler.isReady()) {
            button.setEnabled(true);
            cachedExchangeRateStorage.updateCachedRates(currencyTableHandler.getAllRates(), currencyTableHandler.getLastUpdateTime());
        } else {
            //In case if we update the currency table(first launch), animate the update button
            ImageButton updateButton = (ImageButton) findViewById(R.id.imageButton_updateRates);
            updateButton.setEnabled(false);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_update);
            progressBar.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.INVISIBLE);
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
                double rate = getRate(fragment.getFirstCurrency(), fragment.getSecondCurrency());
                fragment.updateRate(rate);
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
                    getRate(fragment.getFirstCurrency(), fragment.getSecondCurrency()));
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

    private double getRate(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency){
        if (CachedExchangeRateStorage.getInstance().isSynchronized()){
            return CachedExchangeRateStorage.getInstance().getExchangeRate(firstCurrency, secondCurrency);
        }
        return currencyTableHandler.getExchangeRate(firstCurrency, secondCurrency);
    }

    /**
     * This method is called when the "add fragment" button is pressed.
     * @param view View which called the method
     */
    public void addFragment(View view) {
        showAddDialog();
        //If currency DB isn't ready yet, adding fragments isn't permitted.
//        if (currencyTableHandler.isReady()) {
//            AbstractCurrency nok = CurrencyHelper.getCurrency("NOK");
//            AbstractCurrency rub = CurrencyHelper.getCurrency("RUB");
//            AbstractCurrency usd = CurrencyHelper.getCurrency("USD");
//            AbstractCurrency eur = CurrencyHelper.getCurrency("EUR");
//            AbstractCurrency czk = CurrencyHelper.getCurrency("CZK");
//            AbstractCurrency aud = CurrencyHelper.getCurrency("AUD");
//            /*addFragment(CurrencyHelper.getCurrency("NOK"), CurrencyHelper.getCurrency("RUB"),
//                    currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency("NOK"), CurrencyHelper.getCurrency("RUB")));
//            addFragment(CurrencyHelper.getCurrency("USD"), CurrencyHelper.getCurrency("EUR"),
//                    currencyTableHandler.getExchangeRate(CurrencyHelper.getCurrency("USD"), CurrencyHelper.getCurrency("EUR")));*/
//            addFragment(rub, nok);
//            addFragment(czk, nok);
//            addFragment(rub, czk);
//            addFragment(usd, nok);
//            addFragment(eur, aud);
//        }
    }

    private void showAddDialog(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag("addDialog");
        if (prev != null) {
            ft.remove(prev);
        }

        addDialog.show(getSupportFragmentManager(), "addDialog");
    }

    /**
     * This method is called when the update button is pressed.
     * Essentially it creates a new RateChecker instance, which updates the rates and updates the DB when it's done.
     * @param view View which called the method
     */
    public void updateRates(View view) {
        if (!isConnectedToInternet()){
            showShortToast("No internet connection!");
            return;
        }
        //New ratechecker with "forUpdates" parametre set to true.
        final RateChecker newChecker = RateChecker.newInstance(this);
        view.setEnabled(false);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_update);
        progressBar.setVisibility(View.VISIBLE);
        view.setVisibility(View.INVISIBLE);
    }


    private boolean isConnectedToInternet(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This method actually adds the fragment onto the activity.
     * @param firstCurrency     First currency of the fragment
     * @param secondCurrency    Second currency of the fragment
     */
    private void addFragment(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RateFragment rateFragment = new RateFragment();
        rateFragment.setCurrency(firstCurrency, secondCurrency, getRate(firstCurrency, secondCurrency));

        fragmentTableHandler.createFragment(firstCurrency.getCurrencyCode(), secondCurrency.getCurrencyCode());
        int availableSQLID = fragmentTableHandler.getCount();

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

        runOnUiThread(() -> {
            showShortToast("Rates updated!");
            ImageButton button = (ImageButton) findViewById(R.id.imageButton_updateRates);
            button.setEnabled(true);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_update);
            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);

            //If the "add fragment" button is unclickable, set it clickable.
            Button addButton = (Button) findViewById(R.id.buttonAddFragment);
            if (!addButton.isEnabled()) {
                addButton.setEnabled(true);
            }


        });
    }

    /**
     * This method updates the "last update time" text view.
     * @param lastTime  Last time of update, in milliseconds.
     */
    public void updateLastUpdateTime(final long lastTime) {
        runOnUiThread(() -> {
            TextView lastUpdateTV = (TextView) findViewById(R.id.textView_lastUpdateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
            Date resultdate = new Date(lastTime);

            lastUpdateTV.setText("Rates were last updated at: " + sdf.format(resultdate));
        });

    }

    public void errorOccurred(String text){
        if (        text.contains("Unable to resolve")
                ||  text.contains("Timeout")) {
            showShortToast("Problems with internet connection!");
        } else {
            showShortToast(text);
        }
        runOnUiThread(() -> {
            ImageButton updateButton = (ImageButton) findViewById(R.id.imageButton_updateRates);
            updateButton.setEnabled(true);

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_update);
            progressBar.setVisibility(View.INVISIBLE);
            updateButton.setVisibility(View.VISIBLE);
        });
    }

    public void showShortToast(String text){
        runOnUiThread(() -> {
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        });
    }

    @Override
    public void onAddFragment() {
        showShortToast("Added!");
    }

    @Override
    public void onCancel() {
        //clearGlide();
        showShortToast("Cancelled!");
    }

    public void clearGlide(){
        new Thread(() -> Glide.get(this).clearDiskCache()).start();
        Glide.get(this).clearMemory();
    }


    @Override
    public void onCurrencySelection(AbstractCurrency currency) {
        showShortToast("Selected currency!");
    }
}
